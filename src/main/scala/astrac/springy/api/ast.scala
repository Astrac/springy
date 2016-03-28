package astrac.springy
package api

import cats.free.Free
import concurrent.duration.Duration

// Request AST
sealed trait IndexIOAst[T]

// Index / Delete API
case class DeleteIndexRequest(indexName: String) extends IndexIOAst[AcknowledgedResponse]
object DeleteIndexRequest {
  val all = DeleteIndexRequest("_all")
}

// Index / Get API
case class GetIndexRequest(indexName: String) extends IndexIOAst[GetIndexResponse]

// Document / Index API
case class IndexDocumentRequest[T](
  index: String,
  `type`: String,
  id: Option[String],
  document: T,
  ttl: Option[Long] = None,
  opType: Option[OpType] = None,
  parent: Option[String] = None,
  refresh: Option[Boolean] = None,
  routing: Option[String] = None,
  timestamp: Option[String] = None,
  version: Option[Long] = None,
  versionType: Option[VersionType] = None,
  consistencyLevel: Option[ConsistencyLevel] = None,
  timeout: Option[Duration] = None
)(implicit val writeable: Writeable[T])
  extends IndexIOAst[IndexDocumentResponse]
  with BulkableRequest

// Document / Get API
case class GetDocumentRequest[T](index: String, `type`: String = "_all", id: String)(implicit val readable: Readable[T])
  extends IndexIOAst[GetDocumentResponse[T]]

// Document / Update API
case class UpdateDocumentRequest[T](index: String, `type`: String, id: String, document: T)(implicit val writeable: Writeable[T])
  extends IndexIOAst[UpdateDocumentResponse]
  with BulkableRequest

// Document / Delete API
case class DeleteDocumentRequest(index: String, `type`: String, id: String)
  extends IndexIOAst[DeleteDocumentResponse]
  with BulkableRequest

// Document / Bulk API
sealed trait BulkableRequest
case class BulkRequest(requests: Seq[BulkableRequest]) extends IndexIOAst[BulkResponse]

object BulkRequest {
  def indexDocument[T: Writeable](
    index: String,
    `type`: String,
    id: Option[String],
    document: T,
    ttl: Option[Long] = None,
    opType: Option[OpType] = None,
    parent: Option[String] = None,
    refresh: Option[Boolean] = None,
    routing: Option[String] = None,
    timestamp: Option[String] = None,
    version: Option[Long] = None,
    versionType: Option[VersionType] = None,
    consistencyLevel: Option[ConsistencyLevel] = None,
    timeout: Option[Duration] = None
  ): BulkableRequest =
    IndexDocumentRequest(index, `type`, id, document, ttl, opType, parent,
      refresh, routing, timestamp, version, versionType, consistencyLevel, timeout)

  def updateDocument[T: Writeable](index: String, `type`: String, id: String, document: T): BulkableRequest =
    UpdateDocumentRequest(index, `type`, id, document)

  def deleteDocument(index: String, `type`: String, id: String): BulkableRequest  =
    DeleteDocumentRequest(index, `type`, id)
}

// Search API
case class SearchRequest[T](index: String, `type`: String, query: Query)(implicit val readable: Readable[T]) extends IndexIOAst[SearchResponse[T]]

// Request monad
object IndexIO {
  type IndexIO[T] = astrac.springy.api.IndexIO[T]

  private def lift[R, T <: IndexIOAst[R]](v: T) = Free.liftF[IndexIOAst, R](v)

  def getIndex(indexName: String): IndexIO[GetIndexResponse] = lift(GetIndexRequest(indexName))

  def deleteIndex(indexName: String): IndexIO[AcknowledgedResponse] = lift(DeleteIndexRequest(indexName))

  def indexDocument[T: Writeable](
    index: String,
    `type`: String,
    id: Option[String],
    document: T,
    ttl: Option[Long] = None,
    opType: Option[OpType] = None,
    parent: Option[String] = None,
    refresh: Option[Boolean] = None,
    routing: Option[String] = None,
    timestamp: Option[String] = None,
    version: Option[Long] = None,
    versionType: Option[VersionType] = None,
    consistencyLevel: Option[ConsistencyLevel] = None,
    timeout: Option[Duration] = None
  ): IndexIO[IndexDocumentResponse] = lift(IndexDocumentRequest(index, `type`, id, document, ttl, opType, parent,
    refresh, routing, timestamp, version, versionType, consistencyLevel, timeout))

  def getDocument[T: Readable](index: String, `type`: String = "_all", id: String): IndexIO[GetDocumentResponse[T]] =
    lift(GetDocumentRequest[T](index, `type`, id))

  def updateDocument[T: Writeable](index: String, `type`: String, id: String, document: T): IndexIO[UpdateDocumentResponse] =
    lift(UpdateDocumentRequest(index, `type`, id, document))

  def deleteDocument(index: String, `type`: String, id: String): IndexIO[DeleteDocumentResponse] =
    lift(DeleteDocumentRequest(index, `type`, id))

  def bulk(requests: Seq[BulkableRequest]): IndexIO[BulkResponse] =
    lift(BulkRequest(requests))

  def search[T: Readable](index: String, `type`: String, query: Query): IndexIO[SearchResponse[T]] =
    lift(SearchRequest[T](index, `type`, query))
}
