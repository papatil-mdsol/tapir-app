package com.pankaj.tapir.server

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxEitherId
import com.comcast.ip4s.IpLiteralSyntax
import com.pankaj.tapir.endpoints.{AnimalEndpoints, ErrorResponse, Kitten}
import com.pankaj.tapir.server.{Database => DB}
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import sttp.model.StatusCode
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.{AnyEndpoint, PublicEndpoint, endpoint, query, stringBody}

import scala.concurrent.ExecutionContext

object KittenHtt4sServer extends IOApp {
  // the endpoint: single fixed path input ("hello"), single query parameter
  // corresponds to: GET /hello?name=...
  private val helloWorld: PublicEndpoint[String, Unit, String, Any] =
  endpoint.get.in("hello").in(query[String]("name")).out(stringBody)

  // converting an endpoint to a route (providing server-side logic); extension method comes from imported packages
  private val hello: HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toRoutes(helloWorld.serverLogic(name => IO(s"Hello, $name!".asRight[Unit])))

  private val getKitten: HttpRoutes[IO] = {
    Http4sServerInterpreter[IO]().toRoutes(AnimalEndpoints.kittens.serverLogic(_ => IO[Either[String, List[Kitten]]](Right(DB.kittens))))
  }

  private val postKittens: HttpRoutes[IO] = Http4sServerInterpreter[IO]().toRoutes {
    AnimalEndpoints.kittenPost.serverLogic(kitten => {
      if (kitten.id <= 0) {
        IO(Left(StatusCode.BadRequest -> ErrorResponse("negative ids are not accepted")))
      } else {
        if (DB.kittens.exists(_.id == kitten.id)) {
          IO(Left(StatusCode.BadRequest -> ErrorResponse(s"kitten with ud ${kitten.id} already exists")))
        } else {
          DB.kittens = DB.kittens :+ kitten
          IO(Right(StatusCode.Ok -> kitten))
        }
      }
    })
  }

  private val putKittens = Http4sServerInterpreter[IO]().toRoutes(
    AnimalEndpoints.kittensPut.serverLogic(kitten => {
      val updatedKittenOpt = DB.kittens.find(_.id == kitten.id).map(_.copy(name = kitten.name, gender = kitten.gender, ageInDays = kitten.ageInDays))
      updatedKittenOpt.map(updatedKitten => {
        DB.kittens = DB.kittens.filterNot(_.id == kitten.id) :+ updatedKitten
        IO(Right(StatusCode.Ok -> updatedKitten))
      }).getOrElse(IO(Left(StatusCode.NotFound -> ErrorResponse(s"kitten with id ${kitten.id} was not found"))))
    })
  )

  private val deleteKittens = Http4sServerInterpreter[IO]().toRoutes(AnimalEndpoints.kittensDelete.serverLogic(
    kittenId => {
      val deletedKittenOpt = DB.kittens.find(_.id == kittenId)
      deletedKittenOpt.map(deletedKitten => {
        DB.kittens = DB.kittens.filterNot(_.id == kittenId)
        IO(Right(StatusCode.Ok -> deletedKitten))
      }).getOrElse(IO(Left(StatusCode.NotFound -> ErrorResponse(s"kitten with id $kittenId was not found"))))
    })
  )

  private val endpointsForDocs = List(helloWorld, AnimalEndpoints.kittens, AnimalEndpoints.kittenPost, AnimalEndpoints.kittensPut, AnimalEndpoints.kittensDelete, AnimalEndpoints.kittensWithPathParam, AnimalEndpoints.kittensWithQueryParam)

  def withSwaggerDocs(endpoints: List[AnyEndpoint]): HttpRoutes[IO] = {
    Http4sServerInterpreter[IO]().toRoutes(SwaggerInterpreter().fromEndpoints[IO](endpoints, "Kittens", "1.0"))
  }

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override def run(args: List[String]): IO[ExitCode] = {
    // starting the server
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(Router("/" -> hello, "/" -> getKitten, "/" -> postKittens, "/" -> putKittens, "/" -> deleteKittens, "/" -> withSwaggerDocs(endpointsForDocs)).orNotFound)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
