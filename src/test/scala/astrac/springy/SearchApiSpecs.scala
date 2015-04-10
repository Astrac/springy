package astrac.springy

import api._
import elasticsearch.DocumentApiSupport._
import elasticsearch.SearchApiSupport._
import SprayJsonSerialization._
import org.scalatest.{ FlatSpec, Matchers }
import scalaz._
import Scalaz._

class SearchApiSpecs extends FlatSpec with ElasticsearchSpec with Protocol with Matchers {

  "The Search API support" should "allow issuing queries" in {
    val sirensResp = executor.execute(IndexRequest("es_test", "book", None, Fixtures.sirensOfTitan)).to[Id]

    val protocolsResp = executor.execute(IndexRequest("es_test", "book", None, Fixtures.protocolsOfTralfamadore)).to[Id]

    commitEs()

    val list = executor.execute(SearchRequest[Book]("es_test", "book", Query.MatchAll)).to[Id]

    list.hits.total shouldEqual 2
    list.hits.hits.foreach { h =>
      h._index shouldEqual "es_test"
      h._type shouldEqual "book"
    }

    list.hits.hits.map(_._id) should contain theSameElementsAs(sirensResp._id :: protocolsResp._id :: Nil)

    list.hits.hits.map(_._source) should contain theSameElementsAs(Fixtures.sirensOfTitan :: Fixtures.protocolsOfTralfamadore :: Nil)
  }
}
