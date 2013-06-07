package app

import org.specs2.mutable.Specification
import play.api.mvc.Controller

trait ExampleController {
  def index() = {
    println("Hello World")
  }
}
object ExampleControllerSpec extends Specification {
  class TestController() extends Controller with ExampleController
  "Example Page#index" should {
    "should be valid" in {
      val controller = new TestController()
      val result = controller.index
      result must not beNull
    }
  }
}