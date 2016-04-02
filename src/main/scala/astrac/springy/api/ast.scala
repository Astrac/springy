package astrac.springy
package api

import concurrent.duration.Duration

// Request AST
sealed trait IndexIOAst[T]

// Index / Delete API
// TODO: Support IndicesOptions from ES
case class DeleteIndexRequest(indexName: String) extends IndexIOAst[AcknowledgedResponse]

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
