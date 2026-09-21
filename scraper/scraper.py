from playwright.sync_api import sync_playwright
import json
import re

CITIES = {
    "zagreb": {
        "url": "https://www.infozagreb.hr/hr/dogadanja",
        "base_url": "https://www.infozagreb.hr",
        "card_selector": ".event-card",
        "title_selector": ".title",
        "date_selector": ".dates",
        "location_selector": ".location",
        "link_selector": "a",
    },
    "varazdin": {
        "url": "https://varazdin.events/",
        "base_url": None,
        "card_selector": ".bg-white.rounded-xl.overflow-hidden.flex.flex-col",
        "title_selector": "h3.font-bold.leading-snug",
        "date_selector": ".shrink-0.text-center.relative.z-10",
        "location_selector": "span.truncate",
        "link_selector": "a.mt-3.text-xs.font-semibold",
    },
    "bjelovar": {
        "url": "https://www.bjelovar.hr/dogadjaji/",
        "base_url": None,
        "card_selector": "article.post.lsvr_event",
        "title_selector": "h3.post__title, h2.post__title",
        "date_selector": "p.post-info__date",
        "location_selector": "span.post-info__location",
        "link_selector": "a.post__title-link",
    },
    "osijek": {
        "url": "https://osijek.in/dogadaji/kategorija/sva-dogadanja/popis/",
        "base_url": None,
        "card_selector": "li.tribe-events-calendar-list__event-row",
        "title_selector": "a.tribe-events-calendar-list__event-title-link",
        "date_selector": "time.tribe-events-calendar-list__event-datetime",
        "location_selector": "span.tribe-events-calendar-list__event-venue-title",
        "link_selector": "a.tribe-events-calendar-list__event-title-link",
    },
    "pula": {
        "url": "https://pulainfo.hr/hr/pula-events/",
        "base_url": None,
        "card_selector": ".col-sm-6.col-lg-4.col-xl-3.col-reset",
        "title_selector": ".color-caption h2",
        "date_selector": ".color-caption p[style='margin-bottom:0; float:left;']",
        "location_selector": ".color-caption p.box-location",
        "link_selector": "a",
    },
    "rijeka": {
        "url": "https://visitrijeka.hr/dogadanja",
        "base_url": "https://visitrijeka.hr",
        "card_selector": ".view-grid-item",
        "title_selector": "h3.nc-font-sssb.text-center",
        "date_selector": "p.nc-font-sssb.type.mb-3.pt-3.text-center",
        "location_selector": None,
        "link_selector": "a.text-dark",
    },
    "zadar": {
        "url": "https://zadar.travel/hr/dogadaji/",
        "base_url": None,
        "card_selector": "article.a-item",
        "title_selector": "a.a-item__title, h2.a-item__title, h3.a-item__title",
        "date_selector": ".a-item__details__item:has(.icon-calendar) p",
        "location_selector": ".a-item__details__item.curs-pointer p",
        "link_selector": "a.a-item__details__item",
    },
    "split": {
        "url": "https://split.hr/kalendar/en/city-split-calendar",
        "base_url": None,
        "card_selector": "article.l-item",
        "title_selector": "p.o-title",
        "date_selector": ".o-hour",
        "location_selector": ".o-location",
        "link_selector": "a.o-link",
    },
    "dubrovnik": {
        "url": "https://experiencedubrovnik.com/en/event-calendar?filter=thisweek",
        "base_url": None,
        "card_selector": ".tzdevents-event-card",
        "title_selector": "h3.tzdevents-card-title",
        "date_selector": ".tzdevents-card-date-box",
        "location_selector": "span.tzdevents-card-location",
        "link_selector": "a.tzdevents-card-more",
    }
}

def scrape_events(page, page_url, card_sel, title_sel, date_sel, location_sel, link_sel, base_url=None):
    page.goto(page_url, wait_until="domcontentloaded", timeout=60000)

    try:
        page.wait_for_selector(card_sel, timeout=20000)
    except Exception:
        pass

    try:
        page.click("text=Use necessary cookies only", timeout=2000)
    except Exception:
        pass

    page.mouse.wheel(0, 2000)
    page.wait_for_timeout(1000)

    cards = page.query_selector_all(card_sel)
    events = []
    LABEL_PREFIX = ["datum", "lokacija"]
    for card in cards:
        card_title = card.query_selector(title_sel)
        card_date = card.query_selector(date_sel)
        card_location = card.query_selector(location_sel) if location_sel else None
        card_link = card.query_selector(link_sel)
        href = card_link.get_attribute("href") if card_link else None
        if href and base_url and not href.startswith("http"):
            href = base_url + href

        raw_title =  card_title.inner_text() if card_title else None
        raw_date = card_date.inner_text() if card_date else None
        raw_location = card_location.inner_text() if card_location else None

        events.append({
            "title": clean_data(raw_title, LABEL_PREFIX),
            "date": clean_data(raw_date, LABEL_PREFIX),
            "location": clean_data(raw_location, LABEL_PREFIX),
            "sourceUrl": href,
        })
    return events

def clean_data(raw, strip_prefix=None):
    if raw is None:
        return None

    text = " ".join(raw.split())

    if not text: 
        return None

    if strip_prefix: 
        for prefix in strip_prefix:
            pattern = rf"^{re.escape(prefix)}\s+"
            text = re.sub(pattern, "", text, flags=re.IGNORECASE)

    return text.strip() or None

def scrape():
    results = {}

    with sync_playwright() as p:
        browser = p.firefox.launch()
        page = browser.new_page()

        for city, config in CITIES.items():
            try:
                results[city] = scrape_events(
                    page,
                    page_url = config["url"],
                    base_url = config["base_url"],
                    card_sel = config["card_selector"],
                    title_sel = config["title_selector"],
                    date_sel = config["date_selector"],
                    location_sel = config["location_selector"],
                    link_sel = config["link_selector"]
                )
                print(f"{city} - pronađeno {len(results[city])} događanja.")
            except Exception as e:
                print(f"Greška prilikom dohvaćivanja podataka za {city}: {e}")
                results[city] = []

        browser.close()
    return results

def main():
    data = scrape()

    with open("data.json", "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
    print("Spremljeno u data.json")

if __name__ == "__main__":
    main()