package models

import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db._

case class Task(id: Long, label: String) {

}

object Task {
	val simple = {
		get[Long]("id") ~
			get[String]("label") map {
				case id ~ label => Task(id, label)
			}
	}
	def all: List[Task] = {
		DB.withConnection(implicit connection =>
			SQL("select * from task").as(Task.simple *))
	}
	def create(label: String) = {
		DB.withConnection(implicit connection =>
			SQL("insert into task(label) values({label})").on("label" -> label).executeUpdate)
	}
	def delete(id: Long) = {
		DB.withConnection(implicit connection =>
			SQL("delete from task where id={id}").on("id" -> id).executeUpdate)
	}
}