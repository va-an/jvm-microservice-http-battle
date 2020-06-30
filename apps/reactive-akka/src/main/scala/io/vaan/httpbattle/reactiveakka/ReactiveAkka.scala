package io.vaan.httpbattle.reactiveakka

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object ReactiveAkka {
  private val PORT = 8084
  private val DELAY_SERVICE_URL = "http://localhost:8080";

  private implicit val system: ActorSystem = ActorSystem("my-system")
  private implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private val httpClient: HttpExt = Http()

  def main(args: Array[String]): Unit = {
    val route: Route =
      pathPrefix(Segment) { delayMillis =>
        get {
          complete (for {
            response <- httpClient.singleRequest(HttpRequest(uri = s"$DELAY_SERVICE_URL/$delayMillis"))
            responseText <- Unmarshal(response.entity).to[String]
            responseWithText = HttpResponse(status = StatusCodes.OK, entity = s"ReactiveAkka: $responseText")
          } yield responseWithText)
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", PORT)

    println(s"Server online at http://localhost:$PORT/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
