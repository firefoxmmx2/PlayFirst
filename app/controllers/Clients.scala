package controllers

import java.io._
import models._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.libs._
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Security._
import play.api.libs.concurrent.Akka
import play.api.libs.iteratee.Enumerator

object Clients extends Controller {
	val barForm = Form(single("name" -> nonEmptyText))
	implicit val jsonFormat = Json.writes[Bar]

	def addBar = Action {
		implicit request =>
			barForm.bindFromRequest.fold(
				errors => BadRequest,
				{
					case (name) =>
						Bar.create(Bar(0, name))
						Redirect(routes.Clients.toBar)
				})
	}

	def toBar = Action {
		Ok(views.html.bar(barForm))
	}

	def bar(bid: Long) = Action {
		Ok(Json.toJson(Bar.getById(bid)))
	}
	def listBar() = Action {
		val bars = Bar.findAll
		val json = Json.toJson(bars)
		Ok(json)
	}

	def updateBar() = Action {
		Ok
	}

	def deleteBar(bid: Long) = Action {
		Ok
	}

	def flash_() = Action {
		implicit request =>
			Ok(flash.get("success").getOrElse("Welcome"))
	}

	def flashSave() = Action {
		Redirect("/flash").flashing("success" -> "The item has been created")
	}

	def save() = Action {
		implicit request =>
			val body: AnyContent = request.body
			val textBody: Option[String] = body.asText

			textBody.map(text =>
				Ok("Got:" + text)).getOrElse(BadRequest("Expecting text/plain request body"))

	}

	def saveAsJson = Action {
		request =>
			Ok("Got: " + request.body).as("application/json")
	}

	def saveAsFile = Action(parse.file(to = new File("/tmp/upload"))) {
		implicit request =>
			Ok("Saved the request content to " + request.body)
	}

	def toBodyParse = Action {
		Ok(views.html.bodyParse(""))
	}

	//	val storeInUserFile = parse.using(request =>
	//		request.session.get("username").map(user =>
	//			file(to = new File("/tmp/" + user + ".upload")))
	//			.getOrElse(error(Unauthorized("you don't have the right to upload here"))))
	//
	//	def saveAsMixed = Action(storeInUserFile) { implicit  request =>
	//		Ok("Saved the request content to "+ request.body)
	//	}

	//	def saveAsLengthLimit = Action(maxLength(1024)) { request =>
	//		Ok("Got: "+request.body)
	//	}

	def saveAsLengthLimit = TODO

	case class Logging[A](action: Action[A]) extends Action[A] {
		def apply(request: Request[A]): Result = {
			Logger.info("Calling action")
			action(request)
		}

		lazy val parser = action.parser

	}

	//	实现2
	//	def Logging[A](action:Action[A]):Action[A]={
	//		Action(action.parser){request =>
	//			Logger.info("Call action")
	//			action(request)
	//		}
	//	}
	def index = Logging {
		Action {
			Ok("Hello World")
		}
	}

	def index2 = Logging {
		Action(parse.text) {
			request =>
				Ok("Hello world")
		}
	}

	//	def index3 = Authenticated { user =>
	//		Action { request =>
	//			Ok("Hello "+user.name)
	//		}
	//	}

	case class AuthenticatedRequest[A](user: User, private val request: Request[A]) extends WrappedRequest(request)

	//	def Authenticated[A](p: BodyParser[A])(f: AuthenticatedRequest[A] => Result) = {
	//		Action(p) { request =>
	//			val result = for {
	//				id <- request.session.get("user")
	//				user <- User.find(request.getQueryString("id").getOrElse("0").toLong)
	//			} yield f(Authenticated(user)(request))
	//			result getOrElse Unauthorized
	//		}
	//	}

	import play.api.mvc.BodyParsers._
	//	def Authenticated(f: AuthenticatedRequest[AnyContent] => Result): Action[AnyContent] = {
	//		Authenticated(parse.anyContent)(f)
	//	}

	val list = Action {
		implicit request =>
			val items = Bar.findAll
			render {
				case Accepts.Html => Ok(views.html.bar(barForm))
				case Accepts.Json => Ok(Json.toJson(items))
			}
	}

	//	val futurePIValue:Future[Double]=computePIAsynchronously()
	//	val futureResult:Future[Result]=futurePIValue map {
	//		pi=> Ok("PI value computed:"+pi)
	//	}

	//	def index3=Action {
	//		val futrueInt = Akka.future { 1+1 }
	//			Async {
	//			futureInt map(i=>Ok("God result:"+i))
	//		}
	//	}

	def index3 = Action {
		val file = new File("/tmp/(成年コミック) [胡桃屋ましみん] 肉妻通信 [08-01-01].zip")
		val fileContent: Enumerator[Array[Byte]] = Enumerator.fromFile(file)

		SimpleResult(
			header = ResponseHeader(200),
			body = fileContent)
	}

	def index4 = Action {
		val file = new File("/tmp/(成年コミック) [胡桃屋ましみん] 肉妻通信 [08-01-01].zip")
		val fileContent: Enumerator[Array[Byte]] = Enumerator.fromFile(file)
		SimpleResult(
			header = ResponseHeader(200,
				Map(CONTENT_LENGTH -> file.length().toString)),
			body = fileContent)
	}

	def index5 = Action {
		Ok.sendFile(new File("/tmp/(成年コミック) [胡桃屋ましみん] 肉妻通信 [08-01-01].zip"))
	}

	def index6 = Action {
		Ok.sendFile(
			content = new File("/tmp/(成年コミック) [胡桃屋ましみん] 肉妻通信 [08-01-01].zip"),
			fileName = _ => "termsOfService.pdf")
	}

}