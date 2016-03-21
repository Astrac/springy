package astrac.springy.api

import astrac.springy._
import scala.concurrent.duration.Duration

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

// Common models
case class AcknowledgedResponse(acknowledged: Boolean)

// Index / Delete API
case class IndexDeleteRequest(indexName: String)
object IndexDeleteRequest {
  val all = IndexDeleteRequest("_all")
}

// Index / Get API
case class GetIndexRequest(indexName: String)
case class GetIndexResponse(exists: Boolean) // TODO: Map real response

// Document / Index API
case class IndexRequest[T](
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
)
case class IndexResponse(_index: String, _type: String, _id: String, _version: Long, created: Boolean)
// Document / Get API
case class GetRequest[T](index: String, `type`: String = "_all", id: String)
case class GetResponse[T](_index: String, _type: String, _id: String, _version: Long, found: Boolean, document: Option[T])
// Document / Update API
// TODO: case class ScriptUpdateRequest[T](index: String, `type`: String, id: String, script: String, params: T)
case class DocumentUpdateRequest[T](index: String, `type`: String, id: String, document: T)
case class UpdateResponse(_index: String, _type: String, _id: String, _version: Long)
// Document / Delete API
case class DocumentDeleteRequest(index: String, `type`: String, id: String)
case class DocumentDeleteResponse(_index: String, _type: String, _id: String, _version: Long, found: Boolean)

trait Document[E <: Executor] {
  implicit def indexSupport[T: Writeable]: Executable[E, IndexRequest[T], IndexResponse]
  implicit def getSupport[T: Readable]: Executable[E, GetRequest[T], GetResponse[T]]
  implicit def documentUpdateSupport[T: Writeable]: Executable[E, DocumentUpdateRequest[T], UpdateResponse]
  implicit def deleteSupport: Executable[E, DocumentDeleteRequest, DocumentDeleteResponse]
}

// Search API
sealed trait Query
object Query {
  case class Term[T](field: String, value: T) extends Query
  case object MatchAll extends Query
}

case class SearchRequest[T](index: String, `type`: String, query: Query)
case class ShardInfo(total: Int, successful: Int, failed: Int)
case class SearchHit[T](_index: String, _type: String, _id: String, _source: T)
case class SearchHits[T](total: Long, hits: List[SearchHit[T]])
case class SearchResponse[T](_shard: ShardInfo, hits: SearchHits[T])

trait Search[E <: Executor] {
  implicit def searchSupport[T: Readable]: Executable[E, SearchRequest[T], SearchResponse[T]]
}
