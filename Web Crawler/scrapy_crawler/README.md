> Every command should be run in provided order - from Install to Statistics
# Install
```shell
$ pip3 install python-json-logger
$ pip3 install lxml
$ pip3 install beautifulsoup4
$ pip3 install scrapy
```
# Crawler
```shell
$ cd scrapy_crawler
$ scrapy crawl mycrawler -a search_query="your_search_term_here" -O sample/sample.json
```
## Total log file
/scrapy_crawler/logfile.json

## Sample log file
/scrapy_crawler/sample/sample.json

# Statistics
## Crawled page
```shell
$ python3 crawl/link_parser.py
$ python3 crawl/statistics.py
```
## Sampled page
```shell
#/scrapy_crawler
$ python3 sample/statistics.py
$ python3 sample/language.py
```

### JSON Formatter
```shell
# PyCharm
opt + cmd + L
```