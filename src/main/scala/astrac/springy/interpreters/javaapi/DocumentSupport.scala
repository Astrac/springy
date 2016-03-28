package astrac.springy
package interpreters
package javaapi

import api._
import org.elasticsearch.action.{ ActionWriteResponse, WriteConsistencyLevel }
import org.elasticsearch.client.Client
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.action.index.{IndexRequest => EsIndexRequest, IndexResponse => EsIndexResponse}
import org.elasticsearch.action.delete.{DeleteResponse => EsDeleteResponse}
import org.elasticsearch.action.update.{UpdateResponse => EsUpdateResponse}
import org.elasticsearch.index.{VersionType => EsVersionType}

trait DocumentSupport {
  private def indexDocumentRequest[T: Writeable](client: Client, request: IndexDocumentRequest[T]) = {
    val ex = client
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
      case VersionType.External => ex.setVersionType(EsVersionType.EXTERNAL)
      case VersionType.ExternalGte => ex.setVersionType(EsVersionType.EXTERNAL_GTE)
      case VersionType.Force => ex.setVersionType(EsVersionType.FORCE)
    }
    request.consistencyLevel.foreach {
      case ConsistencyLevel.One => ex.setConsistencyLevel(WriteConsistencyLevel.ONE)
      case ConsistencyLevel.Quorum => ex.setConsistencyLevel(WriteConsistencyLevel.QUORUM)
      case ConsistencyLevel.All => ex.setConsistencyLevel(WriteConsistencyLevel.ALL)
    }
    request.timeout.foreach(d => ex.setTimeout(TimeValue.timeValueMillis(d.toMillis)))

    ex
  }

  private def indexDocumentResponse(r: EsIndexResponse): IndexDocumentResponse =
    IndexDocumentResponse(r.getIndex(), r.getType(), r.getId(), r.getVersion(), r.isCreated())

  private def updateDocumentRequest[T: Writeable](client: Client, request: UpdateDocumentRequest[T]) = client
    .prepareUpdate(request.index, request.`type`, request.id)
    .setDoc(implicitly[Writeable[T]].toBytes(request.document))

  private def updateDocumentResponse(r: EsUpdateResponse): UpdateDocumentResponse =
    UpdateDocumentResponse(r.getIndex(), r.getType(), r.getId(), r.getVersion())

  private def deleteDocumentRequest(client: Client, request: DeleteDocumentRequest) = client
    .prepareDelete(request.index, request.`type`, request.id)

  private def deleteDocumentResponse(r: EsDeleteResponse): DeleteDocumentResponse =
    DeleteDocumentResponse(r.getIndex(), r.getType(), r.getId(), r.getVersion(), r.isFound())

  def indexDocument[T: Writeable](client: Client, request: IndexDocumentRequest[T]): IndexDocumentResponse =
    indexDocumentResponse(indexDocumentRequest(client, request).execute().get())

  def getDocument[T: Readable](client: Client, request: GetDocumentRequest[T]) = {
    val r = client
      .prepareGet(request.index, request.`type`, request.id)
      .execute()
      .get()

    GetDocumentResponse(r.getIndex(), r.getType(), r.getId(), r.getVersion(), r.isExists(),
      if (r.isExists()) Some(implicitly[Readable[T]].fromBytes(r.getSourceAsBytes()).getOrElse(???))
      else None)

  }

  def updateDocument[T: Writeable](client: Client, request: UpdateDocumentRequest[T]) =
    updateDocumentResponse(updateDocumentRequest(client, request).execute().get())

  def deleteDocument(client: Client, request: DeleteDocumentRequest) =
    deleteDocumentResponse(deleteDocumentRequest(client, request).execute().get())

  def bulk(client: Client, request: BulkRequest) = {
    val r = request.requests.foldLeft(client.prepareBulk()) { (builder, request) =>
      request match {
        case r @ DeleteDocumentRequest(_, _, _) => builder.add(deleteDocumentRequest(client, r))
        case r @ IndexDocumentRequest(_, _, _, _, _, _, _, _, _, _, _, _, _, _) => builder.add(indexDocumentRequest(client, r)(r.writeable))
        case r @ UpdateDocumentRequest(_, _, _, _) => builder.add(updateDocumentRequest(client, r)(r.writeable))
      }
    }
      .execute()
      .get()

    val items = r.getItems().map { i =>
      if (i.isFailed()) BulkFailure(i.getIndex, i.getType, i.getId, i.getFailure.getCause)
      else i.getResponse[ActionWriteResponse] match {
        case resp: EsDeleteResponse => deleteDocumentResponse(resp)
        case resp: EsUpdateResponse => updateDocumentResponse(resp)
        case resp: EsIndexResponse => indexDocumentResponse(resp)
      }
    }

    BulkResponse(items)
  }
}
