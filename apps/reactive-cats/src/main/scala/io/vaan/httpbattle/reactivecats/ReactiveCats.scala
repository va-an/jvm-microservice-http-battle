package io.vaan.httpbattle.reactivecats

import java.util.concurrent.{ExecutorService, Executors}
import java.util.concurrent.Executors.newFixedThreadPool

import cats.effect.IO
import cats.effect._
import org.http4s._
import org.http4s.client.JavaNetClientBuilder
import org.http4s.implicits._
import org.http4s.dsl.io._
import org.http4s.client.blaze._
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, ExecutionContextExecutorService}
import scala.concurrent.ExecutionContext.Implicits.global

object ReactiveCats extends IOApp {
  private val PORT = 8083
  private val DELAY_SERVICE_URL = "http://localhost:8080"

  val clientPool: ExecutorService = Executors.newFixedThreadPool(64)
  val clientExecutor: ExecutionContextExecutor = ExecutionContext.fromExecutor(clientPool)

  private val httpClient = BlazeClientBuilder[IO](clientExecutor).resource

  private val httpApp = HttpRoutes.of[IO] {
    case GET -> Root / delayMillis =>
      httpClient.use { client =>
        client
          .expect[String](s"$DELAY_SERVICE_URL/$delayMillis")
          .flatMap(response => Ok(s"ReactiveCats: $response"))
      }
  }.orNotFound

  val serverPool: ExecutorService = Executors.newFixedThreadPool(64)
  val serverExecutor: ExecutionContextExecutor = ExecutionContext.fromExecutor(serverPool)

  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](serverExecutor)
      .bindHttp(port = PORT, host = "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
