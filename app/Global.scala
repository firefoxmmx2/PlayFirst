import play.api._
import org.squeryl.adapters.H2Adapter
import org.squeryl.{ Session, SessionFactory }
import play.api.db._
import play.api.mvc.WithFilters
import play.api.mvc.Filter
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Handler

object Global extends WithFilters(AccessLog) {

  override def onStart(app: Application) {
    implicit val _app = app
    Logger.info("Initializing squeryl session factory")
    SessionFactory.concreteFactory = Some(() => Session.create(DB.getConnection()(app), new H2Adapter))
    Logger.info("Application has started")

  }

  override def onStop(app: Application) {
    Logger.info("Application has stoped")
  }

  //overriding onRouteRequest
  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    println("executed before every request: " + request.toString)
    super.onRouteRequest(request)
  }
}

object AccessLog extends Filter {
  override def apply(next: RequestHeader => Result)(req: RequestHeader): Result = {
    val result = next(req)
    play.Logger.info(req + "\n\t => " + result)
    result
  }
}
