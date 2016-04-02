package astrac.springy

import org.elasticsearch.index.query.QueryBuilders
import testkit._
import CirceSerialization._
import io.circe.generic.auto._
import api.IndexIO._
import api.Query
import org.scalatest.{ FlatSpec, Matchers }

class SearchApiSpecs extends FlatSpec with ElasticsearchSpec with Matchers {

  val indexedDocuments = for {
    _ <- deleteIndex("es_test").asTry
    sirens <- indexDocument("es_test", "book", None, Fixtures.sirensOfTitan)
    protocols <- indexDocument("es_test", "book", None, Fixtures.protocolsOfTralfamadore)
    _ = commitEs()
  } yield (sirens, protocols)

  "The Search API support" should "allow issuing MatchAll queries" in {
    val io = for {
      docs <- indexedDocuments
      (sirens, protocols) = docs
      list <- search[Book]("es_test", "book", Query.MatchAll)
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

  it should "allow issuing Term queries" in {
    val io = for {
      docs <- indexedDocuments
      list <- search[Book]("es_test", "book", Query.Term("title", "sirens"))
    } yield (list, docs._1)

    val (list, sirensResp) = io.foldMap(unsafeInterpreter)

    list.hits.total shouldEqual 1
    list.hits.hits.foreach { h =>
      h._index shouldEqual "es_test"
      h._type shouldEqual "book"
    }

    list.hits.hits.map(_._id) should contain theSameElementsAs(sirensResp._id :: Nil)

    list.hits.hits.map(_._source) should contain theSameElementsAs(Fixtures.sirensOfTitan :: Nil)
  }

  it should "allow issuing Native queries" in {
    val query = Query.Native(QueryBuilders.termQuery("author", "trout"))

    val io = for {
      docs <- indexedDocuments
      list <- search[Book]("es_test", "book", query)
    } yield (list, docs._2)

    val (list, protocolsResp) = io.foldMap(unsafeInterpreter)

    list.hits.total shouldEqual 1
    list.hits.hits.foreach { h =>
      h._index shouldEqual "es_test"
      h._type shouldEqual "book"
    }

    list.hits.hits.map(_._id) should contain theSameElementsAs(protocolsResp._id :: Nil)

    list.hits.hits.map(_._source) should contain theSameElementsAs(Fixtures.protocolsOfTralfamadore :: Nil)
  }
}
