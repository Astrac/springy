package astrac.springy

import api._
import elasticsearch._
import SprayJsonSerialization._
import org.scalatest.{ FlatSpec, Matchers }
import scalaz._
import Scalaz._

class DocumentApiSpecs extends FlatSpec with ElasticsearchSpec with Protocol with Matchers {

  "The Document API support" should "allow indexing and retrieving a document" in {
    val idxResp = executor.execute(IndexRequest("es_test", "book", None, Fixtures.sirensOfTitan)).to[Id]

    idxResp._index shouldEqual "es_test"
    idxResp._type shouldEqual "book"
    idxResp._version shouldEqual 1
    idxResp.created shouldBe true

    val getResp = executor.execute(GetRequest[Book]("es_test", "book", idxResp._id)).to[Id]

    getResp._index shouldEqual "es_test"
    getResp._type shouldEqual "book"
    getResp._version shouldEqual 1
    getResp._id shouldEqual idxResp._id
    getResp.found shouldBe true
    getResp.document shouldEqual Some(Fixtures.sirensOfTitan)
  }

}
