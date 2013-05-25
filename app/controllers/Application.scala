package controllers

import java.io.FileInputStream
import models._
import play.api.data._
import play.api.data.Forms._
import play.api.libs._
import play.api.libs.functional.syntax._
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.mvc._
import play.api.templates.Html
import java.io.File
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

  def listBar() = Action {
    val bars = Bar.findAll
    Ok("Hello World")
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

  //  网络套接字,一直打开
  def webSocket = WebSocket.using[String] {
    request =>
      // Log events to the console
      val in = Iteratee.foreach[String](println).mapDone {
        _ => println("Disconnected")
      }

      //      val Send a single "Hello!" message
      val out = Enumerator("Hello")
      (in, out)
  }
  //网络套接字,在发送消息后自动关闭 >>> Enumerator.eof
  def webSocket2 = WebSocket.using[String]({
    request =>
      val in = Iteratee.consume[String]()
      val out = Enumerator("Hello!") >>> Enumerator.eof
      (in, out)
  })

  //Json处理
  val jNumber = Json.toJson(4)
  val jArray = Json.toJson(Seq(1, 2, 3, 4))
  //  val jArray2=Json.toJson(Seq(1,"Bob",3,4)) 这是错的, Seq[Int]不能带String
  val jArray3 = Json.toJson(Seq(Json.toJson(1), Json.toJson("Bob"), Json.toJson(3), Json.toJson(4)))
  val jObject = Json.toJson(
    Map(
      "users" -> Seq(
        Json.toJson(
          Map("name" -> Json.toJson("Bob"),
            "age" -> Json.toJson(31),
            "email" -> Json.toJson("bob@gmail.com"))),
        Json.toJson(
          Map(
            "name" -> Json.toJson("kiki"),
            "age" -> Json.toJson(25),
            "email" -> JsNull)))))
  //Json 处理请求
  def sayHello = Action {
    request =>
      request.body.asJson.map({ json =>
        (json \ "name").asOpt[String].map(name => Ok("Hello " + name)).getOrElse(BadRequest("Missing parameter [name]"))
      }).getOrElse(BadRequest("Expecting Json data"))
  }
  //更好的办法
  def sayHello2 = Action(parse.json) { request =>
    (request.body \ "name").asOpt[String].map {
      name => Ok("Hello " + name)
    }.getOrElse({
      BadRequest("Missing parameter [name]")
    })
  }

  def sayHelloRes = Action(parse.json) { request =>
    (request.body \ "name").asOpt[String].map {
      name =>
        Ok(Json.toJson(Map("status" -> " Ko", "message" -> ("Hello " + name))))
    }.getOrElse { BadRequest(Json.toJson(Map("st tus" -> " O", "message" -> "Missing parameter [name]"))) }
  }

  //  body 解析器
  def toBodyParser = Action {
    //    Ok(views.html.bodyParser())
    Ok("")
  }

  //  文本解析器,严格检查CONTENT-TYPE
  def textParser = Action(parse.text) {
    request =>
      Ok("Got: " + request.body)
  }
  //文本解析器,不严格检查
  def tolerantTextParser = Action(parse.tolerantText) {
    request =>
      Ok("Got: " + request.body)
  }
  //文件解析器
  def fileParser = Action(parse.file(new File("/tmp/.upload"))) { request =>
    Ok("Saved the request content to" + request.body);
  }
  val storeInfoUserFile = parse.using[File] {
    request =>
      request.session.get("username").map({
        user => parse.file(new File("/tmp/" + user + ".upload"))
      }).getOrElse { parse.file(new File("/tmp/test.upload")) }
  }
  //混合解析器
  def mixedParser = Action(storeInfoUserFile) {
    request =>
      Ok("Saved the request content to" + request.body);
  }

  def jsonParser = TODO

  //  长度限制的解析器
  def lengthLimitParser = Action(parse.maxLength(10, parser = parse.tolerantText)) {
    request =>
      Ok("Saved the request content to " + request.body)
  }

  case class SearchResult[T](elements: List[T], page: Int, pageSize: Int, total: Int)
  import play.api.libs.functional.syntax._
  implicit val searchResultWrite: Writes[SearchResult[String]] = (
    (__ \ "elements").write[List[String]] and
    (__ \ "page").write[Int] and
    (__ \ "pageSize").write[Int] and
    (__ \ "total").write[Int])(unlift(SearchResult.unapply[String]))

  def jsonSearchResult = Action {
    val res = SearchResult[String](
      elements = List[String]("hello"),
      page = 1,
      pageSize = 5,
      total = 10)
    Ok(toJson(res))
  }
  def jsonResult = Action {
    val res = SearchResult[String](
      elements = List[String]("hello"),
      page = 1,
      pageSize = 5,
      total = 10)
    import com.codahale.jerkson.Json
    Ok(Json.generate(res))
  }
  //  implicit val searchResultFormat:Reads[SearchResult[String]]=
  //  object SearchResult {
  //    implicit def searchResultReads[T](implicit fmt: Reads[T]): Reads[SearchResult[T]] = new Reads[SearchResult[T]] {
  //      def reads(json: JsValue): SearchResult[T] = new SearchResult[T](
  //        (json \ "elements") match {
  //          case JsArray(ts) => ts.map(t => fromJson(t)(fmt))
  //          case _ => throw new RuntimeException("Elements MUST be a list")
  //        },
  //        (json \ "page").as[Int],
  //        (json \ "pageSize").as[Int],
  //        (json \ "total").as[Int]
  //      )
  //    }
  //
  //    implicit def searchResultWrites[T](implicit fmt: Writes[T]): Writes[SearchResult[T]] = new Writes[SearchResult[T]] {
  //      def writes(ts: SearchResult[T]) = JsObject(Seq(
  //        "page" -> JsNumber(ts.page),
  //        "pageSize" -> JsNumber(ts.pageSize),
  //        "total" -> JsNumber(ts.total),
  //        "elements" -> JsArray(ts.elements.map(toJson(_)))
  //      ))
  //    }
  //  }

  //  处理xml请求
  def xmlRequest = Action {
    request =>
      request.body.asXml.map({
        xml =>
          (xml \\ "name" headOption).map(_.text).map({ name =>
            Ok("Hello " + name)
          }).getOrElse(BadRequest("Missing parameter [name]"))
      }).getOrElse(BadRequest("Excepting Xml data"))
  }
  //  更简单的方法
  def xmlReqBetter = Action(parse.xml) { request =>
    (request.body \\ "name" headOption).map(_.text).map {
      name => Ok("Hello " + name)
    }.getOrElse {
      BadRequest("Missing parameter [name]")
    }

  }
  //  xml响应
  def xmlResponse = Action(parse.xml) {
    request =>
      (request.body \\ "name" headOption).map(_.text).map({
        name =>
          Ok(<message status="OK">Hello { name }</message>)
      }).getOrElse({
        BadRequest(<message status="KO">Missing parameter [name]</message>)
      })
  }
  //  文件上传页面
  def fileUploadHtml = Action {
    Ok(views.html.fileRequest())
  }
  //  文件上传请求
  def fileRequest = Action(parse.multipartFormData) {
    reqeust =>
      reqeust.body.file("picture").map {
        picture =>
          import java.io.File
          val filename = picture.filename
          val contentType = picture.contentType
          picture.ref.moveTo(new File("/tmp/picture"))
          Ok("File uploaded")
      }.getOrElse {
        Redirect(routes.Application.index).flashing("error" -> "Missing file")
      }
  }

  //  ajax异步上传
  def ajaxUpload = Action(parse.temporaryFile) {
    request =>
      request.body.moveTo(new File("/tmp/picture"), true)
      Ok("File Upload")
  }
  //ajax上传页面
  def ajaxUploadHtml = Action {
    Ok(views.html.ajaxUploadHtml())
  }
}