package astrac.springy.api

import astrac.springy._

// Document / Index API
case class IndexRequest[T](index: String, `type`: String, id: Option[String], document: T)
case class IndexResponse(_index: String, _type: String, _id: String, _version: Long, created: Boolean)
// Document / Get API
case class GetRequest[T](index: String, `type`: String = "_all", id: String)
case class GetResponse[T](_index: String, _type: String, _id: String, _version: Long, found: Boolean, document: Option[T])
// Document / Update API
// TODO: case class ScriptUpdateRequest[T](index: String, `type`: String, id: String, script: String, params: T)
case class DocumentUpdateRequest[T](index: String, `type`: String, id: String, document: T)
case class UpdateResponse(_index: String, _type: String, _id: String, _version: Long)
// Document / Delete API
case class DeleteRequest(index: String, `type`: String, id: String)
case class DeleteResponse(_index: String, _type: String, _id: String, _version: Long, found: Boolean)

trait Document[E <: Executor] {
  implicit def indexSuport[T: Writeable]: Executable[E, IndexRequest[T], IndexResponse]
  implicit def getSupport[T: Readable]: Executable[E, GetRequest[T], GetResponse[T]]
  implicit def documentUpdateSupport[T: Writeable]: Executable[E, DocumentUpdateRequest[T], UpdateResponse]
  implicit def deleteSupport: Executable[E, DeleteRequest, DeleteResponse]
}
