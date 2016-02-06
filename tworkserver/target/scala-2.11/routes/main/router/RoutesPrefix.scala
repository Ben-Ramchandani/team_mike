
// @GENERATOR:play-routes-compiler
// @SOURCE:/media/ben/hdd_free/programming/team_mike/tworkserver/conf/routes
// @DATE:Sat Feb 06 09:59:53 GMT 2016


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
