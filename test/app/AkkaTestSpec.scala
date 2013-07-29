package app

import org.specs2.mutable.Specification
import akka.actor._
import play._
import play.api.libs._
import play.api.libs.concurrent._
import play.api.Play.current

object AkkaTestSpec extends Specification {
  class ReceiveString extends Actor {
    def receive = {
      case x: String => {
        Logger.info("receive string: x = " + x)
      }
    }
  }
  "Akka Receive Test" should {
    "should be receive" in {
      var x: String = "1"
      val myActor = Akka.system.actorOf(Props[ReceiveString], name = "myactor")
      myActor ! "Hello"

      x must equalTo("1")
    }
  }
}
