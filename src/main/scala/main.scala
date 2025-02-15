package com.scalaStocks.Main

import scala.io.Source
import sttp.client3.*

import java.io.*
import java.time._
import java.util.Date
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.sys.exit
import scala.util.{Failure, Success}

object Main:
  def main(args: Array[String]): Unit =
    println("Only thing better than watching Greyhound?... BUYING STOCKS MAAAAAN!")

    val quotesPerSecond = 50 // send up to 50 requests in parallel

    val stocks = readFile("in.csv")

    if (stocks.length == 0)
      println("Nothing found in in.csv")

    val sb: StringBuilder = new StringBuilder()

    val token = readFile("tokenConfig.txt", false)

    if (token.isEmpty || token.head.isEmpty) {
      println("No api token found in config file, contact Sean for help man.")
      exit()
    }

    val quoteClient = new FMPClient(token.head.trim)

    val batches = stocks.grouped(quotesPerSecond)

    println(s"Fetching stock prices in batches of $quotesPerSecond. Please allow ${stocks.grouped(quotesPerSecond).length} seconds...")

    var shouldSleep = false

    var allPrices: List[String] = List()

    batches.foreach(batch => {
      if (shouldSleep) {
        Thread.sleep(1000) // sleep one sec to accommodate our free api subscription.
      }

      shouldSleep = true
      println("Getting prices for: " + batch)

      val allPricesInBatchF = for {
        prices <- Future.sequence(batch.map(stock => quoteClient.getQuote(stock)))
      } yield (prices)

      val oneMin = scala.concurrent.duration.Duration(60, "seconds")
      val allPricesInBatch = Await.result(allPricesInBatchF, oneMin)

      allPrices ++= allPricesInBatch
    })

    allPrices.foreach(price => {
      sb ++= price
      sb ++= "\n"
    })

    println(sb.toString())
    writeFile(sb.toString())
    Thread.sleep(500) // just to ensure the file io completes.

    println("All of your stocks and prices have been written to out.csv")
    println("I hope it's a fortune, Clark.")

  def readFile(filename: String, toUpper: Boolean = true): List[String] =
    val bufferedSource = Source.fromFile(filename)

    val stocks = bufferedSource.getLines.map(line => {
      if (toUpper) {
        line.trim().toUpperCase()
      } else {
        line.trim
      }
    }).toList
    bufferedSource.close
    stocks

  def writeFile(data: String): Unit =
    val pw = new PrintWriter(new File("out.csv"))
    pw.write(data)
    pw.close()
