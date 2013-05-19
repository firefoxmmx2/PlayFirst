package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Security._
import play.api.data._
import models._
import play.api.libs.json._
import play.api.data.Forms._
import java.io._

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

	def Authenticated[A](p: BodyParser[A])(f: AuthenticatedRequest[A] => Result) = {
		Action(p) { request =>
			val result = for {
				id <- request.session.get("user")
				user <- User.find(request.getQueryString("id").getOrElse("0").toLong)
			} yield f(Authenticated(user)(request))
			result getOrElse Unauthorized
		}
	}

	import play.api.mvc.BodyParsers._
	def Authenticated(f: AuthenticatedRequest[AnyContent] => Result): Action[AnyContent] = {
		Authenticated(parse.anyContent)(f)
	}
	
}