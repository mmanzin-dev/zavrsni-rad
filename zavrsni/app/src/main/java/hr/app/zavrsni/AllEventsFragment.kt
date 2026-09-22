package hr.app.zavrsni

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class AllEventsFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var adapter: EventAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var errorText: TextView
    private lateinit var citySpinner: Spinner

    private var allEventsByCity: Map<String, List<Event>> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        adapter = EventAdapter { event ->
            val url = event.sourceUrl ?: return@EventAdapter
            CustomTabsIntent.Builder().build().launchUrl(requireContext(), url.toUri())
        }

        recyclerView = view.findViewById(R.id.eventsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        emptyText = view.findViewById(R.id.emptyText)
        errorText = view.findViewById(R.id.errorText)
        citySpinner = view.findViewById(R.id.citySpinner)

        view.findViewById<View>(R.id.retryButton).setOnClickListener { loadAllEvents() }

        loadAllEvents()
    }

    private fun loadAllEvents() {
        recyclerView.visibility = View.GONE
        emptyText.visibility = View.GONE
        errorText.visibility = View.GONE
        loadingIndicator.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                allEventsByCity = RetrofitClient.api.getAllEvents()
                loadingIndicator.visibility = View.GONE
                setupSpinner()
                showEventsForSelectedCity("Svi gradovi")
            } catch (e: Exception) {
                loadingIndicator.visibility = View.GONE
                errorText.visibility = View.VISIBLE
                errorText.text = "${e.message}"
            }
        }
    }

    private fun setupSpinner() {
        val cityNames = listOf("Svi gradovi") + allEventsByCity.keys.sorted()

        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            cityNames
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            citySpinner.adapter = adapter
        }

        citySpinner.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        showEventsForSelectedCity(parent.getItemAtPosition(pos) as String)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun showEventsForSelectedCity(cityKey: String) {
        val events = if (cityKey == "Svi gradovi") {
            allEventsByCity.flatMap { (city, cityEvents) ->
                cityEvents.map { it.copy(city = city) }
            }
        } else {
            allEventsByCity[cityKey]?.map { it.copy(city = cityKey) } ?: emptyList()
        }

        if (events.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            emptyText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateEvents(events)
        }
    }
}