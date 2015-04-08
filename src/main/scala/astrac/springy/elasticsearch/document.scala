package astrac.springy.elasticsearch

import astrac.springy._
import astrac.springy.api._
import scala.language.higherKinds
import org.elasticsearch.action.index.{ IndexResponse => EsIndexResponse }
import scalaz.Applicative

trait IndexSupport {
  class IndexExecutable[T: Writeable] extends Executable[Executor.JavaApi, IndexRequest[T], IndexResponse] {

    def perform[M[_]: Applicative](executor: Executor.JavaApi, request: IndexRequest[T]): M[IndexResponse] = {
      val result = executor
        .client
        .prepareIndex(request.index, request.`type`, request.id.orNull)
        .setSource(implicitly[Writeable[T]].toBytes(request.document))
        .execute()

      implicitly[Applicative[M]].point {
        val resp = result.get()
        IndexResponse(resp.getIndex(), resp.getType(), resp.getId(), resp.getVersion(), resp.isCreated())
      }
    }
  }

  implicit def indexSupport[T: Writeable]: Executable[Executor.JavaApi, IndexRequest[T], IndexResponse] = new IndexExecutable[T]
}

trait GetDocumentSupport {
  class GetDocumentExecutable[T: Readable] extends Executable[Executor.JavaApi, GetRequest[T], GetResponse[T]] {
    def perform[M[_]: Applicative](executor: Executor.JavaApi, request: GetRequest[T]): M[GetResponse[T]] = {
      val result = executor
        .client
        .prepareGet(request.index, request.`type`, request.id)
        .execute()

      implicitly[Applicative[M]].point {
        val r = result.get()

        GetResponse(r.getIndex(), r.getType(), r.getId(), r.getVersion(), r.isExists(),
          if (r.isExists()) Some(implicitly[Readable[T]].fromBytes(r.getSourceAsBytes()))
          else None)
      }
    }
  }

  implicit def getSupport[T: Readable]: Executable[Executor.JavaApi, GetRequest[T], GetResponse[T]] = new GetDocumentExecutable[T]
}

trait UpdateDocumentSupport {
  class DocumentUpdateExecutable[T: Writeable] extends Executable[Executor.JavaApi, DocumentUpdateRequest[T], UpdateResponse] {
    def perform[M[_]: Applicative](executor: Executor.JavaApi, request: DocumentUpdateRequest[T]): M[UpdateResponse] = {
      val result = executor
        .client
        .prepareUpdate(request.index, request.`type`, request.id)
        .execute()

      implicitly[Applicative[M]].point {
        val r = result.get()

        UpdateResponse(r.getIndex(), r.getType(), r.getId(), r.getVersion())
      }
    }
  }

  implicit def documentUpdateSupport[T: Writeable]: Executable[Executor.JavaApi, DocumentUpdateRequest[T], UpdateResponse] = new DocumentUpdateExecutable[T]
}

trait DeleteDocumentSupport {
  implicit val deleteSupport = new Executable[Executor.JavaApi, DeleteRequest, DeleteResponse] {
    def perform[M[_]: Applicative](executor: Executor.JavaApi, request: DeleteRequest): M[DeleteResponse] = {
      val result = executor
        .client
        .prepareDelete(request.index, request.`type`, request.id)
        .execute()

      implicitly[Applicative[M]].point {
        val r = result.get()

        DeleteResponse(r.getIndex(), r.getType(), r.getId(), r.getVersion(), r.isFound())
      }
    }
  }
}

trait DocumentApiSupport
  extends Document[Executor.JavaApi]
  with IndexSupport
  with GetDocumentSupport
  with UpdateDocumentSupport
  with DeleteDocumentSupport

object DocumentApiSupport extends DocumentApiSupport
