package io.vaan.httpbattle.reactivecats

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

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, ExecutionContextExecutorService}
import scala.concurrent.ExecutionContext.Implicits.global

object ReactiveCats extends IOApp {
  private val PORT = 8083
  private val DELAY_SERVICE_URL = "http://localhost:8080"

  private val clientPool: Resource[IO, ExecutorService] =
    Resource.make(IO(Executors.newFixedThreadPool(64)))(ex => IO(ex.shutdown()))

  private val clientExecutor: Resource[IO, ExecutionContextExecutor] =
    clientPool.map(ExecutionContext.fromExecutor)

  private val httpClient = clientExecutor.flatMap(ex => BlazeClientBuilder[IO](ex).resource)

  private def httpApp(client: Client[IO]): Kleisli[IO, Request[IO], Response[IO]] = HttpRoutes.of[IO] {
    case GET -> Root / delayMillis =>
      client
        .expect[String](s"$DELAY_SERVICE_URL/$delayMillis")
        .flatMap(response => Ok(s"ReactiveCats: $response"))
  }.orNotFound

  override def run(args: List[String]): IO[ExitCode] = {
    val serverPool: ExecutorService = Executors.newFixedThreadPool(64)
    val serverExecutor: ExecutionContextExecutor = ExecutionContext.fromExecutor(serverPool)

    httpClient.use { client =>
      BlazeServerBuilder[IO](serverExecutor)
        .bindHttp(port = PORT, host = "localhost")
        .withHttpApp(httpApp(client))
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    }
  }
}
