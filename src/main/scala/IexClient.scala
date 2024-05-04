package com.scalaStocks.Main

import java.time._
import scala.concurrent.Future
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global

import sttp.client3.{HttpClientFutureBackend, UriContext, basicRequest}

class IexClient(token: String) {

  private val isMarketOpen: Boolean = determineIfMarketIsOpen()
  def determineIfMarketIsOpen(): Boolean = {
    // Get the current time in the UTC time zone
    val utcZoneId = ZoneId.of("UTC")
    val zonedDateTime = ZonedDateTime.now
    val utcDateTime = zonedDateTime.withZoneSameInstant(utcZoneId)

    zonedDateTime.getDayOfWeek.name() match {
      case "SATURDAY" => false
      case "SUNDAY" => false
      case _ => {
        // Extract hours and minutes
        val hours = utcDateTime.getHour
        val minutes = utcDateTime.getMinute

        // Market is open between 13:30 - 20:30 UTC
        if (hours < 13 || hours > 20) {
          false
        } else if (hours == 13 && minutes < 30) {
          false
        } else if (hours == 20 && minutes > 30) {
          false
        } else {
          true
        }
      }
    }
  }

  def getPriceAttributeBasedOnMarketBeingOpen(): String = {
    if (isMarketOpen) {
      "latestPrice"
    } else {
      "iexClose"
    }
  }

  def getQuote(quote: String): Future[String] = {
    val uriStr = uri"https://api.iex.cloud/v1/data/CORE/QUOTE/$quote?token=$token"
    val request = basicRequest.get(uriStr)

    val backend = HttpClientFutureBackend()
    val responseF = request.send(backend)

    responseF map { resp =>
      val json: ujson.Value = ujson.read(resp.body.getOrElse(""))

      // parse the first index of json array, attribute "foo"
      val stock = json(0)
      var price = stock(getPriceAttributeBasedOnMarketBeingOpen()).toString

      if (price == "null") { // default to latest price, ETFs don't have "iexClose" attribute
        price = stock("latestPrice").toString
      }
      price
    } map { price =>
      s"$quote,$price"
    }
  }
}
