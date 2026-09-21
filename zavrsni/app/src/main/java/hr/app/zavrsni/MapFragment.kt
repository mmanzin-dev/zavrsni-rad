package hr.app.zavrsni

import android.os.Bundle
import android.view.LayoutInflater
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class MapFragment : Fragment() {
    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Configuration.getInstance().load(
            requireContext(),
            PreferenceManager.getDefaultSharedPreferences(requireContext()))
        Configuration.getInstance().userAgentValue = "hr.app.zavrsni"
        Configuration.getInstance().userAgentHttpHeader = "User-Agent"

        mapView = view.findViewById(R.id.mapView)
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
            marker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.location_on_48px)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            marker.setOnMarkerClickListener { _, _ ->
                val bundle = Bundle().apply {
                    putString("cityId", city.id)
                    putString("cityName", city.name)
                }
                findNavController().navigate(R.id.action_map_to_events, bundle)
                true
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
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