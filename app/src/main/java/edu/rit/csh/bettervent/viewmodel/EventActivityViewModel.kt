package edu.rit.csh.bettervent.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings.System.getString
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

class EventActivityViewModel(application: Application) : AndroidViewModel(application) {
    val events: ArrayList<Event> = arrayListOf()
    private val settings: SharedPreferences by lazy {
        Log.i("EventActivityViewModel", "Creating sharedprefs!")
        application.applicationContext.getSharedPreferences(application.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
    }

    private val mService = getCalendarService()

    fun refresh() {
        updateEvents()
    }

    private fun getCalendarService(): Calendar {
        Log.i("EventActivityViewModel", settings.getString("test", "test")!!)
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

    private fun updateEvents(){
        doAsync{
            val events = getEventsFromServer()
            uiThread {
                handleEvents(parseEvents(events))
            }
        }
    }

    private fun getEventsFromServer(): Events {
        val calendarId = settings.getString("edu.rit.csh.bettervent.calendarid", "rti648k5hv7j3ae3a3rum8potk@group.calendar.google.com")

        val maxResultsStr = settings.getString("edu.rit.csh.bettervent.maxresults", "100")
        val maxResults = maxResultsStr?.let { Integer.parseInt(it) }

        val now = DateTime(System.currentTimeMillis())
        return mService.events().list(calendarId)
                .setMaxResults(maxResults)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute()
    }

    private fun parseEvents(calendarEvents: Events): ArrayList<Event>{
        val events = ArrayList<Event>()
        for (calendarEvent in calendarEvents.items){
            val event = calendarEvent.parseToEvent()
            event?.also{
                Log.i("EventActivity", "Event added: $event")
                events.add(it)
            }
        }
        return events
    }

    private fun handleEvents(inEvents: ArrayList<Event>){
        events.removeAll(events)

        if (inEvents.isNotEmpty()){
            val eventKeyword = settings.getString("edu.rit.csh.bettervent.filterkeywords", "")!!
            events.removeAll(events)
            for (event in inEvents) {
                val eventFieldToCheck = if (settings.getBoolean("edu.rit.csh.bettervent.filterbytitle", false)) {
                    event.summary
                } else {
                    event.location
                }
                if (eventKeyword.isNotEmpty()) {
                    if (eventFieldToCheck.toLowerCase(Locale.getDefault()).contains(eventKeyword.toLowerCase(Locale.getDefault()))) {
                        events.add(event)
                    }
                } else {
                    events.add(event)
                }
            }
        }
    }

    companion object {
        private const val PREF_ACCOUNT_NAME = "accountName"
        private val SCOPES = arrayOf(CalendarScopes.CALENDAR_READONLY)
        private lateinit var settings: SharedPreferences
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