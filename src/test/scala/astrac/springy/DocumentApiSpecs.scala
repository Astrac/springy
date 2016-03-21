package astrac.springy

import api._
import testkit._
import elasticsearch.DocumentApiSupport._
import CirceSerialization._
import io.circe.generic.auto._
import org.scalatest.{ FlatSpec, Matchers }
import cats.Id

class DocumentApiSpecs extends FlatSpec with ElasticsearchSpec with Matchers {

  "The Document API support" should "allow indexing and retrieving a document" in {
    val idxResp = springy.execute(IndexRequest("es_test", "book", None, Fixtures.sirensOfTitan)).to[Id]

    idxResp._index shouldEqual "es_test"
    idxResp._type shouldEqual "book"
    idxResp._version shouldEqual 1
    idxResp.created shouldBe true

    val getResp = springy.execute(GetRequest[Book]("es_test", "book", idxResp._id)).to[Id]

    getResp._index shouldEqual "es_test"
    getResp._type shouldEqual "book"
    getResp._version shouldEqual 1
    getResp._id shouldEqual idxResp._id
    getResp.found shouldBe true
    getResp.document shouldEqual Some(Fixtures.sirensOfTitan)
  }

  it should "allow indexing and deleting a document" in {
    val idxResp = springy.execute(IndexRequest("es_test", "book", None, Fixtures.sirensOfTitan)).to[Id]

    val delResp = springy.execute(DocumentDeleteRequest("es_test", "book", idxResp._id)).to[Id]

    delResp.found shouldBe true
    delResp._id shouldEqual idxResp._id
    delResp._index shouldEqual "es_test"
    delResp._type shouldEqual "book"

    val getResp = springy.execute(GetRequest[Book]("es_test", "book", delResp._id)).to[Id]

    getResp.found shouldBe false
    getResp.document shouldBe None
  }

  it should "allow indexing and updating a document" in {
    val idxResp = springy.execute(IndexRequest("es_test", "book", None, Fixtures.sirensOfTitan)).to[Id]

    val updResp = springy.execute(DocumentUpdateRequest("es_test", "book", idxResp._id, Fixtures.protocolsOfTralfamadore)).to[Id]

    updResp._id shouldEqual idxResp._id
    updResp._index shouldEqual "es_test"
    updResp._type shouldEqual "book"
    updResp._version shouldEqual 2
  }
}
