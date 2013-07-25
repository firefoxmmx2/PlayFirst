package models

import anorm._
import play.api._
import play.api.db._
import anorm.SqlParser._
import play.api.Play.current

case class Druid(id: Long = 0l, name: String)

object Druid {

	val allListResultSet = {
		get[Long]("id") ~
			get[String]("name") map {
				case id ~ name => Druid(id, name)
			}
	}
	def all() = {
		DB.withConnection({ implicit con =>
			SQL("select * from t_druid").as(allListResultSet *)
		})
	}

	def create(druid: Druid) = DB.withConnection({
		implicit con =>
			SQL("""
				inset into t_druid (name) values({name})
			""").on("name" -> druid.name).executeInsert()
	})

	def update(druid: Druid) = DB.withConnection({
		implicit con =>
			SQL("""
				update t_druid set name = {name} 
				where  id = {id}
		""").on("name" -> druid.name, "id" -> druid.id).executeUpdate

	})

	def delete(druid: Druid) = DB.withConnection({
		implicit connection =>
			SQL("""
					delete from t_druid where id = {id}
			""").on("id" -> druid.id).executeUpdate
	})
}