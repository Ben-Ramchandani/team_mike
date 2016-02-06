
// @GENERATOR:play-routes-compiler
// @SOURCE:/media/ben/hdd_free/programming/team_mike/tworkserver/conf/routes
// @DATE:Sat Feb 06 09:59:53 GMT 2016

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
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """data/$jobID<[^/]+>""", """controllers.Application.data(jobID:Long)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """result/$jobID<[^/]+>""", """controllers.Application.result(jobID:Long)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """computation/$id<[^/]+>""", """controllers.Application.subscribe(id:Long)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """function/$functionID<[^/]+>""", """controllers.Application.function(functionID:String)"""),
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

  // @LINE:19
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
      """ Availability request
 JSON:
	phone-id       : string
	battery-life   : 0 - 100	
	on-charge      : 0 / 1
	on-WiFi		   : 0 / 1
 set cookie in the header; all the other requests should have that cookie set.
 also initialise session -> send ok().""",
      this.prefix + """available"""
    )
  )

  // @LINE:31
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
      """ Job request
 just include the cookie

	This will return a json object:
	job-id 			: long
	function-class 	: string    -You call the URLCLassLoader. 
 	computation-id	: long
	job-description : string
	return a non 200 code if there is no job.
""",
      this.prefix + """job"""
    )
  )

  // @LINE:35
  private[this] lazy val controllers_Application_data4_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("data/"), DynamicPart("jobID", """[^/]+""",true)))
  )
  private[this] lazy val controllers_Application_data4_invoker = createInvoker(
    Application_1.data(fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "data",
      Seq(classOf[Long]),
      "GET",
      """ Data request
  This will return a raw file and a 200 code.""",
      this.prefix + """data/$jobID<[^/]+>"""
    )
  )

  // @LINE:42
  private[this] lazy val controllers_Application_result5_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("result/"), DynamicPart("jobID", """[^/]+""",true)))
  )
  private[this] lazy val controllers_Application_result5_invoker = createInvoker(
    Application_1.result(fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "result",
      Seq(classOf[Long]),
      "POST",
      """ Send result
	
	Send the raw file -> empty is failed

""",
      this.prefix + """result/$jobID<[^/]+>"""
    )
  )

  // @LINE:47
  private[this] lazy val controllers_Application_subscribe6_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("computation/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_Application_subscribe6_invoker = createInvoker(
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

  // @LINE:53
  private[this] lazy val controllers_Application_function7_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("function/"), DynamicPart("functionID", """[^/]+""",true)))
  )
  private[this] lazy val controllers_Application_function7_invoker = createInvoker(
    Application_1.function(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "function",
      Seq(classOf[String]),
      "GET",
      """ Serialized vs URL""",
      this.prefix + """function/$functionID<[^/]+>"""
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
  
    // @LINE:19
    case controllers_Application_available2_route(params) =>
      call { 
        controllers_Application_available2_invoker.call(Application_1.available())
      }
  
    // @LINE:31
    case controllers_Application_job3_route(params) =>
      call { 
        controllers_Application_job3_invoker.call(Application_1.job())
      }
  
    // @LINE:35
    case controllers_Application_data4_route(params) =>
      call(params.fromPath[Long]("jobID", None)) { (jobID) =>
        controllers_Application_data4_invoker.call(Application_1.data(jobID))
      }
  
    // @LINE:42
    case controllers_Application_result5_route(params) =>
      call(params.fromPath[Long]("jobID", None)) { (jobID) =>
        controllers_Application_result5_invoker.call(Application_1.result(jobID))
      }
  
    // @LINE:47
    case controllers_Application_subscribe6_route(params) =>
      call(params.fromPath[Long]("id", None)) { (id) =>
        controllers_Application_subscribe6_invoker.call(Application_1.subscribe(id))
      }
  
    // @LINE:53
    case controllers_Application_function7_route(params) =>
      call(params.fromPath[String]("functionID", None)) { (functionID) =>
        controllers_Application_function7_invoker.call(Application_1.function(functionID))
      }
  }
}