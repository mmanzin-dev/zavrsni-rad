package hr.app.zavrsni

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        Configuration.getInstance().userAgentValue = "hr.app.zavrsni"
        Configuration.getInstance().userAgentHttpHeader = "User-Agent"

        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val croatia = GeoPoint(45.00, 16.30)
        mapView.controller.setZoom(8.0)
        mapView.controller.setCenter(croatia)

        addCityMarkers()
    }

    private fun addCityMarkers() {
        val cities = listOf(
            City("zagreb", "Zagreb", 45.81318574587369, 15.97731730452615),
            City("varazdin", "Varaždin", 46.306060882491366, 16.340456302384784),
            City("bjelovar", "Bjelovar", 45.8986807624263, 16.84218635382768),
            City("osijek", "Osijek", 45.55488218448623, 18.69533710249372),
            City("pula", "Pula", 44.87323085305393, 13.85008702776191),
            City("rijeka", "Rijeka", 45.327044438192495, 14.447528398239657),
            City("zadar", "Zadar", 44.11933024237374, 15.231360450418428),
            City("split", "Split", 43.51470011943644, 16.44355537319929),
            City("dubrovnik", "Dubrovnik", 42.64915994725809, 18.093913727589587)
        )

        cities.forEach { city ->
            val marker = Marker(mapView)
            marker.position = GeoPoint(city.latitude, city.longitude)
            marker.title = city.name
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            marker.setOnMarkerClickListener { _, _ ->
                openEventsScreen(city.id, city.name)
                true
            }

            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    private fun openEventsScreen(cityId: String, cityName: String) {
        val intent = Intent(this, EventsActivity::class.java)
        intent.putExtra("cityId", cityId)
        intent.putExtra("cityName", cityName)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}