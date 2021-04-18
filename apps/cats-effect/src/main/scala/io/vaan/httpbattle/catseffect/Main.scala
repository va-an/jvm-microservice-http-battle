package io.vaan.httpbattle.catseffect

import java.util.concurrent.{ExecutorService, Executors, TimeUnit}
import java.util.concurrent.Executors.newFixedThreadPool
import cats.data.Kleisli
import cats.effect.IO
import cats.effect._
import org.http4s._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.implicits._
import org.http4s.dsl.io._
import org.http4s.client.blaze._
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, ExecutionContextExecutorService}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object Main extends IOApp {
  private val PORT = 8083
  private val DELAY_SERVICE_URL = "http://localhost:8080"

  private val clientPool: Resource[IO, ExecutorService] =
    Resource.make(IO(Executors.newFixedThreadPool(64)))(ex => IO(ex.shutdown()))

  private val clientExecutor: Resource[IO, ExecutionContextExecutor] =
    clientPool.map(ExecutionContext.fromExecutor)

  private val httpClient =
    clientExecutor.flatMap(ex =>
      BlazeClientBuilder[IO](ex)
        .withConnectTimeout(60 seconds)
        .withRequestTimeout(60 seconds)
        .withResponseHeaderTimeout(60 seconds)
        .withIdleTimeout(100 seconds)
        .withMaxTotalConnections(10_000)
        .withMaxWaitQueueLimit(10_000)
        .resource
    )

  private def httpApp(
      client: Client[IO]
  ): Kleisli[IO, Request[IO], Response[IO]] = HttpRoutes
    .of[IO] { case GET -> Root / delayMillis =>
      client
        .expect[String](s"$DELAY_SERVICE_URL/$delayMillis")
        .flatMap(response => Ok(s"ReactiveCats: $response"))
        .timeout(30 seconds)
    }
    .orNotFound

  override def run(args: List[String]): IO[ExitCode] = {
    httpClient.use { client =>
      BlazeServerBuilder[IO](ExecutionContext.global)
        .bindHttp(port = PORT, host = "localhost")
        .withHttpApp(httpApp(client))
        .withMaxConnections(10_000)
        .withResponseHeaderTimeout(60 seconds)
        .withIdleTimeout(120 seconds)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    }
  }
}
