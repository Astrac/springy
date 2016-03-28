package astrac.springy
package interpreters
package javaapi

import api._
import org.elasticsearch.action.WriteConsistencyLevel
import org.elasticsearch.client.Client
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.action.index.{IndexRequest => EsIndexRequest}
import org.elasticsearch.index.{VersionType => EsVersionType}

trait DocumentSupport {
  def indexDocument[T: Writeable](client: Client, request: IndexDocumentRequest[T]): IndexDocumentResponse = {
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

    val resp = ex.execute().get()
    IndexDocumentResponse(resp.getIndex(), resp.getType(), resp.getId(), resp.getVersion(), resp.isCreated())
  }

  def getDocument[T: Readable](client: Client, request: GetDocumentRequest[T]) = {
    val r = client
      .prepareGet(request.index, request.`type`, request.id)
      .execute()
      .get()

    GetDocumentResponse(r.getIndex(), r.getType(), r.getId(), r.getVersion(), r.isExists(),
      if (r.isExists()) Some(implicitly[Readable[T]].fromBytes(r.getSourceAsBytes()).getOrElse(???))
      else None)

  }

  def updateDocument[T: Writeable](client: Client, request: UpdateDocumentRequest[T]) = {
    val r = client
      .prepareUpdate(request.index, request.`type`, request.id)
      .setDoc(implicitly[Writeable[T]].toBytes(request.document))
      .execute()
      .get()

    UpdateDocumentResponse(r.getIndex(), r.getType(), r.getId(), r.getVersion())
  }

  def deleteDocument(client: Client, request: DeleteDocumentRequest) = {
    val r = client
      .prepareDelete(request.index, request.`type`, request.id)
      .execute()
      .get()

    DeleteDocumentResponse(r.getIndex(), r.getType(), r.getId(), r.getVersion(), r.isFound())
  }
}
