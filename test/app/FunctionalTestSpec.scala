package app

import org.specs2.mutable.Specification
import play.api.test._
import play.api.mvc._
import play.api.libs.ws.WS

/**
 * 模板,控制器,路由器测试
 * @author hooxin
 *
 */
object FunctionalTestSpec extends Specification {
  "temple / controller / router test" should {
    "render index templete" in new WithServer {
      val html = views.html.index
      //      contentType(html) must equalTo("text/html")
      //      contentAsString(html) must contain("Hello Coco")
    }

    "respond to the index Action" in {
      val result = controllers.Application.index()(FakeRequest())
      //      status(result) must equalTo(OK)
      //      contentType(result) must beSome("text/html")
      //      charset(result) must beSome("utf-8")
      //      contentAsString(result) must contain("Hello Bob")

    }

    "respond to the index Action by router" in {
      //      val Some(result) = route(FakeRequest(GET,"/Bob"))
      //      
      //      status(result) must equalTo(OK)
      //      contentType(result) must beSome("text/html")
      //      charset(result) must beSome("utf-8")
      //      contentAsString(result) must contain("hello world")

    }

    "run in a server" in new WithServer {
      //      await(WS.url("http://localhost:"+port).get).status must equalTo(OK)
    }

    "run in a browser" in new WithBrowser {
      browser.goTo("/")
      //      browser.$("#title").getTexts().get(0) must equalTo("ffmmx")
      //      browser.$("a").click()
      //      browser.url must equalTo("/")
      browser.$("pre").getTexts().get(0) must equalTo("Oops, you are not connected")

    }

  }
}