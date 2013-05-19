package models

import anorm._
import play.api.db._
import play.api.Play.current
import anorm.SqlParser._

case class Bar(id: Long, name: String)

object Bar {
	val simple = {
		get[Long]("id") ~
			get[String]("name") map {
				case id ~ name => Bar(id, name)
			}
	}

	def findAll(): Seq[Bar] = {
		DB.withConnection(implicit connection =>
			SQL("select * from bar").as(Bar.simple *))
	}

	def create(bar: Bar): Unit = {
		DB.withConnection(implicit connecton =>
			SQL("insert into bar(name) values({name})")
			.on('name -> bar.name).executeUpdate)
	}

	def delete(bar: Bar): Unit = {
		DB.withConnection(implicit connection =>
			SQL("delete from bar where id ={id}").on('id -> bar.id).executeUpdate)
	}

	def getById(id: Long) = {
		DB.withConnection(implicit connection => 
			SQL("select * from bar where id = {id}").on('id -> id).as(Bar.simple *))
	}
}