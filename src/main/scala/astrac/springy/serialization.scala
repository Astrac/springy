package astrac.springy

import spray.json.{ JsonReader, JsonWriter, JsonParser }

trait Writeable[T] {
  def toBytes(doc: T): Array[Byte]
}

trait Readable[T] {
  def fromBytes(bytes: Array[Byte]): T
}

object SprayJsonSerialization {
  implicit def jsonWriterIsWriteable[T: JsonWriter] = new Writeable[T] { def toBytes(doc: T) = implicitly[JsonWriter[T]].write(doc).compactPrint.getBytes() }
  implicit def jsonReaderIsReadable[T: JsonReader] = new Readable[T] { def fromBytes(bytes: Array[Byte]) = implicitly[JsonReader[T]].read(JsonParser(bytes)) }
}
