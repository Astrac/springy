package astrac.springy

import cats.data.Xor
import io.circe.{ Decoder, Encoder, Error }
import io.circe.parser.decode

trait Writeable[T] {
  def toBytes(doc: T): Array[Byte]
}

trait Readable[T] {
  def fromBytes(bytes: Array[Byte]): Error Xor T
}

object CirceSerialization {
  implicit def jsonWriterIsWriteable[T: Encoder] = new Writeable[T] {
    def toBytes(doc: T) = implicitly[Encoder[T]].apply(doc).noSpaces.getBytes
  }

  implicit def jsonReaderIsReadable[T: Decoder] = new Readable[T] {
    def fromBytes(bytes: Array[Byte]) = decode[T](new String(bytes))
  }
}
