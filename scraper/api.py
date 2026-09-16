from flask import Flask, jsonify
from flask_cors import CORS
import json
import os

DATA_FILE = "data.json"

app = Flask(__name__)
CORS(app)

def load_data():
    if not os.path.exists(DATA_FILE):
        return {}
    with open(DATA_FILE, "r", encoding="utf-8") as f:
        return json.load(f)

@app.route("/events")
def all_events():
    data = load_data()
    return jsonify(data)

@app.route("/events/<city>")
def events_city(city):
    data = load_data()
    city_key = city.lower()
    if city_key not in data:
        return jsonify({"greska":f"nema podataka za '{city}'"}), 404
    return jsonify(data[city_key])

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)