package com.pankaj.tapir.client

import com.pankaj.tapir.endpoints.{AnimalEndpoints, ErrorResponse, Kitten}
import sttp.client3.{HttpClientSyncBackend, Identity, Request, RequestT, UriContext, asStringAlways}
import sttp.model.StatusCode
import sttp.tapir.DecodeResult
import sttp.tapir.client.sttp.SttpClientInterpreter

object GetClient extends App {
  private val kittenRequest: Unit => Request[DecodeResult[Either[String, List[Kitten]]], Any] =
    SttpClientInterpreter().toRequest(AnimalEndpoints.kittens, Some(uri"http://localhost:8080"))

  private val backend = HttpClientSyncBackend()
  private val kittensR: RequestT[Identity, String, Any] = kittenRequest().response(asStringAlways)
  private val kittensResponse = kittensR.send(backend)
  println(s"kittens:${kittensResponse.body}")
}

object ClientPost extends App {
  private val kittenPostRequest: Kitten => Request[DecodeResult[Either[(StatusCode, ErrorResponse), (StatusCode, Kitten)]], Any] =
    SttpClientInterpreter().toRequest(AnimalEndpoints.kittenPost, Some(uri"http://localhost:8080"))

  private val backend = HttpClientSyncBackend()
  private val chaseR: RequestT[Identity, String, Any] = kittenPostRequest(Kitten(12L, "chase", "male", 14)).response(asStringAlways)
  private val chaseResponse = chaseR.send(backend)
  println(s"chase: ${chaseResponse.body}")
}
