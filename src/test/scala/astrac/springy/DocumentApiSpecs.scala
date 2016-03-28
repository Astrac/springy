package astrac.springy

import api._
import testkit._
import CirceSerialization._
import io.circe.generic.auto._
import org.scalatest.{FlatSpec, Matchers}

class DocumentApiSpecs extends FlatSpec with ElasticsearchSpec with Matchers {

  "The Document API support" should "allow indexing and retrieving a document" in {
    val io = for {
      idx <- IndexIO.indexDocument("es_test", "book", None, Fixtures.sirensOfTitan)
      get <- IndexIO.getDocument[Book]("es_test", "book", idx._id)
    } yield (idx, get)

    val (idxResp, getResp) = io.foldMap(unsafeInterpreter)

    idxResp._index shouldEqual "es_test"
    idxResp._type shouldEqual "book"
    idxResp._version shouldEqual 1
    idxResp.created shouldBe true

    getResp._index shouldEqual "es_test"
    getResp._type shouldEqual "book"
    getResp._version shouldEqual 1
    getResp._id shouldEqual idxResp._id
    getResp.found shouldBe true
    getResp.document shouldEqual Some(Fixtures.sirensOfTitan)
  }

  it should "allow indexing and deleting a document" in {
    val io = for {
      idx <- IndexIO.indexDocument("es_test", "book", None, Fixtures.sirensOfTitan)
      del <- IndexIO.deleteDocument("es_test", "book", idx._id)
      get <- IndexIO.getDocument[Book]("es_test", "book", del._id)
    } yield (idx, del, get)

    val (idxResp, delResp, getResp) = io.foldMap(unsafeInterpreter)

    delResp.found shouldBe true
    delResp._id shouldEqual idxResp._id
    delResp._index shouldEqual "es_test"
    delResp._type shouldEqual "book"

    getResp.found shouldBe false
    getResp.document shouldBe None
  }

  it should "allow indexing and updating a document" in {
    val io = for {
      idx <- IndexIO.indexDocument("es_test", "book", None, Fixtures.sirensOfTitan)
      upd <- IndexIO.updateDocument("es_test", "book", idx._id, Fixtures.protocolsOfTralfamadore)
    } yield (idx, upd)

    val (idxResp, updResp) = io.foldMap(unsafeInterpreter)

    updResp._id shouldEqual idxResp._id
    updResp._index shouldEqual "es_test"
    updResp._type shouldEqual "book"
    updResp._version shouldEqual 2
  }
}
