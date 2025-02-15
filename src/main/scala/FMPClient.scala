package com.scalaStocks.Main

import java.time._
import scala.concurrent.Future
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global

import sttp.client3.{HttpClientFutureBackend, UriContext, basicRequest}

class FMPClient(token: String) {
  // https://site.financialmodelingprep.com/developer/docs
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

  def getQuote(quote: String): Future[String] = {
    val uriStr = uri"https://financialmodelingprep.com/api/v3/quote/$quote?apikey=$token"
    val request = basicRequest.get(uriStr)

    val backend = HttpClientFutureBackend()
    val responseF = request.send(backend)

    responseF map { resp =>
      val json: ujson.Value = ujson.read(resp.body.getOrElse(""))
      try {
        val stock = json(0)
        val price = stock("price").toString
        price
      } catch {
        case exception: Exception => s"price not found"
      }
    } map { price =>
      s"$quote,$price"
    }
  }
}
