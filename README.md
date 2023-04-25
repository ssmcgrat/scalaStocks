# Overview

Utility application written for my father to quickly get current stock prices. 

This is very brittle... just scrapping yahoo finance html, so if that changes this will need to change. 

Ideally would query an api... but all the free ones only support stocks, not ETFs and Mutual Funds... maybe some day.

# Running the app

### Prerequisite - Java 11 installed on windows

Download this repo

Copy contents of `out/artifacts/scalaStocks_jar` to a convenient location on your PC

Edit `in.csv`, adding one stock symbol per line

Double-click `run.bat`. Command window will open and you'll see the app running. Once done, results will be saved to a new file, `out.csv`.

If you start seeing output for one or more stocks that "no price was found", we likely need to update the regex pattern which scrapes the stock price from
the raw HTML. Copy the text within `out/artifacts/scalaStocks_jar/regex.conf` into your local regex.conf file and try again.