package models
import play.api.db._
import play.api.Play.current
import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ ExtendedTable => Table }
import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.session._

object Bar2 extends Table[(Long, String, String, Array[Byte])]("bar") {

  lazy val database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("id", O PrimaryKey, O AutoInc)
  def name = column[String]("name", O NotNull)
  def summary = column[String]("summary")
  def image = column[Array[Byte]]("image")
  def * = id ~ name ~ summary ~ image

  def find = database.withSession({
    implicit db: Session =>
      (for (t <- this) yield t.id ~ t.name).list
  })
}