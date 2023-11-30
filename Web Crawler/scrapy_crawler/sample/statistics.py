import json
import os

stats = {}
collecting = False

current_directory = os.path.dirname(os.path.abspath(__file__))
base_directory = os.path.abspath("/Users/sammita/Projects/nyu-grad/CS-GY 6913 Web Search Engines/web-search-engines/web_crawler/scrapy_crawler")
log_file_path = os.path.join(base_directory, 'logfile.json')
stats_json_path = os.path.join(current_directory, 'statistics.json')
stats_txt_path = os.path.join(current_directory, 'statistics.txt')

with open(log_file_path, 'r') as f:
    for line in f:
        line = line.strip()

        if "INFO: Dumping Scrapy stats:" in line:
            collecting = True
            continue
        if collecting:
            if 'start_time' in line:
                break

            line = line.replace("Unparsed line: ", "").replace("'", "\"")
            if ":" in line:
                if "{\"downloader" in line:
                    line = line.replace("{\"", "")
                key, value = line.split(":", 1)
                key = key.strip("\" ")
                value = value.strip(",\" ")

                if "datetime.datetime" in value:
                    stats[key] = value
                else:
                    try:
                        stats[key] = json.loads(value)
                    except json.JSONDecodeError:
                        stats[key] = value

with open(stats_json_path, "w") as out_file:
    out_file.write(json.dumps(stats, indent=4))

print("Extracted stats have been saved to statistics.json.")
print("You can find meaningful statistics on statistics.txt\n")
print("[Summary]")

# Only define core keys to extract, handle response_status_count dynamically
keys_to_extract = [
    "downloader/response_count",
    "downloader/response_bytes",
    "elapsed_time_seconds"
]

with open(stats_json_path, "r") as json_file:
    data = json.load(json_file)

with open(stats_txt_path, "w") as txt_file:
    for key in keys_to_extract:
        print(f"{key}: {data[key]}")
        txt_file.write(f"{key}: {data[key]}\n")
        if key == "elapsed_time_seconds":
            minutes = int(data[key] // 60)
            seconds = data[key] % 60
            print(f"elapsed_time_minutes: {minutes}m {seconds:.2f}s")
            txt_file.write(f"elapsed_time_minutes: {minutes}m {seconds:.2f}s\n")

    # Check and extract downloader/response_status_count dynamically
    for key in data:
        if key.startswith("downloader/response_status_count"):
            print(f"{key}: {data[key]}")
            txt_file.write(f"{key}: {data[key]}\n")