import cats.*
import cats.syntax.all.*
import weaver.*

object ApplicativeSuite extends SimpleIOSuite {
  pureTest("Applicative") {
    val username: Option[String] = Some("username")
    val password: Option[String] = Some("password")
    val url: Option[String]      = Some("some.login.url.here")

    case class Connection(username: String, password: String)

    def attemptConnect(username: String, password: String, url: String): Option[Connection] = Connection(username, password).some

    val result = Applicative[Option].map3(username, password, url)(attemptConnect)

    expect(result == Connection("username", "password").some.some)
  }

  pureTest("Applicative with mapN") {
    val username: Option[String] = Some("username")
    val password: Option[String] = Some("password")
    val url: Option[String]      = Some("some.login.url.here")

    case class Connection(username: String, password: String)

    def attemptConnect(username: String, password: String, url: String): Option[Connection] = Connection(username, password).some

    val result = (username, password, url).mapN(attemptConnect)

    expect(result == Connection("username", "password").some.some)
  }
}
