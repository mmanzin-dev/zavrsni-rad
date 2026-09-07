package hr.app.zavrsni

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class EventsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        val cityId = intent.getStringExtra("cityId")
        val cityName = intent.getStringExtra("cityName")

        title = "Događanja u $cityName"
    }
}