package astrac.springy

case class Book(isbn: String, title: String, author: String)

object Fixtures {
  val sirensOfTitan = Book("0000000000000", "The Sirens Of Titan", "Kurt Vonnegut")
  val protocolsOfTralfamadore = Book("1111111111111", "The Protocols of the Elders of Tralfamadore", "Kilgore Trout")
  val gospelFromOuterSpace = Book("2222222222222", "The Gospel From Outer Space", "Kilgore Trout")
  val slaughterhousFive = Book("3333333333333", "Slaughterhouse Five", "Kurt Vonnegut")
}
