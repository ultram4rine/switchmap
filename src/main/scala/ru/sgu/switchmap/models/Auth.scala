package ru.sgu.switchmap.models

final case class User(username: String, password: String, rememberMe: Boolean)

final case class AuthToken(token: String)
