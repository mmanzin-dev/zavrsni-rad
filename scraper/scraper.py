from playwright.sync_api import sync_playwright
import json

CITIES = {
    "zagreb": {
        "url": "https://www.infozagreb.hr/en/events",
        "card_selector": ".event-card",
        "title_selector": ".title",
        "date_selector": ".dates",
        "location_selector": ".location",
        "link_selector": "a",
    },
    "varazdin": {
        "url": "https://varazdin.events/",
        "card_selector": ".bg-white.rounded-xl.overflow-hidden.flex.flex-col",
        "title_selector": "h3.font-bold.leading-snug",
        "date_selector": ".shrink-0.text-center.relative.z-10",
        "location_selector": "span.truncate",
        "link_selector": "a.mt-3.text-xs.font-semibold",
    },
    "bjelovar": {
        "url": "https://www.bjelovar.hr/dogadjaji/",
        "card_selector": "article.post.lsvr_event",
        "title_selector": "h2.post__title",
        "date_selector": "p.post-info__date",
        "location_selector": "span.post-info__location",
        "link_selector": "a.post__location-link",
    },
    "osijek": {
        "url": "https://osijek.in/dogadaji/kategorija/sva-dogadanja/popis/",
        "card_selector": "li.tribe-events-calendar-list__event-row",
        "title_selector": "a.tribe-events-calendar-list__event-title-link",
        "date_selector": "time.tribe-events-calendar-list__event-datetime",
        "location_selector": "span.tribe-events-calendar-list__event-venue-title",
        "link_selector": "a.tribe-events-calendar-list__event-title-link",
    },
    "pula": {
        "url": "https://pulainfo.hr/hr/pula-events/",
        "card_selector": ".col-sm-6.col-lg-4.col-xl-3.col-reset",
        "title_selector": ".color-caption h2",
        "date_selector": ".color-caption p[style='margin-bottom:0; float:left;']",
        "location_selector": ".color-caption p.box-location",
        "link_selector": "a",
    },
    "rijeka": {
        "url": "https://visitrijeka.hr/dogadanja",
        "card_selector": ".view-grid-item",
        "title_selector": "h3.nc-font-sssb.text-center",
        "date_selector": "p.nc-font-sssb.type.mb-3.pt-3.text-center",
        "location_selector": None,
        "link_selector": "a.text-dark",
    },
    "zadar": {
        "url": "https://zadar.travel/hr/dogadaji/",
        "card_selector": "article.a-item",
        "title_selector": "a.a-item__title, h2.a-item__title, h3.a-item__title",
        "date_selector": ".a-item__details__item:has(.icon-calendar) p",
        "location_selector": ".a-item__details__item.curs-pointer p",
        "link_selector": "a.a-item__details__item",
    },
    "split": {
        "url": "https://split.hr/kalendar/en/city-split-calendar",
        "card_selector": "article.l-item",
        "title_selector": "p.o-title",
        "date_selector": ".o-hour",
        "location_selector": ".o-location",
        "link_selector": "a.o-link",
    },
    "dubrovnik": {
        "url": "https://experiencedubrovnik.com/en/event-calendar?filter=thisweek",
        "card_selector": ".tzdevents-event-card",
        "title_selector": "h3.tzdevents-card-title",
        "date_selector": ".tzdevents-card-date-box",
        "location_selector": "span.tzdevents-card-location",
        "link_selector": "a.tzdevents-card-more",
    }
}

def scrape_events(page, page_url, card_sel, title_sel, date_sel, location_sel, link_sel):
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
    for card in cards:
        card_title = card.query_selector(title_sel)
        card_date = card.query_selector(date_sel)
        card_location = card.query_selector(location_sel) if location_sel else None
        card_link = card.query_selector(link_sel)

        events.append({
            "title": card_title.inner_text().strip() if card_title else None,
            "date": card_date.inner_text().strip() if card_date else None,
            "location": card_location.inner_text().strip() if card_location else None,
            "sourceUrl": card_link.get_attribute("href") if card_link else None,
        })
    return events

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