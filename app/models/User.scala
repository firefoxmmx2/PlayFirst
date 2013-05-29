package models

import play.api.db._
import play.api.Play.current
import org.squeryl.PrimitiveTypeMode._
import org.squeryl._
import org.squeryl.annotations._
import org.squeryl.dsl.ManyToOne
import org.squeryl.dsl.OneToMany

case class Address(id: Long,
                   province: String,
                   city: String,
                   country: String,
                   street: String,
                   road: String,
                   No: String) extends KeyedEntity[Long] {
  val users:OneToMany[User] = System.addressToUsers.left(this)
}
case class User(id: Long,
                name: String,
                username: String,
                password: String,
                email: String,
                addressId: Long) extends KeyedEntity[Long] {
  lazy val address: ManyToOne[Address] = System.addressToUsers.right(this)
}

object User {
  def find(): List[User] = {
    from(System.users)(user => select(user)).toList
  }
  def insert(user: User) = {
    System.users.insert(user)
  }
  def delete(user: User) = {
    System.users.delete(user.id)
  }
  def update(user: User): Unit = {
    System.users.update(user)
  }
  def findById(id: Long): User = {
    from(System.users)(s => where(s.id === id) select (s)).single
  }
}

object Address {
  def get(id:Long):Address = System.addresses.get(id)
  def insert(address:Address):Address = System.addresses.insert(address)
  def update(address:Address) = System.addresses.update(address)
}

object System extends Schema {
  val users = table[User]
  val addresses = table[Address]

  val addressToUsers =
    oneToManyRelation(addresses, users).via((s, c) => s.id === c.addressId)
}