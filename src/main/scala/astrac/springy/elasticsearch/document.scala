package astrac.springy.elasticsearch

import astrac.springy._
import astrac.springy.api._
import org.elasticsearch.action.WriteConsistencyLevel
import org.elasticsearch.common.unit.TimeValue
import scala.language.higherKinds
import org.elasticsearch.action.index.{ IndexResponse => EsIndexResponse }
import org.elasticsearch.action.index.{ IndexRequest => EsIndexRequest }
import org.elasticsearch.index.{ VersionType => EsVersionType }
import cats.Applicative

trait IndexSupport {
  class IndexExecutable[T: Writeable] extends Executable[Executor.JavaApi, IndexRequest[T], IndexResponse] {

    def perform[M[_]: Applicative](executor: Executor.JavaApi, request: IndexRequest[T]): M[IndexResponse] = {
      val ex = executor
        .client
        .prepareIndex(request.index, request.`type`, request.id.orNull)
        .setSource(implicitly[Writeable[T]].toBytes(request.document))

      request.ttl.foreach(ex.setTTL)
      request.opType.foreach {
        case OpType.Index => ex.setOpType(EsIndexRequest.OpType.INDEX)
        case OpType.Create => ex.setOpType(EsIndexRequest.OpType.CREATE)
      }
      request.parent.foreach(ex.setParent)
      request.refresh.foreach(ex.setRefresh)
      request.routing.foreach(ex.setRouting)
      request.timestamp.foreach(ex.setTimestamp)
      request.version.foreach(ex.setVersion)
      request.versionType.foreach {
        case VersionType.Internal => ex.setVersionType(EsVersionType.INTERNAL)
        case VersionType.External=> ex.setVersionType(EsVersionType.EXTERNAL)
        case VersionType.ExternalGte => ex.setVersionType(EsVersionType.EXTERNAL_GTE)
        case VersionType.Force => ex.setVersionType(EsVersionType.FORCE)
      }
      request.consistencyLevel.foreach {
        case ConsistencyLevel.One => ex.setConsistencyLevel(WriteConsistencyLevel.ONE)
        case ConsistencyLevel.Quorum => ex.setConsistencyLevel(WriteConsistencyLevel.QUORUM)
        case ConsistencyLevel.All => ex.setConsistencyLevel(WriteConsistencyLevel.ALL)
      }
      request.timeout.foreach(d => ex.setTimeout(TimeValue.timeValueMillis(d.toMillis)))

      implicitly[Applicative[M]].pure {
        val resp = ex.execute().get()
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

      implicitly[Applicative[M]].pure {
        val r = result.get()

        GetResponse(r.getIndex(), r.getType(), r.getId(), r.getVersion(), r.isExists(),
          if (r.isExists()) Some(implicitly[Readable[T]].fromBytes(r.getSourceAsBytes()).getOrElse(???))
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
        .setDoc(implicitly[Writeable[T]].toBytes(request.document))
        .execute()

      implicitly[Applicative[M]].pure {
        val r = result.get()

        UpdateResponse(r.getIndex(), r.getType(), r.getId(), r.getVersion())
      }
    }
  }

  implicit def documentUpdateSupport[T: Writeable]: Executable[Executor.JavaApi, DocumentUpdateRequest[T], UpdateResponse] = new DocumentUpdateExecutable[T]
}

trait DeleteDocumentSupport {
  implicit val deleteSupport = new Executable[Executor.JavaApi, DocumentDeleteRequest, DocumentDeleteResponse] {
    def perform[M[_]: Applicative](executor: Executor.JavaApi, request: DocumentDeleteRequest): M[DocumentDeleteResponse] = {
      val result = executor
        .client
        .prepareDelete(request.index, request.`type`, request.id)
        .execute()

      implicitly[Applicative[M]].pure {
        val r = result.get()

        DocumentDeleteResponse(r.getIndex(), r.getType(), r.getId(), r.getVersion(), r.isFound())
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
