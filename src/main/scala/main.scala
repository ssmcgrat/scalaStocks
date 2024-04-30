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
    val iexClient = new IexClient()

    val batches = stocks.grouped(5)

    var shouldSleep = false

    batches.foreach(batch => {
      if (shouldSleep) {
        Thread.sleep(1001)
      }

      shouldSleep = true
      println("Getting prices for: " + batch)

      val allBatchF = for {
        prices <- Future.sequence(batch.map(stock => iexClient.getQuote(stock)))
        _ = prices.foreach(price =>
          println(price)
          sb ++= price
          sb ++= "\n"
        )
//        written = writeFile(sb.toString())
      } yield ()

      val oneMin = scala.concurrent.duration.Duration(60, "seconds")
      Await.ready(allBatchF, oneMin)
    })
    writeFile(sb.toString())
    Thread.sleep(500)

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
