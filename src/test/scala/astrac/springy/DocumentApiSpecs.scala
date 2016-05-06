package astrac.springy

// import api._
import astrac.springy.api.{ BulkRequest, DeleteDocumentResponse, IndexDocumentResponse, UpdateDocumentResponse }
import testkit._
import CirceSerialization._
import io.circe.generic.auto._
import api.IndexIO._
import org.scalatest.{FlatSpec, Matchers}

class DocumentApiSpecs extends FlatSpec with ElasticsearchSpec with Matchers {

  "The Document API support" should "allow indexing and retrieving a document" in {
    val io = for {
      idx <- indexDocument("es_test", "book", None, Fixtures.sirensOfTitan)
      get <- getDocument[Book]("es_test", "book", idx._id)
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
      idx <- indexDocument("es_test", "book", None, Fixtures.sirensOfTitan)
      del <- deleteDocument("es_test", "book", idx._id)
      get <- getDocument[Book]("es_test", "book", del._id)
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
      idx <- indexDocument("es_test", "book", None, Fixtures.sirensOfTitan)
      upd <- updateDocument("es_test", "book", idx._id, Fixtures.protocolsOfTralfamadore)
    } yield (idx, upd)

    val (idxResp, updResp) = io.foldMap(unsafeInterpreter)

    updResp._id shouldEqual idxResp._id
    updResp._index shouldEqual "es_test"
    updResp._type shouldEqual "book"
    updResp._version shouldEqual 2
  }

  it should "allow performing bulk operations" in {
    def buildBulk(delId: String, updId: String) = Seq(
      BulkRequest.indexDocument("es_test", "book", None, Fixtures.gospelFromOuterSpace),
      BulkRequest.deleteDocument("es_test", "book", delId),
      BulkRequest.updateDocument("es_test", "book", updId, Fixtures.slaughterhousFive)
    )

    val io = for {
      del <- indexDocument("es_test", "book", None, Fixtures.protocolsOfTralfamadore)
      upd <- indexDocument("es_test", "book", None, Fixtures.sirensOfTitan)
      blk <- bulk(buildBulk(del._id, upd._id))
      delBlk <- getDocument[Book]("es_test", "book", del._id)
      updBlk <- getDocument[Book]("es_test", "book", upd._id)
    } yield (blk, delBlk, updBlk)

    val (blkResp, delResp, updResp) = io.foldMap(unsafeInterpreter)

    blkResp.items should have size(3)
    blkResp.items should matchPattern {
      case Seq(_: IndexDocumentResponse, _: DeleteDocumentResponse, _: UpdateDocumentResponse) =>
    }

    delResp.found should be(false)
    updResp.found should be(true)
    updResp._version should equal(2)
    updResp.document should equal(Some(Fixtures.slaughterhousFive))
  }
}
