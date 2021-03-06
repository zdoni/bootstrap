package pl.softwaremill.bootstrap.auth

import org.scalatra.SweetCookies
import org.specs2.mock.Mockito
import javax.servlet.http.HttpServletResponse
import org.scalatra.test.specs2.MutableScalatraSpec
import pl.softwaremill.bootstrap.rest.EntriesServlet
import pl.softwaremill.bootstrap.service.user.UserService
import pl.softwaremill.bootstrap.service.data.UserJson

// http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html

class RememberMeStrategySpec extends MutableScalatraSpec with Mockito {

  "RememberMe" should {
    val httpResponse = mock[HttpServletResponse]
    val app = mock[EntriesServlet]
    val userService = mock[UserService]
    val loggedUser: UserJson = UserJson("admin", "admin@admin.net", "token")
    userService.authenticateWithToken(loggedUser.token) returns Option(loggedUser)

    val rememberMe = true
    val strategy = new RememberMeStrategy(app, rememberMe, userService)

    "authenticate user base on cookie" in {
      // Given
      app.cookies returns new SweetCookies(Map(("rememberMe", loggedUser.token)), httpResponse)

      // When
      val user: Option[UserJson] = strategy.authenticate()

      // Then
      user must not be equalTo(None)
      user.get.login must be equalTo ("admin")
    }

    "not authenticate user with invalid cookie" in {
      // Given
      app.cookies returns new SweetCookies(Map(("rememberMe", loggedUser.token + "X")), httpResponse)

      // When
      val user: Option[UserJson] = strategy.authenticate()

      // Then
      user must be equalTo(null)
    }

  }

}
