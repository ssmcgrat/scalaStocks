package com.scalaStocks.Main

import scala.io.Source
import sttp.client3._
import scala.util.matching.Regex
import java.io._

object Main:
  def main(args: Array[String]): Unit =
    println("Only thing better than watching Greyhound?... BUYING STOCKS MAAAAAN!")

    val stocks = readFile()

    if (stocks.length == 0)
      println("Nothing found in in.csv")

    val sb: StringBuilder = new StringBuilder()
    stocks.foreach(stock => {
      val price = getPrice(stock)
      val line = s"$stock,$price"

      println(line)
      sb ++= line
      sb ++= "\n"
    })

    writeFile(sb.toString())

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

  def getPrice(stock: String): String =
    println(s"Getting Price for: $stock")
    val url = s"https://finance.yahoo.com/quote/$stock?p=$stock"

    val request = basicRequest.get(uri"https://finance.yahoo.com/quote/$stock?p=$stock")

    val backend = HttpClientSyncBackend()
    val response = request.send(backend)

    val pat: Regex = """data-pricehint="2" value="\d+.\d+""".r

    val matchOpt = pat.findFirstMatchIn(response.body.getOrElse(""))

    matchOpt match
      case Some(value) =>
        value.group(0).split("value=\"").last
      case _ => "no price found (yahoo html might have changed)"

