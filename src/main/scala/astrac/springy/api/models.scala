package astrac.springy
package api

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

// Bulk model
sealed trait BulkItemResponse
case class BulkFailure(index: String, `type`: String , id: String, t: Throwable) extends BulkItemResponse

// Response model
case class AcknowledgedResponse(acknowledged: Boolean)
case class GetIndexResponse(exists: Boolean) // TODO: Map real response
case class IndexDocumentResponse(_index: String, _type: String, _id: String, _version: Long, created: Boolean) extends BulkItemResponse
case class GetDocumentResponse[T](_index: String, _type: String, _id: String, _version: Long, found: Boolean, document: Option[T])
case class DeleteDocumentResponse(_index: String, _type: String, _id: String, _version: Long, found: Boolean) extends BulkItemResponse
case class UpdateDocumentResponse(_index: String, _type: String, _id: String, _version: Long) extends BulkItemResponse
// TODO: case class ScriptUpdateRequest[T](index: String, `type`: String, id: String, script: String, params: T)
case class BulkResponse(items: Seq[BulkItemResponse])
case class SearchResponse[T](_shard: ShardInfo, hits: SearchHits[T])
