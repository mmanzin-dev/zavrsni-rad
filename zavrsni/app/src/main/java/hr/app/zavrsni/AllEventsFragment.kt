package hr.app.zavrsni

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class AllEventsFragment : Fragment() {
    private lateinit var adapter: EventAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var errorText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EventAdapter { event ->
            val url = event.sourceUrl ?: return@EventAdapter
            CustomTabsIntent.Builder().build().launchUrl(requireContext(), url.toUri())
        }

        recyclerView  = view.findViewById<RecyclerView>(R.id.eventsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        emptyText = view.findViewById(R.id.emptyText)
        errorText = view.findViewById(R.id.errorText)

        loadEvents()
    }

    private fun loadEvents() {
        recyclerView.visibility = View.GONE
        emptyText.visibility = View.GONE
        errorText.visibility = View.GONE
        loadingIndicator.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getAllEvents()
                val events = response.flatMap { (cityKey, cityEvents) ->
                    cityEvents.map { it.copy(city = cityKey) }
                }
                loadingIndicator.visibility = View.GONE

                if (events.isEmpty()) {
                    emptyText.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    adapter.updateEvents(events)
                }
            } catch (e: Exception) {
                loadingIndicator.visibility = View.GONE
                errorText.visibility = View.VISIBLE
                errorText.text = "${e.message}"
            }
        }
    }
}