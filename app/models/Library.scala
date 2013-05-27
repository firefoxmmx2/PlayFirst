package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl._
import org.squeryl.annotations._
import java.util.Date
import java.sql.Timestamp

class Author(val id: Long = 0l,
             val firstName: String = "",
             val lastName: String = "",
             val email: Option[String] = Some("")) {
  def findBooksOfThisAuthor=from(Library.books)(b=>where(b.authorId===id) select(b))
}
object Author {
  def findAll=from(Library.authors)(a=>select(a))
}
class Book(val id: Long = 0l,
           var title: String = "",
           @Column("AUTHOR_ID") var authorId: Long = 0l,
           var coAuthorId: Option[Long] = Some(0l)) {
  
  def author=from(Library.authors)(s=>where(s.id===authorId) select(s))
  def coAuthor=from(Library.authors)(a=>where(a.id===coAuthorId) select(a))
}
object Book {
  def findAll=from(Library.books)(b=>select(b))
}
object Library extends Schema {
  val authors = table[Author]("AUTHORS")
  val books = table[Book]


  on(authors)(s => declare(s.email is (unique, indexed("idxEmailAddresses")),
    s.firstName is (indexed),
    s.lastName is (indexed, dbType("varchar(255)")),
    columns(s.firstName, s.lastName) are (indexed)))

  on(books)(b => declare(columns(b.authorId, b.coAuthorId) are (indexed)))

  override def printDdl: Unit = printDdl(println(_))

}