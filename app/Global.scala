import play.api._
import org.squeryl.adapters.H2Adapter
import org.squeryl.{ Session, SessionFactory }
import play.api.db._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    implicit val _app = app
    Logger.info("Initializing squeryl session factory")
    SessionFactory.concreteFactory = Some(() => Session.create(DB.getConnection(), new H2Adapter))
    Logger.info("Application has started")

  }

  override def onStop(app: Application) {
    Logger.info("Application has stoped")
  }
}