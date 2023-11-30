import datetime
from langdetect import detect
from scrapy.spiders import CrawlSpider, Rule
from scrapy.linkextractors import LinkExtractor
from scrapy.http import Response, Request
from bs4 import BeautifulSoup
from urllib.parse import urlparse, urlunparse
from collections import defaultdict
from scrapy import Item, Field
from .seed_pages import generate_seed_pages
class CrawledItem(Item):
    link = Field()
    link_type = Field()
    time_crawled = Field()
    size = Field()
    return_code = Field()
    language = Field()
    title = Field()
    keywords = Field()
    description = Field()
def is_external(base, next_url):
    if not next_url:  # Checking for None URLs
        return False
    return urlparse(base).netloc != urlparse(next_url).netloc

def clean_url(url):
    # Removing fragments
    scheme, netloc, path, params, query, fragment = urlparse(url)
    cleaned_url = urlunparse((scheme, netloc, path, params, query, ''))
    return cleaned_url

class CustomCrawler(CrawlSpider):
    name = "mycrawler"

    def __init__(self, search_query='', *args, **kwargs):
        super(CustomCrawler, self).__init__(*args, **kwargs)
        self.start_urls = generate_seed_pages(search_query)

    # rules = (
    #     Rule(LinkExtractor(), callback='parse_item', follow=True),
    # )
    def start_requests(self):
        for url in self.start_urls:
            yield Request(url, callback=self.parse_item)

    domain_count = defaultdict(int)
    DOMAIN_LIMIT = 50  # Set a limit for pages per domain

    def parse_item(self, response: Response):
        # Use BeautifulSoup to parse the page
        soup = BeautifulSoup(response.text, 'lxml')

        # Getting the details
        current_link = response.url
        link_type = "Internal"  # Default value
        time_crawled = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        size = len(response.body)  # size in bytes
        return_code = response.status

        # Extracting title
        title = soup.title.string if soup.title else ""

        # Extracting keywords from meta (if available)
        keywords_meta = soup.find('meta', attrs={"name": "keywords"})
        keywords = keywords_meta["content"] if keywords_meta and "content" in keywords_meta.attrs else ""

        # Extracting description from meta (if available)
        description_meta = soup.find('meta', attrs={"name": "description"})
        description = description_meta["content"] if description_meta and "content" in description_meta.attrs else ""

        # Detecting language (library doesn't work well)
        # try:
        #     language = detect(response.text)
        # except:
        #     language = "unknown"

        # Extract the 'lang' attribute from the <html> tag
        language = soup.html.get('lang', 'unknown')

        # Increment domain count for the current domain
        self.domain_count[urlparse(response.url).netloc] += 1

        # Process links on the page
        found_links = []  # Store the links found on this page
        for a_tag in soup.find_all('a', href=True):
            next_url = response.urljoin(a_tag['href'])  # Canonicalizing URLs
            next_url = clean_url(next_url) # Removing fragments

            # Skip javascript: or other non-http/https URLs
            if not (next_url.startswith('http://') or next_url.startswith('https://')):
                continue

            next_domain = urlparse(next_url).netloc
            priority = 1

            # If the next domain hasn't exceeded its limit or it's an external domain
            if is_external(response.url, next_url):
                link_type = "External"
                priority = 10

            link_info = {
                "Link Type": "External" if is_external(response.url, next_url) else "Internal",
                "Link": next_url,
            }
            found_links.append(link_info)

            if self.domain_count[next_domain] < self.DOMAIN_LIMIT or is_external(response.url, next_url):
                yield Request(next_url, self.parse_item, priority=priority)

        current_page_info = {
            "link": current_link,
            "link_type": link_type,
            "time_crawled": time_crawled,
            "size": size,
            "return_code": return_code,
            "language": language,
            "title": title,
            "keywords": keywords,
            "description": description
        }

        self.logger.info({
            "current_page": current_page_info,
            "found_links": found_links
        })

        # Saving the details
        item = CrawledItem()
        item['link'] = current_link
        item['link_type'] = link_type
        item['time_crawled'] = time_crawled
        item['size'] = size
        item['return_code'] = return_code
        item['language'] = language
        item['title'] = title
        item['keywords'] = keywords
        item['description'] = description
        yield item
