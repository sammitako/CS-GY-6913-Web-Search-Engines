import requests


def generate_seed_pages(search_query):
    API_KEY = "AIzaSyD0HhdPAtKW8N2_WMkqtBZzB1Ttm7EQ91s"
    SEARCH_ENGINE_ID = "51dbfb6f888134f07"

    url = 'https://www.googleapis.com/customsearch/v1'
    params = {
        'q': search_query,
        'key': API_KEY,
        'cx': SEARCH_ENGINE_ID
    }

    response = requests.get(url, params=params)
    results = response.json()

    seed_links = []
    if 'items' in results:
        for item in results['items']:
            seed_links.append(item['link'])

    return seed_links
