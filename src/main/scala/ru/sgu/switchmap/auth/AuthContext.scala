package ru.sgu.switchmap.auth

import org.http4s.rho.AuthedContext
import ru.sgu.switchmap.Main.AppTask
import zio.interop.catz._

object AuthContext extends AuthedContext[AppTask, AuthStatus.Status]
