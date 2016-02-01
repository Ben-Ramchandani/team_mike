
// @GENERATOR:play-routes-compiler
// @SOURCE:/home/razvan/groupproject/team_mike/tworkserver/conf/routes
// @DATE:Mon Feb 01 20:43:47 GMT 2016

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._

import _root_.controllers.Assets.Asset
import _root_.play.libs.F

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:6
  Application_1: controllers.Application,
  // @LINE:9
  Assets_0: controllers.Assets,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:6
    Application_1: controllers.Application,
    // @LINE:9
    Assets_0: controllers.Assets
  ) = this(errorHandler, Application_1, Assets_0, "/")

  import ReverseRouteContext.empty

  def withPrefix(prefix: String): Routes = {
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, Application_1, Assets_0, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix, """controllers.Application.index()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """assets/$file<.+>""", """controllers.Assets.versioned(path:String = "/public", file:Asset)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """available""", """controllers.Application.available()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """job""", """controllers.Application.job()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """computation/$id<[^/]+>""", """controllers.Application.subscribe(id:Long)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """result/$id<[^/]+>""", """controllers.Application.result(id:Long)"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:6
  private[this] lazy val controllers_Application_index0_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix)))
  )
  private[this] lazy val controllers_Application_index0_invoker = createInvoker(
    Application_1.index(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "index",
      Nil,
      "GET",
      """ Home page""",
      this.prefix + """"""
    )
  )

  // @LINE:9
  private[this] lazy val controllers_Assets_versioned1_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("assets/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_versioned1_invoker = createInvoker(
    Assets_0.versioned(fakeValue[String], fakeValue[Asset]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Assets",
      "versioned",
      Seq(classOf[String], classOf[Asset]),
      "GET",
      """ Map static resources from the /public folder to the /assets URL path""",
      this.prefix + """assets/$file<.+>"""
    )
  )

  // @LINE:12
  private[this] lazy val controllers_Application_available2_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("available")))
  )
  private[this] lazy val controllers_Application_available2_invoker = createInvoker(
    Application_1.available(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "available",
      Nil,
      "GET",
      """ Availability request""",
      this.prefix + """available"""
    )
  )

  // @LINE:15
  private[this] lazy val controllers_Application_job3_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("job")))
  )
  private[this] lazy val controllers_Application_job3_invoker = createInvoker(
    Application_1.job(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "job",
      Nil,
      "GET",
      """ Job request""",
      this.prefix + """job"""
    )
  )

  // @LINE:18
  private[this] lazy val controllers_Application_subscribe4_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("computation/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_Application_subscribe4_invoker = createInvoker(
    Application_1.subscribe(fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "subscribe",
      Seq(classOf[Long]),
      "GET",
      """ Computation subscription""",
      this.prefix + """computation/$id<[^/]+>"""
    )
  )

  // @LINE:21
  private[this] lazy val controllers_Application_result5_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("result/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_Application_result5_invoker = createInvoker(
    Application_1.result(fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "result",
      Seq(classOf[Long]),
      "POST",
      """ Send result""",
      this.prefix + """result/$id<[^/]+>"""
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:6
    case controllers_Application_index0_route(params) =>
      call { 
        controllers_Application_index0_invoker.call(Application_1.index())
      }
  
    // @LINE:9
    case controllers_Assets_versioned1_route(params) =>
      call(Param[String]("path", Right("/public")), params.fromPath[Asset]("file", None)) { (path, file) =>
        controllers_Assets_versioned1_invoker.call(Assets_0.versioned(path, file))
      }
  
    // @LINE:12
    case controllers_Application_available2_route(params) =>
      call { 
        controllers_Application_available2_invoker.call(Application_1.available())
      }
  
    // @LINE:15
    case controllers_Application_job3_route(params) =>
      call { 
        controllers_Application_job3_invoker.call(Application_1.job())
      }
  
    // @LINE:18
    case controllers_Application_subscribe4_route(params) =>
      call(params.fromPath[Long]("id", None)) { (id) =>
        controllers_Application_subscribe4_invoker.call(Application_1.subscribe(id))
      }
  
    // @LINE:21
    case controllers_Application_result5_route(params) =>
      call(params.fromPath[Long]("id", None)) { (id) =>
        controllers_Application_result5_invoker.call(Application_1.result(id))
      }
  }
}