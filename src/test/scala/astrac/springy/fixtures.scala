package astrac.springy

case class Book(isbn: String, title: String, author: String)

object Fixtures {
  val sirensOfTitan = Book("0000000000000", "The Sirens Of Titan", "Kurt Vonnegut")
  val protocolsOfTralfamadore = Book("1111111111111", "The Protocols of the Elders of Tralfamadore", "Kilgore Trout")
}
