
// @GENERATOR:play-routes-compiler
// @SOURCE:/home/razvan/groupproject/team_mike/tworkserver/conf/routes
// @DATE:Thu Feb 04 12:20:52 GMT 2016


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
