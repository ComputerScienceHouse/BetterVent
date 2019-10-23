package edu.rit.csh.bettervent.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Events
import edu.rit.csh.bettervent.R
import edu.rit.csh.bettervent.view.Event
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.collections.ArrayList

class CompanionActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val settings: SharedPreferences =
            application.applicationContext.getSharedPreferences(application.getString(R.string.preference_file_key), Context.MODE_PRIVATE)

    private val mService: Calendar by lazy { getCalendarService() }

    private val locationsString = settings.getString("locations", "")!!

    val usedLocations = locationsString.split("|").filterNot{ it.isBlank() }.toMutableSet()
    val allLocations= mutableSetOf<String>()
    val eventsByLocation = mutableMapOf<String, Event?>()

    fun refresh(onComplete: () -> Unit) {
        updateEvents(onComplete)
    }

    fun addUsedLocation(location: String){
        var locationsString = settings.getString("locations", "")!!
        locationsString = "$locationsString|$location"
        settings.edit().putString("locations", locationsString).apply()
        usedLocations.add(location)
    }

    fun removeUsedLocation(location: String){
        var locationsString = settings.getString("locations", "")!!
        locationsString = locationsString.replace(location, "").replace("||", "")
        settings.edit().putString("locations", locationsString).apply()
        usedLocations.remove(location)
    }

    private fun getCalendarService(): Calendar {
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

        val credential = GoogleAccountCredential.usingOAuth2(
                getApplication(), listOf(*SCOPES))
                .setBackOff(ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, ""))

        return Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build()
    }

    private fun updateEvents(f: () -> Unit){
        doAsync{
            val events = getEventsFromServer(MAX_EVENTS)
            uiThread {
                handleEvents(parseEvents(events))
                f.invoke()
            }
        }
    }

    private fun getEventsFromServer(maxEvents: Int): Events {
        val calendarId = settings.getString("edu.rit.csh.bettervent.calendarid", "rti648k5hv7j3ae3a3rum8potk@group.calendar.google.com")

        val now = DateTime(System.currentTimeMillis())
        return mService.events().list(calendarId)
                .setMaxResults(maxEvents)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute()
    }

    private fun parseEvents(calendarEvents: Events): MutableList<Event>{
        val events = mutableListOf<Event>()

        events.addAll(calendarEvents.items.mapNotNull { calendarEvent ->
            calendarEvent.location?.let { allLocations.add(it) }
            calendarEvent.parseToEvent()
        })
        Log.i("CompanionActivityViewModel", allLocations.toString())

        return events
    }

    private fun handleEvents(inEvents: Collection<Event>){
        eventsByLocation.clear()
        eventsByLocation.putAll(usedLocations.map { location ->
            location to inEvents.firstOrNull { event ->
                event.location.trim().toLowerCase(Locale.getDefault()) ==
                        location.trim().toLowerCase(Locale.getDefault())
            }
        })
    }

    companion object {
        private const val PREF_ACCOUNT_NAME = "accountName"
        private const val MAX_EVENTS = 50
        private val SCOPES = arrayOf(CalendarScopes.CALENDAR_READONLY)
    }

    private fun com.google.api.services.calendar.model.Event.parseToEvent(): Event?{
        location?.also{
            return Event(summary,
                    Date(start.dateTime.value),
                    Date(end.dateTime.value),
                    location)
        }
        return null
    }
}