import json
import ast
import os


def parse_json_string(s):
    try:
        return json.loads(s)
    except json.JSONDecodeError:
        return ast.literal_eval(s)

# Paths to the log file and output file
base_directory = os.path.abspath("/Users/sammita/Projects/nyu-grad/CS-GY 6913 Web Search Engines/web-search-engines/web_crawler/scrapy_crawler")
log_file_path = os.path.join(base_directory, 'logfile.json')
output_file_path = os.path.join(base_directory, 'crawl/crawl.json')

info_data = []

with open(log_file_path, 'r') as log_file:
    for line in log_file:
        if "[mycrawler] INFO:" in line:
            json_start = line.index("{")
            json_part = line[json_start:]
            try:
                entry = parse_json_string(json_part.strip())
                info_data.append(entry)
            except (json.JSONDecodeError, ValueError) as e:
                print(f"Error parsing line: {line}\nError: {e}")


# Write the filtered data to crawl.json
with open(output_file_path, 'w') as output_file:
    json.dump(info_data, output_file, indent=4)

print(f"Extracted {len(info_data)} INFO messages and saved to {output_file_path}.")