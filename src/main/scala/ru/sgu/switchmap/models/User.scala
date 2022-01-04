package ru.sgu.switchmap.models

import sttp.tapir.Schema.annotations._

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
