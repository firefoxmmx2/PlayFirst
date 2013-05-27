package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl._
import org.squeryl.annotations._

class Author(val id: Long = 0l,
             val firstName: String = "",
             val lastName: String = "",
             val email: Option[String] = Some("")) extends KeyedEntity[Long] {
  def findBooksOfThisAuthor = from(Library.books)(b => where(b.authorId === id) select (b)).toList
}
object Author {
  def apply() = new Author
  def apply(id: Long = 0l, firstName: String = "", lastName: String = "", email: Option[String] = Some("")) = new Author(id, firstName, lastName, email)
  def findAll = from(Library.authors)(a => select(a))
}
class Book(val id: Long = 0l,
           var title: String = "",
           @Column("AUTHOR_ID") var authorId:Long=0l,
           var coAuthorId: Option[Long] = Some(0l)) extends KeyedEntity[Long] {

  def author = from(Library.authors)(s => where(s.id === authorId) select (s)).single
  def coAuthor = from(Library.authors)(a => where(a.id === coAuthorId) select (a)).single
}
object Book {
  def apply(id: Long = 0l, title: String = "", author: Author = Author(), coAuthorId: Option[Long] = Some(0l)) = new Book(id = id, title = title, authorId = author.id, coAuthorId = coAuthorId)
  def apply(title: String, author: Author, coAuthorId: Option[Author]) = new Book()
  def unapply(book: Book) = Some(book.id, book.title, book.authorId, book.coAuthorId)
  def findAll(): List[Book] = from(Library.books)(b => select(b)).toList
  def insert(book: Book) = Library.books.insert(book)
  //  def update(book:Book) = Library.books.update(s=>)
  def delete(book: Book) = Library.books.deleteWhere(b => b.id === book.id)
}
object Library extends Schema {
  val authors = table[Author]("AUTHORS")
  val books = table[Book]

  on(authors)(s => declare(
    s.email is (unique, indexed("idxEmailAddresses")),
    s.firstName is (indexed),
    s.lastName is (indexed, dbType("varchar(255)")),
    columns(s.firstName, s.lastName) are (indexed)
  ))

  on(books)(b => declare(
    columns(b.authorId, b.coAuthorId) are (indexed)
  ))

  override def printDdl: Unit = printDdl(println(_))

}