package controllers

import java.io.FileInputStream
import models.Bar
import models.Task
import play.api.data.Form
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.single
import play.api.libs.Comet
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.iteratee.Enumeratee
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.ChunkedResult
import play.api.mvc.Controller
import play.api.mvc.Cookie
import play.api.mvc.ResponseHeader
import play.api.mvc.ResponseHeader
import play.api.mvc.SimpleResult
import play.api.templates.Html
import play.api.mvc.Codec

object Application extends Controller {

  def index = Action {
    implicit request =>
      request.session.get("connected").map(user =>
        Ok("Hello" + user)).getOrElse {
        Unauthorized("Oops, you are not connected")
      }
  }

  val taskForm = Form("label" -> nonEmptyText)
  def tasks = Action {
    Ok(views.html.task(Task.all, taskForm))
  }

  def newTask = Action {
    implicit request =>
      taskForm.bindFromRequest.fold(errors => BadRequest, {
        case (label) =>
          Task.create(label)
          Redirect(routes.Application.tasks)
      })
  }

  def deleteTask(id: Long) = Action {
    Task.delete(id)
    Redirect(routes.Application.tasks)
  }

  val barForm = Form(single("name" -> nonEmptyText))

  def addBar = Action {
    implicit request =>
      barForm.bindFromRequest.fold(
        errors => BadRequest,
        {
          case (name) =>
            Bar.create(Bar(0, name))
            Redirect(routes.Application.bar)
        })
  }

  def bar = Action {
    Ok(views.html.bar(barForm))
  }

  implicit val jsonFormat = Json.writes[Bar]

  def listBar() = Action {
    val bars = Bar.findAll
    val json = Json.toJson(bars)
    Ok(json)
  }

  def chunkedResponse = Action {
    val dataIn = new FileInputStream("/tmp/(成年コミック) [胡桃屋ましみん] 肉妻通信 [08-01-01].zip")
    val dataContent: Enumerator[Array[Byte]] = Enumerator.fromStream(dataIn)

    //    ChunkedResult(
    //      header = ResponseHeader(200),
    //      chunks = dataContent)

    //    Ok.stream(dataContent)
    Ok.stream(Enumerator("kiki", "foo", "bar").andThen(Enumerator.eof))

  }

  /**
   * 彗星脚本,在客户端接到响应的时候,立即执行分块里面的<script>脚本
   */

  def comet = Action {
    val events = Enumerator(
      """<script>console.log('kiki')</script>""",
      """<script>console.log('foo')</script>""",
      """<script>console.log('bar')</script>""")
    Ok.stream(events >>> Enumerator.eof).as("html")
  }

  val toCometMessage = Enumeratee.map[String] { data =>
    Html("""<script>console.log('"""" + data + """')</script>""")
  }

  def cometMessage = Action {
    val events = Enumerator("kiki", "foo", "bar")
    Ok.stream(events >>> Enumerator.eof &> toCometMessage)
  }

  def cometMessage2 = Action {
    val events = Enumerator("kiki", "foo", "bar")
    Ok.stream(events &> Comet(callback = "console.log"))
  }

  def simpleResult = Action {
    SimpleResult(
      header = ResponseHeader(200, Map(CONTENT_TYPE -> "text/plain")),
      body = Enumerator("Hello World!"))
  }

  def easyResult = Action {
    Ok("Hello World1")
  }
  val ok = Ok("Hello World")
  val notFound = NotFound
  val pageNotFound = NotFound(<h1>Page not found</h1>)
  //	val badRequest=BadRequest(views.html.form(formWithErrors))	
  val oops = InternalServerError("Oops")
  val anyStatus = Status(488)("Strange response type")

  def redirectResult = Action {
    Redirect("/")
  }

  //	def redirectResult2 = Action {
  //		Redirect("/", status = MOVED_PERMANENTLY)
  //
  //	}

  val textResult = Ok("Hello World")
  val xmlResult = Ok(<message>Hello World</message>)

  val htmlResult = Ok(<h1>Hello World1</h1>)
  val htmlResult2 = Ok(<h1>Hello World</h1>).as(HTML)
  val htmlResult3 = Ok(<h1>Hello World!</h1>).as("html")

  def httpHeader = Action {
    Ok("Hello world!").withHeaders(
      CACHE_CONTROL -> "max-age=3600",
      ETAG -> "xx")
  }

  def httpHeader2 = Action {
    Ok("Hello World!").withHeaders(
      "author" -> "Unmi",
      "siteFlag" -> "Hongkong")
      .withHeaders("siteFlag" -> "USA")
      .withHeaders("siteFlag" -> "CN")

  }

  //cookie
  //设置cookie
  def cookie = Action {
    Ok("Hello World!").withCookies(
      Cookie("theme", "blue"))
  }
  //废弃cookie
  def discardCookie = Action {
    Ok("Hello World!").discardingCookies("theme")
  }

  implicit val myCustomCharset = Codec.javaSupported("iso-8859-1")

  def changeCharset = Action {
    Ok(<h1>Hello World!</h1>).as(HTML)
  }

  //会话
  def session_ = Action {
    request =>
      request.session.get("connected").map {
        user => Ok("Hello " + user)
      }.getOrElse(Unauthorized("Oops, you are not connected"))
  }

  def session_2 = Action {
    implicit request =>
      request.session.get("connected") map {
        user => Ok("Hello " + user)
      } getOrElse {
        Unauthorized("Oops, you are not connected")
      }
  }
  //写入session,会替换
  def saveSession = Action {
    Ok("Hello World").withSession(
      "connected" -> "firefoxmmx@gmail.com")
  }
  //写入session,添加
  def appendSession = Action {
    request =>
      val session = request.session
      Ok("Hello World").withSession(session + ("saidHello" -> "yes"))
  }
  //写入session,删除
  def removeSession = Action {
    request =>
      val session = request.session
      Ok("Hello World").withSession(session - "connected")
  }

  //创造一个全新session,可以废弃以前session
  def newSession = Action {
    Ok("Bye").withNewSession
  }
}