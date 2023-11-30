from web_crawler.manual_crawler.crawler import ManualCrawler
from web_crawler.manual_crawler.seed_pages import generate_seed_pages
import asyncio
"""
 asyncio is used to perform web requests asynchronously. 
 Instead of making a request and waiting for the response (blocking the program's execution), 
 asyncio allows the program to make a request, move on to other tasks while waiting for the response, 
 and then resume the original task once the response is received.

If I've set max_requests=500, my crawler will attempt to make 500 requests.
If the crawler has not finished these requests within the timeout=600 (10 minutes), then any still-pending tasks will be cancelled.
Regardless of whether tasks finish or get cancelled, my logger will save the logs in the finally block.
The crawler won't "finish completely" after the timeout; it will continue with the next steps in my program. 
But any tasks that haven't completed by the timeout will be stopped.
"""

async def handle_crawl_errors(crawler, url):
    """Handle exceptions during crawling."""
    try:
        # request_delay:  preventing my IP from getting blocked
        await crawler.crawl(url, max_depth=5, max_retries=1, max_requests=100000, request_delay=1, max_pages_per_domain=30)
    except Exception as e:
        print(f"Error while crawling {url}: {e}")


async def main():
    # Generate seed pages using Google's Custom Search Engine API
    seed_pages = generate_seed_pages("machine learning vs deep learning")

    # Start the web crawler using the seed pages
    crawler = ManualCrawler()
    # For each URL in the seed pages, it creates an asynchronous task that attempts to crawl that URL using `handle_crawl_errors`
    tasks = [asyncio.create_task(handle_crawl_errors(crawler, url)) for url in seed_pages]

    # Timeout mechanism to ensure that the crawler doesn't get stuck on tasks that take too long.
    # Each batch of tasks 600 seconds(=10 minutes) to complete.
    # If tasks aren't completed within this timeout, they're canceled.
    timeout = 1200
    try:
        completed, pending = await asyncio.wait(tasks, timeout=timeout)

        # Cancel any tasks that didn't complete within the timeout
        for task in pending:
            task.cancel()

    except (Exception, asyncio.CancelledError) as e:
        print(f"Unhandled exception: {e}")

    finally:
        # Save logs after all seed pages have been crawled
        crawler.logger.save_logs()


# Begin the crawling process
if __name__ == "__main__":
    try:
        asyncio.run(main())
    except Exception as e:
        print(f"Error during main execution: {e}")