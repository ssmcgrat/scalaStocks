package com.scalaStocks.Main

import scala.concurrent.Future
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global

import sttp.client3.{HttpClientFutureBackend, UriContext, basicRequest}

class IexClient(token: String) {
  def getQuote(quote: String): Future[String] = {
    val uriStr = uri"https://api.iex.cloud/v1/data/CORE/QUOTE/$quote?token=$token"
    val request = basicRequest.get(uriStr)

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
