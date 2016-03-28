package astrac.springy

import api._
import testkit._
import CirceSerialization._
import io.circe.generic.auto._
import org.scalatest.{ FlatSpec, Matchers }

class SearchApiSpecs extends FlatSpec with ElasticsearchSpec with Matchers {

  "The Search API support" should "allow issuing queries" in {
    val io = for {
      sirens <- IndexIO.indexDocument("es_test", "book", None, Fixtures.sirensOfTitan)
      protocols <- IndexIO.indexDocument("es_test", "book", None, Fixtures.protocolsOfTralfamadore)
      _ = commitEs()
      list <- IndexIO.search[Book]("es_test", "book", Query.MatchAll)
    } yield (list, sirens, protocols)

    val (list, sirensResp, protocolsResp) = io.foldMap(unsafeInterpreter)

    list.hits.total shouldEqual 2
    list.hits.hits.foreach { h =>
      h._index shouldEqual "es_test"
      h._type shouldEqual "book"
    }

    list.hits.hits.map(_._id) should contain theSameElementsAs(sirensResp._id :: protocolsResp._id :: Nil)

    list.hits.hits.map(_._source) should contain theSameElementsAs(Fixtures.sirensOfTitan :: Fixtures.protocolsOfTralfamadore :: Nil)
  }
}
