from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.firefox.launch()
    page = browser.new_page()
    page.goto("https://www.infozagreb.hr/en/events", wait_until="networkidle")

    cards = page.query_selector_all(".event-card")
    events = []
    for card in cards:
        card_title = card.query_selector(".title")
        card_date = card.query_selector(".dates")
        card_location = card.query_selector(".location")
        card_link = card.query_selector("a")

        events.append({
            "title": card_title.inner_text().strip() if card_title else None,
            "date": card_date.inner_text().strip() if card_date else None,
            "location": card_location.inner_text().strip() if card_location else None,
            "sourceUrl": card_link.get_attribute("href") if card_link else None,
        })

    for e in events:
        print(e)

    browser.close()