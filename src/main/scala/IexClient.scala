package com.scalaStocks.Main

import sttp.client3.{HttpClientFutureBackend, UriContext, basicRequest}

import scala.concurrent.Future
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global

import ujson._

class IexClient {

  val token = "pk_1f5c527a76a94ab28866ea4454a7dd87"

  def getQuote(quote: String): Future[String] = {
    val request = basicRequest.get(uri"https://api.iex.cloud/v1/data/CORE/QUOTE/$quote?token=$token")

    val backend = HttpClientFutureBackend()
    val responseF = request.send(backend)

    responseF map { resp =>
      val json: ujson.Value = ujson.read(resp.body.getOrElse(""))

      // parse the first index of json array, attribute "foo"
      val stock = json(0)
      stock("latestPrice").toString
    } map { price =>
      s"$quote,$price"
    }

  }
}
