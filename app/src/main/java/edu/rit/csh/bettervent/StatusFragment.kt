package edu.rit.csh.bettervent

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import kotlinx.android.synthetic.main.fragment_status.*
import kotlinx.android.synthetic.main.fragment_status.view.*
import kotlinx.android.synthetic.main.password_alert.*
import kotlinx.android.synthetic.main.password_alert.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

import java.io.Serializable

class StatusFragment : Fragment() {

    private lateinit var appSettings: SharedPreferences // Settings object containing user preferences.

    var events: ArrayList<Event> = ArrayList()

    // Variables for storing what the status should read out as
    var currentTitle: String? = null
    lateinit var currentTime: String
    var nextTitle: String? = null
    lateinit var nextTime: String

    /**
     * Extract information from the bundle that may have been provided with the StatusFragment,
     * inflate status_layout and set it as the currently active view, then make references to all of
     * the various pieces of the UI so that the class can update the UI with the API data.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        infoPrint("Loaded Status Fragment.")

        // Load up app settings to fetch passwords and background colors.
        appSettings = context!!.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val args = arguments
        if (args != null) {
            events.addAll(args.getSerializable("events") as List<Event>)
            getCurrentAndNextEvents()

        } else {
            infoPrint("ERROR! NO DATA FOUND!")
        }

        val view = inflater.inflate(R.layout.fragment_status, container, false)

        MainActivity.centralClock.setTextColor(-0x1)

        fun showAlertWithFunction(onSuccess: () -> Unit){
            context!!.alert("Enter Password:"){
                val v = layoutInflater.inflate(R.layout.password_alert, null)
                customView = v
                fun checkPassword(pw: String){
                    if (pw == appSettings!!.getString("edu.rit.csh.bettervent.password", "")) onSuccess()
                }
                yesButton { checkPassword(v.password_et.text.toString()) }
                noButton { dialog -> dialog.cancel() }
            }.show()
        }

        view.leave_button.setOnClickListener {
            showAlertWithFunction { System.exit(0) }
        }

        view.settings_button.setOnClickListener {
            MainActivity.selectedFragment = SettingsFragment()

            showAlertWithFunction { fragmentManager!!.beginTransaction().replace(R.id.fragment_container,
                    MainActivity.selectedFragment as SettingsFragment).commit() }
        }

        if (currentTitle == null) {
            nextTime = ""
            nextTitle = nextTime
            currentTime = nextTitle as String
            currentTitle = currentTime
        }
        if (nextTitle == null) nextTitle = ""
        return view
    }

    /**
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        infoPrint("Fragment Event Title: " + currentTitle!!)
        setRoomStatus()
    }

    /**
     *
     */
    private fun setRoomStatus() {
        // Set current status of the room
        if (currentTitle != "") {
            free_label.visibility = View.INVISIBLE
            reserved_label.visibility = View.VISIBLE
            event_title.text = currentTitle
            event_time!!.text = currentTime
            status_layout.setBackgroundColor(resources.getColor(R.color.CSHRed))
        } else {
            reserved_label.visibility = View.INVISIBLE
            free_label.visibility = View.VISIBLE
            event_title.text = ""
            event_time.text = ""
            status_layout.setBackgroundColor(resources.getColor(R.color.CSHGreen))
        }

        // Set the future status of the room
        if (nextTitle !== "") {
            next_label.visibility = View.VISIBLE
            next_event_title.text = nextTitle
            next_event_time.text = nextTime
        } else {
            next_label.visibility = View.INVISIBLE
            next_event_title.text = "There are no upcoming events."
            next_event_time.text = ""
        }
    }

    /**
     * Looks at the APIOutList (the List of Events generated by the API),
     * and based on how many there are and when they are, sets the string
     * values for currentEventTitle, currentEventTime, nextEventTitle, and
     * nextEventTime.
     */
    private fun getCurrentAndNextEvents() {
        if (events == null)
            infoPrint("There may have been an issue getting the data." + "\nor maybe there was no data.")

        if (events == null || events!!.isEmpty()) {
            nextTime = ""
            nextTitle = nextTime
            currentTime = nextTitle as String
            currentTitle = currentTime
        } else {
            //Here's all the data we'll need.
            val summary = events!![0].summary
            var start: DateTime? = events!![0].start.dateTime
            val end = events!![0].end.dateTime

            if (start == null) {
                // If the event will last all day then only use the event title.
                start = events!![0].start.date
                currentTitle = summary
                currentTime = "All day"
            } else {
                // If the event has a set start and end time then check if it's now or later.
                val now = DateTime(System.currentTimeMillis())
                if (start.value > now.value) {
                    // If the first event will happen in the future
                    // Then there is no current event.
                    currentTitle = ""
                    currentTime = ""
                    nextTitle = summary
                    nextTime = formatDateTime(start) + " — " + formatDateTime(end)
                } else {
                    // Set current event to first event if it's happening right now.
                    currentTitle = summary
                    currentTime = formatDateTime(start) + " — " + formatDateTime(end)
                    if (events!!.size > 1)
                    // Get the next event after this one
                        getNextEvent()
                }
            }
        }
    }

    /**
     * Takes the second index of APIOutList (the List of Events generated by the API)
     * and sets nextEventTitle and nextEventTime.
     */
    private fun getNextEvent() {
        try {
            val nextEventSummary = events!![1].summary
            var nextEventStart: DateTime? = events!![1].start.dateTime
            val nextEventEnd = events!![1].end.dateTime
            if (nextEventStart == null) {
                // All-day events don't have start times, so just use
                // the start date.
                nextEventStart = events!![1].start.date
            }
            nextTitle = nextEventSummary
            nextTime = formatDateTime(nextEventStart!!) + " — " + formatDateTime(nextEventEnd)
        } catch (e: Exception) {
            nextTitle = ""
            nextTime = ""
        }
    }

    /**
     * Method to format DateTimes into human-readable strings
     *
     * @param dateTime: DateTime to make readable
     * @return: HH:MM on YYYY/MM/DD
     */
    private fun formatDateTime(dateTime: DateTime): String {
        return if (dateTime.isDateOnly) {
            dateTime.toString()
        } else {
            val t = dateTime.toString().split("T".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val time = t[1].substring(0, 5)
            val date = t[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val dateString = date[0] + "/" + date[1] + "/" + date[2]

            "$time on $dateString"
        }
    }

    private fun infoPrint(info: String) {
        println("STAT_: $info")
    }

    companion object {

        fun newInstance(events: List<Event>): StatusFragment {
            val f = StatusFragment()
            val args = Bundle()
            args.putSerializable("events", events as Serializable)
            f.arguments = args
            return f
        }
    }
}
