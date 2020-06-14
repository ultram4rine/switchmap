package auth

import java.util.UUID

import com.mohiva.play.silhouette.api.Identity

case class User(userID: UUID) extends Identity
