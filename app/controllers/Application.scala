package controllers

import play.api.mvc._
import play.api.data.Form._
import play.api.data.Forms._
import play.api.data._
import models._
import anorm._
import play.api.libs.json._

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
	
	
}