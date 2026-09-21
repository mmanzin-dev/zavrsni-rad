package hr.app.zavrsni

import retrofit2.http.GET
import retrofit2.http.Path

interface EventApi {
    @GET("events/{city}")
    suspend fun getEventsForCity(@Path("city") city: String) : List<Event>

    @GET("events")
    suspend fun getAllEvents(): Map<String, List<Event>>
}