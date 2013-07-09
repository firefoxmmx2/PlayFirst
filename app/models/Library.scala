package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl._

case class Author(id: Long = 0l,
                  firstName: String = "",
                  lastName: String = "",
                  email: Option[String] = Some("")) extends KeyedEntity[Long] {
}
object Author {
  def findAll() = from(Library.authors)(a => select(a)).toList
  def insert(author:Author):Author = Library.authors.insert(author)
  def update(author:Author):Unit = Library.authors.update(author)
  def delete(author:Author):Unit = Library.authors.delete(author.id)
}
case class Book(id: Long = 0l,
                title: String = "",
                author: Author,
                coAuthor: Option[Author] = Some(Author())) extends KeyedEntity[Long] {

}

object Book {

  def findAll() = from(Library.books)(b => select(b)).toList
  def insert(book: Book) = Library.books.insert(book)
  def update(book: Book) = Library.books.update(book)
  def delete(book: Book) = Library.books.deleteWhere(b => b.id === book.id)
}

object Library extends Schema {
  val authors = table[Author]("AUTHORS")
  val books = table[Book]
  override def printDdl: Unit = printDdl(println(_))
}
