package app

import org.specs2.mutable.Specification
import play.api.test.WithApplication
import play.api.test.FakeApplication
import org.squeryl._
import org.squeryl.dsl._
import play.api.test._

//fake application spec
object FakeApplicationSpec extends Specification {
  "Computer model" should {
    "be retrieved by id" in new WithApplication() {
      import org.squeryl.PrimitiveTypeMode._
      inTransaction {
        val c = Computer(name = "11")
        Computer.insert(c)
        val macintosh = Computer.findById(1)
        macintosh.id must equalTo(1)
      }

    }
    "be retrieved by name" in new WithApplication {
      import org.squeryl.PrimitiveTypeMode._
      inTransaction {
        val c = Computer(name = "x")
        Computer.insert(c)
        val macintosh = Computer.findByName("x")
        macintosh.name must equalTo("x")
      }

    }
  }
}

case class Computer(id: Long = 0, name: String = "") extends KeyedEntity[Long]
object Computer {
  import org.squeryl.PrimitiveTypeMode._
  def findByName(name: String): Computer = from(ComputerStore.computers)(s =>
    where(s.name === name) select (s)).single
  def insert(computer: Computer): Computer = ComputerStore.computers.insert(computer)
  def update(computer: Computer) = ComputerStore.computers.update(computer)
  def delete(computer: Computer) = ComputerStore.computers.delete(computer.id)
  def findById(id: Long): Computer = ComputerStore.computers.get(id)
}
object ComputerStore extends Schema {
  val computers = table[Computer]("t_computer")
}