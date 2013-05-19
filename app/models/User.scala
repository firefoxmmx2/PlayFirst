package models

case class User(id: Long, name: String)

object User {
	def find(id: Long):List[User] = Nil
	def insert(user: User) = {}
	def delete(user: User) = {}
	def update(user: User) = {}
}