import traceback

import aiohttp
import asyncio
from bs4 import BeautifulSoup
from urllib.parse import urlparse, urljoin
from urllib.robotparser import RobotFileParser
from collections import deque, defaultdict
import time
from logger import Logger, simplify_lang_code


# Send multiple requests in parallel
async def fetch(session, url):
    # Takes in a session object and URL and fetches the URL asynchronously
    # Returns the text content of the fetched page and the HTTP status code
    async with session.get(url, timeout=5, ssl=False) as response:
        return await response.text(), response.status


# Function to extract the TLD from a URL
def extract_tld(url):
    parsed_uri = urlparse(url)
    tld = parsed_uri.netloc.split('.')[-1]
    return tld


# Internal vs External
def extract_domain(url):
    """
    Extract the main domain name server from a URL.
    For example:
    'www.en.abc.com' -> 'abc.com'
    'www.abc.com:8080' -> 'abc.com'
    'sub.sub2.abc.com/path/to/resource' -> 'abc.com'
    """
    parsed_uri = urlparse(url)
    domain = '{uri.netloc}'.format(uri=parsed_uri).split('.')
    return '.'.join(domain[-2:])


class ManualCrawler:
    def __init__(self):
        self.visited = set()
        self.robots_cache = {}
        self.logger = Logger()

    def check_robots(self, url):
        """Check if a URL can be fetched using robots.txt rules."""
        parsed_url = urlparse(url)
        base_url = f"{parsed_url.scheme}://{parsed_url.netloc}"
        robot_url = urljoin(base_url, "/robots.txt")

        if robot_url not in self.robots_cache:
            rp = RobotFileParser()
            rp.set_url(robot_url)
            try:
                rp.read()
            except:
                return True  # Allow crawling if there's an issue reading robots.txt
            self.robots_cache[robot_url] = rp
        else:
            rp = self.robots_cache[robot_url]

        return rp.can_fetch("*", url)

    async def crawl(self, start_url, max_depth=3, max_retries=3, max_requests=1000, request_delay=3,
                    max_pages_per_domain=10):
        """DFS crawl starting from the provided URL."""
        stack = deque([(start_url, 0)])  # (URL, depth)
        request_count = 0
        last_domain = None  # Track the domain of the last URL fetched
        pages_per_domain = defaultdict(int)

        while stack:
            if request_count >= max_requests:
                print(f"Reached the maximum number of requests: {max_requests}")
                break

            current_url, depth = stack.pop()
            current_tld = extract_tld(current_url)  # Extract TLD of the current URL

            # Introduce delay only for the same domain
            # Wait between consecutive requests to the same domain
            current_domain = extract_domain(current_url)
            if last_domain == current_domain:
                time.sleep(request_delay)
            last_domain = current_domain  # Update the domain of the last URL fetched

            # Skip javascript: or other non-http/https URLs
            if not (current_url.startswith('http://') or current_url.startswith('https://')):
                continue

            if current_url in self.visited or depth > max_depth:
                continue

            if not self.check_robots(current_url):
                print(f"Blocked by robots.txt: {current_url}")
                continue

            self.visited.add(current_url)
            retries = 0
            success = False

            async with aiohttp.ClientSession() as session:
                while retries < max_retries and not success:
                    try:
                        page_text, status_code = await fetch(session, current_url)

                        # Delay between requests to the same domain
                        # Adjust delay based on the status code
                        if status_code == 429:
                            request_delay += 1  # increase delay by 1 second
                        elif status_code in [500, 503]:
                            request_delay += 0.5  # increase delay by half a second
                        elif status_code == 200 and request_delay > 1:  # if everything is okay and delay is more than 1 second
                            request_delay -= 0.1  # decrease delay slightly

                        # Parse url for links
                        soup = BeautifulSoup(page_text, "html.parser")
                        lang_code = soup.html.get('lang', 'unknown')
                        a_tags = soup.find_all('a', href=True)

                        # Extract href attributes
                        links = [urljoin(current_url, tag['href']) for tag in a_tags]
                        # Prioritize external links
                        for link in links:
                            link_tld = extract_tld(link)  # Extract TLD of the link
                            if link not in self.visited:
                                link_domain = extract_domain(link)
                                current_domain = extract_domain(current_url)
                                if pages_per_domain[link_domain] < max_pages_per_domain:  # Check if we can still crawl this domain
                                    if link_domain != current_domain:
                                        # External link, append to front
                                        link_type = "External link"
                                        stack.appendleft((link, depth + 1))
                                    elif link_tld != current_tld:  # Different TLD, prioritize
                                        link_type = "Different TLD link"
                                        stack.appendleft((link, depth + 1))
                                    else:
                                        # Internal link, append to back
                                        link_type = "Internal link"
                                        stack.append((link, depth + 1))
                                    pages_per_domain[link_domain] += 1  # Update the pages count for the domain

                                    print(f"Found {link_type}: {link}")
                                    self.logger.add_encountered_link(current_url, link, link_type)

                        print(f"Crawled: {current_url}")
                        # lang_code = "undefined"
                        simplified_lang = simplify_lang_code(lang_code)
                        self.logger.lang_counts[simplified_lang] += 1
                        self.logger.add_sampled_link(current_url, page_text, status_code, simplified_lang)

                        # If the fetch is successful, a delay comes in before moving to the next URL
                        if request_count < max_requests:
                            await asyncio.sleep(request_delay)
                        success = True
                        request_count += 1
                    except Exception as e:
                        print(f"Retry {retries + 1} for URL: {current_url}.")
                        print("Error type:", type(e))
                        print("Error:", e)
                        print("Full Traceback:")
                        print(traceback.format_exc())
                        retries += 1
                        continue

            if not success:
                print(f"Failed to crawl after {max_retries} retries: {current_url}")
                continue  # Move on to the next URL in the stack
