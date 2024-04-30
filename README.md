# Overview

Utility application written for my father to quickly get current stock prices. 

This uses the IEX api to fetch quote latest prices. Limitation is the free subscription allows 5 requests per second, so we sleep between batches of 5 stocks.

# Running the app

### Prerequisite - Java 11 installed on windows

Download this repo

Copy contents of `out/artifacts/scalaStocks_jar` to a convenient location on your PC

Paste the value of the api token provided by Sean to `tokenConfig.txt`

Edit `in.csv`, adding one stock symbol per line

Double-click `run.bat`. Command window will open and you'll see the app running. Once done, results will be saved to a new file, `out.csv`.

If you get bad output, close all Excel files and try again. If after 3 bad attempts, contact Sean.