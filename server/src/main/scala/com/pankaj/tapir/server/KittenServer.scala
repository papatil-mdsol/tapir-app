package com.pankaj.tapir.server

import com.pankaj.tapir.endpoints.{AnimalEndpoints, ErrorResponse, Kitten}
import com.pankaj.tapir.server.{Database => DB}
import sttp.model.StatusCode
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object KittenServer extends App with BaseAkkaServer {


  private val getKittens = AkkaHttpServerInterpreter()
    .toRoute(
      AnimalEndpoints
        .kittens
        .serverLogic(_ => Future.successful[Either[String, List[Kitten]]](Right(DB.kittens))))

  private val postKittens = AkkaHttpServerInterpreter().toRoute {
    AnimalEndpoints.kittenPost.serverLogic(kitten => {
      if (kitten.id <= 0) {
        Future.successful(Left(StatusCode.BadRequest -> ErrorResponse("negative ids are not accepted")))
      } else {
        if (DB.kittens.exists(_.id == kitten.id)) {
          Future.successful(Left(StatusCode.BadRequest -> ErrorResponse(s"kitten with ud ${kitten.id} already exists")))
        } else {
          DB.kittens = DB.kittens :+ kitten
          Future.successful(Right(StatusCode.Ok -> kitten))
        }
      }
    })
  }

  private val putKittens = AkkaHttpServerInterpreter().toRoute(
    AnimalEndpoints.kittensPut.serverLogic(kitten => {
      val updatedKittenOpt = DB.kittens.find(_.id == kitten.id).map(_.copy(name = kitten.name, gender = kitten.gender, ageInDays = kitten.ageInDays))
      updatedKittenOpt.map(updatedKitten => {
        DB.kittens = DB.kittens.filterNot(_.id == kitten.id) :+ updatedKitten
        Future.successful(Right(StatusCode.Ok -> updatedKitten))
      }).getOrElse(Future.successful(Left(StatusCode.NotFound -> ErrorResponse(s"kitten with id ${kitten.id} was not found"))))
    })
  )

  private val deleteKittens = AkkaHttpServerInterpreter().toRoute(AnimalEndpoints.kittensDelete.serverLogic(
    kittenId => {
      val deletedKittenOpt = DB.kittens.find(_.id == kittenId)
      deletedKittenOpt.map(deletedKitten => {
        DB.kittens = DB.kittens.filterNot(_.id == kittenId)
        Future.successful(Right(StatusCode.Ok -> deletedKitten))
      }).getOrElse(Future.successful(Left(StatusCode.NotFound -> ErrorResponse(s"kitten with id $kittenId was not found"))))
    })
  )

  private val endpointsForDocs = List(AnimalEndpoints.kittens, AnimalEndpoints.kittenPost, AnimalEndpoints.kittensPut, AnimalEndpoints.kittensDelete, AnimalEndpoints.kittensWithPathParam, AnimalEndpoints.kittensWithQueryParam)

  start(Seq(getKittens, postKittens, putKittens, deleteKittens, withSwaggerDocs(endpointsForDocs)))
}


