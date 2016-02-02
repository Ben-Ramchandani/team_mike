
// @GENERATOR:play-routes-compiler
// @SOURCE:/home/razvan/groupproject/team_mike/tworkserver/conf/routes
// @DATE:Tue Feb 02 12:10:43 GMT 2016

import play.api.routing.JavaScriptReverseRoute
import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset
import _root_.play.libs.F

// @LINE:6
package controllers.javascript {
  import ReverseRouteContext.empty

  // @LINE:9
  class ReverseAssets(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:9
    def versioned: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Assets.versioned",
      """
        function(file) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "assets/" + (""" + implicitly[PathBindable[Asset]].javascriptUnbind + """)("file", file)})
        }
      """
    )
  
  }

  // @LINE:6
  class ReverseApplication(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:28
    def result: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.result",
      """
        function(jobID) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "result/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("jobID", jobID)})
        }
      """
    )
  
    // @LINE:31
    def subscribe: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.subscribe",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "computation/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("id", id)})
        }
      """
    )
  
    // @LINE:18
    def available: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.available",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "available"})
        }
      """
    )
  
    // @LINE:6
    def index: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.index",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + """"})
        }
      """
    )
  
    // @LINE:22
    def job: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.job",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "job"})
        }
      """
    )
  
    // @LINE:25
    def data: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Application.data",
      """
        function(jobID) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "data/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("jobID", jobID)})
        }
      """
    )
  
  }


}