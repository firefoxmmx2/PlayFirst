package controllers

import java.io._
import models._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs._
import play.api.libs.functional.syntax._
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.mvc._
import play.api.templates.Html
import play.Logger
import play.api.cache._
import scala.concurrent.Future
import play.api.libs.ws._
import play.api.libs.openid.OpenID
import play.api.libs.concurrent.Redeemed

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

  val barForm = Form(
    mapping(
      "id" -> ignored(0l),
      "name" -> text,
      "summary" -> text)(
        (id, name, summeray) => Bar(id, name, summeray))((bar: Bar) => Some(bar.id, bar.name, bar.summary)))

  def addBar = Action {
    implicit request =>
      barForm.bindFromRequest.fold(
        errors => BadRequest,
        //简洁写法
        {
          case (bar) => Bar.create(bar); Redirect(routes.Application.bar)
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
    Html("""<script>console.log('""" + data + """')</script>""")
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
    Ok("Hello World!").discardingCookies(DiscardingCookie("theme"))
  }

  implicit val myCustomCharset = Codec.javaSupported("utf-8")

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

  //使用流API数据
  def barNameList = Action {
    import com.codahale.jerkson.Json
    Ok(Json.generate(Bar.findBarNameList))
  }

  //取第一个bar记录
  def firstBar = Action {
    import com.codahale.jerkson.Json
    Ok(Json.generate(Bar.findFirstBar))
  }
  //取bar记录数
  def barCount = Action {
    import com.codahale.jerkson.Json
    Ok(Json.generate(Bar.countBar))
  }
  //使用正则匹配ANORM
  def barPatternMatching = Action {
    import com.codahale.jerkson.Json
    Ok(Json.generate(Bar.findBarByPattern))
  }

  def barSpecialDataTypeClob = Action {
    import com.codahale.jerkson.Json
    Ok(Json.generate(Bar.findBarByClob))
  }

  def barSpecialDataTypeBinary = Action {
    import com.codahale.jerkson.Json
    Ok(Json.generate(Bar.findBarByBinary))
  }

  def barScalaQuery = Action {
    import com.codahale.jerkson.Json
    Ok(Json.generate(Bar2.find))
  }
  import com.codahale.jerkson.Json

  val bookForm = Form(
    mapping(
      "title" -> nonEmptyText,
      "author" -> mapping(
        "lastName" -> nonEmptyText,
        "firstName" -> nonEmptyText,
        "email" -> optional(text))(
          (lastName, firstName, email) => Author(firstName = firstName, lastName = lastName, email = email))(
            (author) => Some(author.firstName, author.lastName, author.email)))((title, author) => Book(title = title, author = author))((book) => Some(book.title, book.author)))
  //事物
  import org.squeryl.PrimitiveTypeMode._
  //创建一个带事物的Action Wrapper
  object TxAction {
    def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result): Action[A] = {
      Action(bodyParser) {
        implicit request =>
          inTransaction {
            block(request)
          }
      }

    }

    def apply(block: Request[AnyContent] => Result): Action[AnyContent] = apply(BodyParsers.parse.anyContent)(block)
    def apply(block: => Result): Action[AnyContent] = apply(_ => block)
  }
  //添加书籍
  def addBook = TxAction {
    implicit request =>
      bookForm.bindFromRequest.fold(
        error => BadRequest,
        {
          case (book: Book) =>
            val author = Author.insert(book.author)
            book.copy(author = author)
            Book.insert(book)
            Redirect(routes.Application.booklist)
        })
  }

  def toBooks = Action {
    Ok(views.html.books(bookForm))
  }

  def booklist = TxAction {
    val lst = Book.findAll()
    Logger.info("Book.findAll() == " + lst)
    Ok(Json.generate(lst)).as(JSON)
  }
  import play.api.Play.current
  //缓存数据对象
  def barNameListCache = Action {
    Cache.set("item.key", Bar.findBarNameList)
    val barList: Option[List[Bar]] = Cache.getAs[List[Bar]]("item.key")
    Ok(Json.generate(barList))
  }
  //缓存数据对象使用默认值
  //  def userList = Action {
  //    val user: User = Cache.getOrElse("user") {
  //      User.findById(1l)
  //    }
  //  }
  //缓存对象的删除
  def removeCacheUser = Action {
    try {
      Cache.remove("user")
    }
    catch {
      case t => Ok("清除用户缓存失败")
    }
    Ok("清楚用户缓存")
  }

  //缓存一个http 响应
  def cacheIndex = Cached("homePage") {
    Action {
      Ok("Hello world")
    }
  }
  //响应调用前的时候调用缓存
  //  import play.mvc._
  //  def userProfile = Authenticated {
  //    user =>
  //      Cached(req=>"profile"+user) {
  //        Action {
  //          Ok(views.html.profile(User.find(user)))
  //        }
  //      }
  //  }

  val userForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "email" -> optional(text))((name, username, password, email) => {
        val user = User(name = name, username = username, password = password, email = email, addressId = 0l, id = 0l)
        user
      })((user: User) =>
        Some(user.name, user.username, user.password, user.email)))
  val addUserAddressForm = Form(
    mapping(
      "province" -> nonEmptyText,
      "city" -> nonEmptyText,
      "country" -> nonEmptyText,
      "street" -> optional(text),
      "road" -> optional(text),
      "No" -> optional(text))((province, city, country, street, road, No) => Address(
        province = province,
        city = city,
        country = country,
        street = street,
        road = road,
        No = No,
        id = 0l))((address) => Some(
        address.province,
        address.city,
        address.country,
        address.street,
        address.road,
        address.No)))
  def addUser = TxAction {
    implicit request =>
      addUserAddressForm.bindFromRequest.fold(errors => BadRequest, {
        case (address) =>
          val iAddress = Address.insert(address)
          userForm.bindFromRequest.fold(errors2 => BadRequest, {
            case (user: User) =>
              user.addressId = iAddress.id
              val insertedUser = User.insert(user)
              Redirect(routes.Application.userlist)
          })

      })

  }

  def toAddUser = Action {
    Ok(views.html.addUser(userForm, addUserAddressForm))
  }

  implicit val addressFormat = (
    (__ \ "id").format[Long] ~
    (__ \ "province").format[String] ~
    (__ \ "city").format[String] ~
    (__ \ "country").format[String] ~
    (__ \ "street").format[Option[String]] ~
    (__ \ "road").format[Option[String]] ~
    (__ \ "No").format[Option[String]])(Address.apply, unlift(Address.unapply))
  //  
  //    implicit val userFormat = (
  //      (__ \ "id").format[Long] ~
  //      (__ \ "name").format[String] ~
  //      (__ \ "username").format[String] ~
  //      (__ \ "password").format[String] ~
  //      (__ \ "email").format[Option[String]] ~
  //      (__ \ "addressId").format[Long]
  //    )(User.apply, unlift(User.unapply))

  //  implicit val addressFmt=play.api.libs.json.Json.format[Address]
  //  implicit val userFmt=play.api.libs.json.Json.format[User]
  //  implicit val userWrite = (
  //    (__ \ "id").write[Long] ~
  //    (__ \ "name").write[String] ~
  //    (__ \ "username").write[String] ~
  //    (__ \ "password").write[String] ~
  //    (__ \ "email").write[Option[String]] ~
  //    (__ \ "addressId").write[Long]
  //  )(User)
  def userlist = TxAction {
    val user = User.find()
    //    import org.json4s._
    //    import org.json4s.JsonDSL._
    //    import org.json4s.jackson.JsonMethods._
    //    val json = ("id"->user)
    //    println(compact(render(List(user))))
    Ok(Json.generate[List[User]](user)).as(JSON)
    //    import play.api.libs.json.Json
    //    Ok(Json.toJson(user)(userWrite)).as(JSON)
  }

  //future and ws
  // making an http call
  val result: Future[ws.Response] = {
    WS.url("http://localhost:9000/post").post("content")
  }

  //retrieving the http response result
  //  def feedTitle(feedUrl:String) = Action {
  //    Async {
  //      WS.url(feedUrl).get().map {
  //        implicit response=>
  //          Ok("Feed title: "+(response.json \ "title").as[String])
  //      }
  //    }
  //  }

  //  openID demo
  def toLogin = Action {
    Ok(views.html.login())
  }

  def login = Action {
    implicit req =>
      Form(single("openid" -> nonEmptyText)).bindFromRequest.fold(
        error => {
          Logger.info("bad request " + error.toString)
          BadRequest(error.toString)
        },
        {
          case (openid) =>
            //            AsyncResult(OpenID.redirectURL(openid, 
            //              routes.Application.openIDCallback.absoluteURL()).extend(_.value match {
            //              case Redeemed(url) => Redirect(url)
            //              case Thrown(t) => Redirect(routes.Application.login)
            //            }))
            Ok("")
        })
  }
  //
  //  def openIDCallback = Action {
  //    implicit req =>
  //      AsyncResult(OpenID.verifiedId.extend(_.value match {
  //        case Redeemed(info) => Ok(info.id + "\n" + info.attributes)
  //        case Thrown(t) => Redirect(routes.Application.login)
  //      }))
  //  }

  /**
   * coffee 测试
   * @return
   */
  def coffee() = Action {
    Ok(views.html.coffee())
  }

  /**
   * coffeeMixins
   */
  def coffeeMixins() = Action {
    Ok(views.html.coffeeMixins())
  }

  def coffeeControl() = Action {
    Ok(views.html.coffeeControl())
  }
}

