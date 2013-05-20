package models

import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play.current
case class User(id: Long, name: String)

object User {
	def find(id: Long): List[User] = Nil
	def insert(user: User) = {
		DB.withConnection(implicit connection =>
			SQL("insert into user(name) values({name})").on('name -> user.name).executeUpdate)
	}
	def delete(user: User) = {}
	def update(user: User): Unit = {
		DB.withConnection(implicit connection =>
			SQL("update user set name={name}")
				.on('name -> user.name).executeUpdate)
	}

}