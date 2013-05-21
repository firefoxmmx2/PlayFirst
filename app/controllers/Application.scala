package controllers

import java.io.FileInputStream
import models.Bar
import models.Task
import play.api.data.Form
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.single
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.ChunkedResult
import play.api.mvc.ResponseHeader
import play.api.libs.iteratee.Enumeratee
import play.api.templates.Html
import play.api.libs.Comet

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
      """<script>console.log('bar')</script>"""
    )
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
}