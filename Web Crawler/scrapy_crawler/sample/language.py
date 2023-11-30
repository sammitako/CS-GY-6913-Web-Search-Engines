import json
import os
from collections import defaultdict

# Paths and initialization
current_directory = os.path.dirname(os.path.abspath(__file__))
sample_path = os.path.join(current_directory, 'sample.json')
stats_path = os.path.join(current_directory, 'statistics.txt')

# Parse language from JSON
with open(sample_path, 'r') as file:
    data = json.load(file)

# Count languages
language_counts = defaultdict(int)
for entry in data:
    lang = entry.get('language', 'unknown').split('-')[0]
    language_counts[lang] += 1

# Calculate percentage
total_entries = len(data)
language_percentages = {lang: (count/total_entries)*100 for lang, count in language_counts.items()}

# Prepare new stats
new_stats = [
    f"Spanish (es): {language_percentages.get('es', 0):.2f}%",
    f"Chinese (zh): {language_percentages.get('zh', 0):.2f}%",
    f"Polish (pl): {language_percentages.get('pl', 0):.2f}%",
    f"English (en): {language_percentages.get('en', 0):.2f}%",
]
new_stats.extend([f"{lang}: {percentage:.2f}%" for lang, percentage in language_percentages.items()])

# Load old stats, overwrite language stats and save
with open(stats_path, 'r') as f:
    old_stats = f.read().splitlines()  # splitlines will split by newline but not include them

# Filter out lines matching our language patterns
old_stats = [line for line in old_stats if not any(ns in line for ns in new_stats)]

# Add the new stats to the end
old_stats.extend(new_stats)

# Save back to file
with open(stats_path, 'w') as f:
    f.write("\n".join(old_stats))

print("\n".join(new_stats))