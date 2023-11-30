import ast
import json
import os

# File path
base_directory = os.path.abspath("/Users/sammita/Projects/nyu-grad/CS-GY 6913 Web Search Engines/web-search-engines/web_crawler/scrapy_crawler")
output_json_path = os.path.join(base_directory, 'crawl/crawl.json')
output_txt_path = os.path.join(base_directory, 'crawl/statistics.txt')

# Initialize variables to store link counts
total_links_count = 0
external_links_count = 0
internal_links_count = 0
current_page_external_count = 0
current_page_internal_count = 0

# Open and read the file
with open(output_json_path, 'r') as file:
    content = file.read().strip()

    # Ensure the content is wrapped in a list
    if not content.startswith('['):
        content = '[' + content + ']'

    # Convert the string content to a list of dictionaries
    # data_list = ast.literal_eval(content)
    data_list = json.loads(content)

    # Process each entry
    for data in data_list:

        # Calculate total links
        total_links_count += 1 + len(data["found_links"])

        # Count External and Internal links for current_page
        if data["current_page"]["link_type"] == "External":
            external_links_count += 1
            current_page_external_count += 1
        else:
            internal_links_count += 1
            current_page_internal_count += 1

        # Count External and Internal links for found_links
        for link in data["found_links"]:
            if link["Link Type"] == "External":
                external_links_count += 1
            else:
                internal_links_count += 1

# Preparing the statistics
statistics = [
    f"Total links: {total_links_count}",
    f"External links: {external_links_count}",
    f"Internal links: {internal_links_count}",
    f"Link type of current pages - External: {current_page_external_count}, Internal: {current_page_internal_count}"
]

# Printing and writing the statistics to a file
with open(output_txt_path, 'w') as stats_file:
    for stat in statistics:
        print(stat)
        stats_file.write(stat + '\n')