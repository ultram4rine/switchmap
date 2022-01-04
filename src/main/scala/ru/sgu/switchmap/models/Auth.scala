package ru.sgu.switchmap.models

import sttp.tapir.Schema.annotations._

@description("JWT")
case class AuthToken(
  @description("Token itself")
  @encodedExample(
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
  )
  token: String
)

@description("User login data")
final case class User(
  @description("User name")
  @encodedExample("user")
  username: String,
  @format("password")
  @description("User password")
  @encodedExample("pass")
  password: String,
  @description("If set to `true` then a long-lived token will be created")
  @encodedExample("true")
  rememberMe: Boolean
)
