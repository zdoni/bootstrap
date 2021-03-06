package pl.softwaremill.bootstrap.service

import config.BootstrapConfiguration
import schedulers.EmailSendingService
import pl.softwaremill.bootstrap.dao.{PasswordResetCodeDAO, UserDAO}
import templates.EmailTemplatingEngine
import pl.softwaremill.bootstrap.domain.User
import com.weiglewilczek.slf4s.Logging
import pl.softwaremill.common.util.RichString
import pl.softwaremill.bootstrap.domain.PasswordResetCode
import org.joda.time.DateTime

/**
 * .
 */
class PasswordRecoveryService(userDao: UserDAO, codeDao: PasswordResetCodeDAO,
                              emailSendingService: EmailSendingService, emailTemplatingEngine: EmailTemplatingEngine) extends Logging {

  def sendResetCodeToUser(login: String) {
    logger.debug("Preparing to generate and send reset code to user")
    logger.debug("Searching for user")
    val userOption = userDao.findByLoginOrEmail(login)

    userOption match {
      case Some(user) => {
        logger.debug("User found")
        val user = userOption.get
        val code = PasswordResetCode(code = RichString.generateRandom(32), userId = user._id)
        storeCode(code)
        sendCode(user, code)
      }
      case None => logger.debug("User not found")
    }
  }

  private def storeCode(code: PasswordResetCode) {
    logger.debug("Storing code")
    codeDao.store(code)
  }

  private def sendCode(user: User, code: PasswordResetCode) {
    logger.debug("Scheduling e-mail with reset code")
    emailSendingService.scheduleEmail(user.email, prepareResetEmail(user, code))
  }

  private def prepareResetEmail(user: User, code: PasswordResetCode) = {
    logger.debug("Preparing content for password reset e-mail")

    val resetLink: String = if (BootstrapConfiguration.resetLinkPattern != null) {
      String.format(BootstrapConfiguration.resetLinkPattern, code.code)
    } else {
      "http://localhost:8080/#/password-reset?code=" + code.code
    }
    emailTemplatingEngine.passwordReset(user.login, resetLink)
  }

  def performPasswordReset(code: String, newPassword: String): Either[String, Boolean] = {
    logger.debug("Performing password reset")
    codeDao.load(code) match {
      case Some(c) => {
        if (c.validTo.isAfter(new DateTime())) {
          changePassword(c, newPassword)
          invalidateResetCode(c)
          Right(true)
        } else {
          invalidateResetCode(c)
          Left("Your reset code is invalid. Please try again.")
        }
      }
      case None => {
        logger.debug("Reset code not found")
        Left("Your reset code is invalid. Please try again.")
      }
    }
  }

  private def changePassword(code: PasswordResetCode, newPassword: String) {
    userDao.load(code.userId.toString) match {
      case Some(u) => userDao.changePassword(u._id.toString, User.encryptPassword(newPassword, u.salt))
      case None => logger.debug("User does not exist")
    }
  }

  private def invalidateResetCode(code: PasswordResetCode) {
    codeDao.delete(code)
  }
}