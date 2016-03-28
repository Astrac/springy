package astrac.springy.api

import astrac.springy._
import cats.free.Free
import scala.concurrent.duration.Duration

// Index model
sealed trait VersionType
object VersionType {
  case object Internal extends VersionType
  case object External extends VersionType
  case object ExternalGte extends VersionType
  case object Force extends VersionType
}

sealed trait ConsistencyLevel
object ConsistencyLevel {
  case object One extends ConsistencyLevel
  case object Quorum extends ConsistencyLevel
  case object All extends ConsistencyLevel
}

sealed trait OpType
object OpType {
  case object Index extends OpType
  case object Create extends OpType
}

// Search model
sealed trait Query
object Query {
  case class Term[T](field: String, value: T) extends Query
  case object MatchAll extends Query
  // TODO: All the other queries
}

case class ShardInfo(total: Int, successful: Int, failed: Int)
case class SearchHit[T](_index: String, _type: String, _id: String, _source: T)
case class SearchHits[T](total: Long, hits: List[SearchHit[T]])

// Responses model
case class AcknowledgedResponse(acknowledged: Boolean)
case class GetIndexResponse(exists: Boolean) // TODO: Map real response
case class IndexDocumentResponse(_index: String, _type: String, _id: String, _version: Long, created: Boolean)
case class GetDocumentResponse[T](_index: String, _type: String, _id: String, _version: Long, found: Boolean, document: Option[T])
case class DeleteDocumentResponse(_index: String, _type: String, _id: String, _version: Long, found: Boolean)
case class UpdateDocumentResponse(_index: String, _type: String, _id: String, _version: Long)
// TODO: case class ScriptUpdateRequest[T](index: String, `type`: String, id: String, script: String, params: T)
case class SearchResponse[T](_shard: ShardInfo, hits: SearchHits[T])

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
)(implicit val writeable: Writeable[T]) extends IndexIOAst[IndexDocumentResponse]

// Document / Get API
case class GetDocumentRequest[T](index: String, `type`: String = "_all", id: String)(implicit val readable: Readable[T]) extends IndexIOAst[GetDocumentResponse[T]]

// Document / Update API
case class UpdateDocumentRequest[T](index: String, `type`: String, id: String, document: T)(implicit val writeable: Writeable[T]) extends IndexIOAst[UpdateDocumentResponse]

// Document / Delete API
case class DeleteDocumentRequest(index: String, `type`: String, id: String) extends IndexIOAst[DeleteDocumentResponse]

// Search API
case class SearchRequest[T](index: String, `type`: String, query: Query)(implicit val readable: Readable[T]) extends IndexIOAst[SearchResponse[T]]

// Request monad
object IndexIO {
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

  def search[T: Readable](index: String, `type`: String, query: Query): IndexIO[SearchResponse[T]] =
    lift(SearchRequest[T](index, `type`, query))
}
