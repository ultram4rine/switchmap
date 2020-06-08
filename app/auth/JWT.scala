package auth

import org.json4s
import org.json4s.{DefaultFormats, JObject, JString}
import pdi.jwt.{JwtAlgorithm, JwtJson4s}
import ru.sgu.switchmap.configuration.Configuration

import scala.util.Try

object JWT {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def encodeUserJwt(userName: String): json4s.JObject = {
    val token = JwtJson4s.encode(
      JObject(("user", JString(userName))),
      Configuration.jwtKey,
      JwtAlgorithm.HS256
    )
    JObject(("token", JString(token)))
  }

  def decodeUserJwt(token: String): Try[UserTokenData] = {
    for {
      decoded <- JwtJson4s.decodeJson(
        token,
        Configuration.jwtKey,
        Seq(JwtAlgorithm.HS256)
      )
      userTokenData = decoded.extract[Token].data
    } yield userTokenData
  }
}

case class Token(data: UserTokenData, exp: Int, iat: Int, iss: String)
case class UserTokenData(id: String)
