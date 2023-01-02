package com.scalaStocks.Main

import scala.io.Source
import sttp.client3.*

import scala.util.matching.Regex
import java.io.*
import java.time.Duration
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.util.{Failure, Success}

object Main:
  def main(args: Array[String]): Unit =
    println("Only thing better than watching Greyhound?... BUYING STOCKS MAAAAAN!")

    val stocks = readFile()

    if (stocks.length == 0)
      println("Nothing found in in.csv")

    val sb: StringBuilder = new StringBuilder()

    val futurePrices = Future.sequence(stocks.map(stock => getPriceF(stock)))

    val oneMin = scala.concurrent.duration.Duration(60, "seconds")
    Await.ready(futurePrices, oneMin)

    futurePrices.map { prices =>
      prices.foreach(price =>
        println(price)
        sb ++= price
        sb ++= "\n"
      )

      writeFile(sb.toString())
    }

    Thread.sleep(1000) // give time to write to file

    println("All of your stocks and prices have been written to out.csv")
    println("I hope it's a fortune, Clark.")

  def readFile(): List[String] =
    val bufferedSource = Source.fromFile("in.csv")

    val stocks = bufferedSource.getLines.map(_.trim().toUpperCase()).toList
    bufferedSource.close
    stocks

  def writeFile(data: String): Unit =
    val pw = new PrintWriter(new File("out.csv"))
    pw.write(data)
    pw.close()

  def getPriceF(stock: String): Future[String] =
    println(s"Getting Price for: $stock")
    val url = s"https://finance.yahoo.com/quote/$stock?p=$stock"

    val request = basicRequest.get(uri"https://finance.yahoo.com/quote/$stock?p=$stock")

    val backend = HttpClientFutureBackend()
    val responseF = request.send(backend)

    responseF map { resp =>
      val pat: Regex = """data-pricehint="2" value="\d+.\d+""".r
      val matchOpt = pat.findFirstMatchIn(resp.body.getOrElse(""))
      matchOpt match
        case Some(value) =>
          value.group(0).split("value=\"").last
        case _ => "no price found (yahoo html might have changed)"
    } map { price =>
      s"$stock,$price"
    }
