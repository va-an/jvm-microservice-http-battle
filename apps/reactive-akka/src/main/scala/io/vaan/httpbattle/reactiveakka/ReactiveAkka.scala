package io.vaan.httpbattle.reactiveakka

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn
import scala.util.{Failure, Success}


object ReactiveAkka {
  private val PORT = 8084
  private val DELAY_SERVICE_URL = "http://localhost:8080";

  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def main(args: Array[String]) {
    val route: Route =
      pathPrefix(Segment) { delayMillis =>
        get {
          val response: Future[HttpResponse] = Http()
            .singleRequest(HttpRequest(uri = s"$DELAY_SERVICE_URL/$delayMillis"))

          response.onComplete {
            case Success(response) => println(s"ReactiveAkka: $response")
            case Failure(exception) => sys.error(exception.toString)
          }

          complete(response)
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
