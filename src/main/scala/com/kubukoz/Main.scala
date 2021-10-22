package com.kubukoz

import caliban.CalibanError
import caliban.GraphQL.graphQL
import caliban.GraphQLRequest
import caliban.GraphQLResponse
import caliban.Http4sAdapter
import caliban.RootResolver
import caliban.interop.cats.implicits._
import caliban.wrappers.Wrapper
import caliban.wrappers.Wrapper.OverallWrapper
import caliban.wrappers.Wrapper.WrappingFunction
import caliban.wrappers.Wrappers
import cats.Applicative
import cats.ApplicativeThrow
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.implicits._
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
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

import ZIOUtils._

object Main extends IOApp.Simple {
  implicit val zioRuntime: zio.Runtime[zio.ZEnv] = zio.Runtime.default

  def mkRoutes[F[_]: Async: Logger](
    queries: Queries[F]
  ): Resource[F, HttpRoutes[F]] = Dispatcher[F].flatMap { implicit disp =>
    graphQL(RootResolver(queries))
      .interpreterAsync[F]
      .map {
        _.wrapExecutionWith { program =>
          program.tap { result =>
            result
              .errors
              .traverse_(Logger[F].error(_)("Error in GraphQL interpreter"))
              .toZioTask
              .orDie
          }
        }
      }
      .map { interp =>
        Router[F](
          "/api/graphql" -> Http4sAdapter.makeHttpServiceF(
            interp
          )
        )
      }
      .toResource
  }

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  implicit val characters: Characters[IO] = Characters.instance[IO]

  def run: IO[Unit] =
    mkRoutes[IO](Queries.instance[IO]).flatMap { routes =>
      BlazeServerBuilder[IO]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(routes.orNotFound)
        .resource
    }.useForever

}
