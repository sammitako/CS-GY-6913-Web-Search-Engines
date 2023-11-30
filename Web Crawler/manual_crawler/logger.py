import json
from collections import defaultdict
import time
from langdetect import detect

# For sample.json -> time crawled, link type, size, return_code, language, and other description for later use.

def simplify_lang_code(lang_code):
    """Extract the primary language from a BCP-47 tag."""
    return lang_code.split('-')[0].lower()


class Logger:
    def __init__(self):
        self.encountered_links = defaultdict(list)
        self.sampled_links = []
        self.lang_counts = defaultdict(int)
        self.total_size = 0
        self.total_time_start = time.time()
        self.http_errors = defaultdict(int)

    def add_encountered_link(self, current_url, found_link, link_type):
        self.encountered_links[current_url].append({"link": found_link, "type": link_type})

    def add_sampled_link(self, url, page_content, status_code, lang):
        if lang is None:
            try:
                # Detect the language of the page content
                lang = detect(page_content)
            except:
                lang = "undefined"  # If detection fails

        simplified_lang = simplify_lang_code(lang)
        self.lang_counts[simplified_lang] += 1  # Note the use of simplified_lang here

        self.sampled_links.append(url)
        self.total_size += len(page_content)
        if status_code >= 400:
            self.http_errors[status_code] += 1

    def save_logs(self):
        # Save encountered links
        all_links = []
        for current_url, found_links in self.encountered_links.items():
            all_links.append({"current_url": current_url, "found_links": found_links})

        with open("log.json", "w") as f:
            json.dump(all_links, f, indent=4)

        # Save sampled links
        # Calculate language percentages and other stats
        total_langs = sum(self.lang_counts.values())
        lang_percentage = {
            "English": (self.lang_counts.get("en", 0) / total_langs) * 100 if total_langs else 0,
            "Chinese": (self.lang_counts.get("zh", 0) / total_langs) * 100 if total_langs else 0,
            "Polish": (self.lang_counts.get("pl", 0) / total_langs) * 100 if total_langs else 0,
            "Unknown": (self.lang_counts.get("unknown", 0) / total_langs) * 100 if total_langs else 0
        }

        # Flatten the encountered links
        flattened_encountered = [link_data['link'] for _, links in self.encountered_links.items() for link_data in
                                 links]
        # Combine with sampled links and remove duplicates
        unique_links = set(flattened_encountered + self.sampled_links)

        log_summary = {
            "Total Links": len(unique_links),
            "Total Crawled": len(self.sampled_links),
            "Total Size": self.total_size,
            "Total Time": time.time() - self.total_time_start,
            "HTTP Errors": dict(self.http_errors),
            "Language Percentage": lang_percentage
        }

        sample_data = {
            "sampled_links": self.sampled_links,
            "summary": log_summary
        }

        with open("sample.json", "w") as f:
            json.dump(sample_data, f, indent=4)

        # Print out the final log
        print()
        print(log_summary)