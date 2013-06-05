package models

import org.squeryl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl._
import play.api.Play.current

class Address(val id: Long,
              val province: String,
              val city: String,
              val country: String,
              var street: Option[String],
              var road: Option[String],
              var No: Option[String]) extends KeyedEntity[Long] {
}

class User(val id: Long,
           val name: String,
           val username: String,
           val password: String,
           var email: Option[String],
           var addressId: Long) extends KeyedEntity[Long] {
  lazy val address: ManyToOne[Address] = System.addressToUsers.right(this)
}

object User {
  def apply(id: Long = 0,
            name: String = "",
            username: String,
            password: String,
            email: Option[String] = Option(""),
            addressId: Long = 0) =
    new User(id = id,
      name = name,
      username = username,
      password = password,
      email = email,
      addressId = addressId)

  def unapply(user: User) = Some(user.id,
    user.name,
    user.username,
    user.password,
    user.email,
    user.addressId)

//  def apply(id: Long = 0,
//            name: String = "",
//            username: String = "",
//            password: String = "",
//            email: Option[String] = Option(""),
//            address: Address=Address()) =   {
//    new User(id = id,
//      name = name,
//      username = username,
//      password = password,
//      email = email,
//      addressId = address.id)
//  }


  def find() = from(System.users)(user => select(user)).toList

  def insert(user: User) = {
    System.users.insert(user)
  }

  def delete(user: User) = {
    System.users.delete(user.id)
  }

  def update(user: User): Unit = {
    System.users.update(user)
  }

  def findById(id: Long): User = from(System.users)(s => where(s.id === id) select (s)).single
}

object Address {
  def apply(id: Long = 0,
            province: String="",
            city: String="",
            country: String="",
            street: Option[String] = Option(""),
            road: Option[String] = Option(""),
            No: Option[String] = Option("")) =
    new Address(id = id,
      province = province,
      city = city,
      country = country,
      street = street,
      road = road,
      No = No)

  def unapply(address: Address) = Some(address.id,
    address.province,
    address.city,
    address.country,
    address.street,
    address.road,
    address.No)

  def get(id: Long): Address = System.addresses.get(id)

  def insert(address: Address): Address = System.addresses.insert(address)

  def update(address: Address) = System.addresses.update(address)
}

object System extends Schema {
  val users = table[User]
  val addresses = table[Address]

  val addressToUsers =
    oneToManyRelation(addresses, users).via((s, c) => s.id === c.addressId)
}
