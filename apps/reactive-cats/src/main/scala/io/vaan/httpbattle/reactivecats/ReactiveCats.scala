package io.vaan.httpbattle.reactivecats

import cats.effect.IO
import cats.effect._
import org.http4s._
import org.http4s.implicits._
import org.http4s.dsl.io._
import org.http4s.client.blaze._
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object ReactiveCats extends IOApp {
  private val DELAY_SERVICE_URL = "http://localhost:8080";

  private val httpApp = HttpRoutes.of[IO] {
    case GET -> Root / delayMillis =>
      BlazeClientBuilder[IO](global).resource.use { client =>
        client
          .expect[String](s"$DELAY_SERVICE_URL/$delayMillis")
          .flatMap(response => Ok(s"ReactiveCats: $response"))
      }
  }.orNotFound

  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(port = 8083, host = "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
