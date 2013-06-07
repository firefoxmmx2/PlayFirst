package app

import org.specs2.mutable.Specification
import play.api.test.WithApplication
import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._
import play.api.test.FakeApplication

case class Computer(name: String = "")
object Computer {
  def findByName(name: String): Computer = from(ComputerStore.computers)(s =>
    where(s.name === name) select (s)).single
}
object ComputerStore extends Schema {
  val computers = table[Computer]("t_computer")
}
//fake application spec
object FakeApplicationSpec extends Specification {
  "Computer model" should {
    "be retrieved by id" in new WithApplication(FakeApplication(additionConfiguration = inMemoryDatabase())) {
      val Some(macintosh) = Computer.findByName("Macintosh")
      macintosh.name must equalTo("Macintosh")
    }
  }
}