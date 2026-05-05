package com.example

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import org.apache.pekko.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, StatusCodes, Uri}
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.Await
import scala.concurrent.duration.*

/** Demonstrates pekko-http: routing, JSON marshalling, and a client request. */
object HttpPoc extends DefaultJsonProtocol:

  final case class Item(id: Int, name: String)
  given RootJsonFormat[Item]       = jsonFormat2(Item.apply)
  given RootJsonFormat[List[Item]] = listFormat[Item]

  def run(): Unit =
    given system: ActorSystem[Nothing] =
      ActorSystem[Nothing](Behaviors.empty[Nothing], "http-poc")
    import system.executionContext

    val store = AtomicReference(Map(1 -> Item(1, "alpha"), 2 -> Item(2, "beta")))

    val routes: Route = concat(
      path("hello") {
        get { complete("hi from pekko-http") }
      },
      pathPrefix("items") {
        concat(
          pathEndOrSingleSlash {
            concat(
              get { complete(store.get().values.toList) },
              post {
                entity(as[Item]) { item =>
                  store.updateAndGet(m => m + (item.id -> item))
                  complete(StatusCodes.Created -> item)
                }
              }
            )
          },
          path(IntNumber) { id =>
            get {
              store.get().get(id) match
                case Some(item) => complete(item)
                case None       => complete(StatusCodes.NotFound)
            }
          }
        )
      }
    )

    val binding = Await.result(
      Http().newServerAt("127.0.0.1", 8080).bind(routes),
      5.seconds
    )
    system.log.info(s"server bound at ${binding.localAddress}")

    val list = Http()
      .singleRequest(HttpRequest(uri = Uri("http://127.0.0.1:8080/items")))
      .flatMap(r => Unmarshal(r.entity).to[List[Item]])
    system.log.info(s"client GET /items -> ${Await.result(list, 5.seconds)}")

    val created = Http()
      .singleRequest(
        HttpRequest(
          method = HttpMethods.POST,
          uri    = Uri("http://127.0.0.1:8080/items"),
          entity = HttpEntity(ContentTypes.`application/json`, """{"id":3,"name":"gamma"}""")
        )
      )
      .flatMap(r => Unmarshal(r.entity).to[Item])
    system.log.info(s"client POST /items -> ${Await.result(created, 5.seconds)}")

    Await.result(binding.unbind(), 5.seconds)
    system.terminate()
    Await.result(system.whenTerminated, 10.seconds)
