package com.kubukoz

import cats.effect.implicits._
import cats.effect.std.Dispatcher
import cats.implicits._
import cats.~>
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import zio.ZIO
import zio.interop.catz.implicits._

object ZIOUtils {

  extension [F[_], A](effect: F[A])
    def toZioTask(using d: Dispatcher[F]): zio.Task[A] = fToTask[F].apply(effect)

  def fToTask[F[_]](using d: Dispatcher[F]): F ~> zio.Task =
    new (F ~> zio.Task) {

      def apply[A](fa: F[A]): zio.Task[A] = {
        val (fut, canc) = d.unsafeToFutureCancelable(fa)

        ZIO.fromFuture(_ => fut).onInterrupt(ZIO.fromFuture(_ => canc()).orDie)
      }

    }

}
