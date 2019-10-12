package edu.rit.csh.bettervent

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextClock
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Events
import kotlinx.android.synthetic.main.activity_event.*
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.collections.ArrayList

class EventActivity : AppCompatActivity(), OpenSettingsListener{
    private lateinit var statusFragment: StatusFragment
    private lateinit var scheduleFragment: ScheduleFragment
    private lateinit var quickModeFragment: QuickModeFragment
    private lateinit var fragments: List<Fragment>
    private lateinit var mService: Calendar
    private lateinit var settings: SharedPreferences
    private val events = ArrayList<Event>()

    private val transport = AndroidHttp.newCompatibleTransport()
    private val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_event)
        Log.i("EventActivity", "Started activity")
        refresh_button.setOnClickListener {
            updateEvents()
        }

        settings = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        mService = getCalendarService()

        val bundle = Bundle()
        bundle.putParcelableArrayList("events", events)
        statusFragment = StatusFragment()
        statusFragment.arguments = bundle
        scheduleFragment = ScheduleFragment()
        scheduleFragment.arguments = bundle
        quickModeFragment = QuickModeFragment()
        fragments = listOf(statusFragment, scheduleFragment, quickModeFragment)

        pager.adapter = SlidingPagerAdapter(supportFragmentManager)
        centralClock = findViewById(R.id.central_clock)

        updateEvents()
    }

    private fun getCalendarService(): Calendar{
        val credential = GoogleAccountCredential.usingOAuth2(
                applicationContext, listOf(*SCOPES))
                .setBackOff(ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, ""))

        Log.i("EventActivity", "${credential.selectedAccountName}")

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

        val maxResultsStr = settings.getString("edu.rit.csh.bettervent.maxresults", "")
        val maxResults = if (maxResultsStr !== "" && maxResultsStr != null)
            Integer.parseInt(maxResultsStr)
        else {
            Log.i("EventActivity", "Max Results not set. Defaulting to 100.")
            100
        }

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

    private fun com.google.api.services.calendar.model.Event.parseToEvent(): Event?{
        location?.also{
            return Event(summary,
                    Date(start.dateTime.value),
                    Date(end.dateTime.value),
                    location)
        }
        return null
    }

    private inner class SlidingPagerAdapter(fm: FragmentManager): FragmentStatePagerAdapter(fm){
        override fun getCount(): Int = fragments.size
        override fun getItem(p0: Int): Fragment {
            Log.i("MainActivity", "Swipe index: $p0")
            bottom_navigation.selectedItemId = p0
            return fragments[p0]
        }
    }

    override fun openSettings() {
        Log.i("EventActivity", "Open settings")
    }

    companion object {
        lateinit var centralClock: TextClock
        private const val PREF_ACCOUNT_NAME = "accountName"
        private val SCOPES = arrayOf(CalendarScopes.CALENDAR_READONLY)
    }
}
