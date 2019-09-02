package edu.rit.csh.bettervent

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event

import java.io.Serializable

class StatusFragment : Fragment() {

    private var appSettings: SharedPreferences? = null // Settings object containing user preferences.

    // Oops, all UI!
    private var statusLayout: ConstraintLayout? = null
    private var reservedText: TextView? = null
    private var freeText: TextView? = null
    private var nextText: TextView? = null
    private var eventTitleText: TextView? = null
    private var eventTimeText: TextView? = null
    private var nextTitleText: TextView? = null
    private var nextTimeText: TextView? = null

    private var leaveButton: Button? = null
    private var settingsButton: Button? = null

    var events: List<Event>? = null

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
            events = args.getSerializable("events") as List<Event>
            getCurrentAndNextEvents()

        } else {
            infoPrint("ERROR! NO DATA FOUND!")
        }

        val view = inflater.inflate(R.layout.fragment_status, container, false)

        statusLayout = view.findViewById(R.id.status_layout)

        reservedText = view.findViewById(R.id.reserved_label)
        freeText = view.findViewById(R.id.free_label)
        nextText = view.findViewById(R.id.next_label)
        eventTitleText = view.findViewById(R.id.event_title)
        eventTimeText = view.findViewById(R.id.event_time)
        nextTitleText = view.findViewById(R.id.next_event_title)
        nextTimeText = view.findViewById(R.id.next_event_time)

        leaveButton = view.findViewById(R.id.leave_button)
        settingsButton = view.findViewById(R.id.settings_button)

        MainActivity.centralClock.setTextColor(-0x1)

        //TODO: There has to be a better way to implement the same password box
        //TODO: for two different things.
        leaveButton!!.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Enter Password:")

            // Set up the input
            val input = EditText(context)
            input.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            input.transformationMethod = PasswordTransformationMethod.getInstance()

            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            builder.setView(input)

            // Set up the buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val password = input.text.toString()
                if (password == appSettings!!.getString("edu.rit.csh.bettervent.password", "")) {
                    System.exit(0)
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

            builder.show()
        }

        settingsButton!!.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Enter Password:")

            // Set up the input
            val input = EditText(context)
            input.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            input.transformationMethod = PasswordTransformationMethod.getInstance()
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            builder.setView(input)

            // Set up the buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val password = input.text.toString()
                if (password == appSettings!!.getString("edu.rit.csh.bettervent.password", "")) {
                    MainActivity.selectedFragment = SettingsFragment()
                    fragmentManager!!.beginTransaction().replace(R.id.fragment_container,
                            MainActivity.selectedFragment as SettingsFragment).commit()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

            builder.show()
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
            freeText!!.visibility = View.INVISIBLE
            reservedText!!.visibility = View.VISIBLE
            eventTitleText!!.text = currentTitle
            eventTimeText!!.text = currentTime
            statusLayout!!.setBackgroundColor(resources.getColor(R.color.CSHRed))
        } else {
            reservedText!!.visibility = View.INVISIBLE
            freeText!!.visibility = View.VISIBLE
            eventTitleText!!.text = ""
            eventTimeText!!.text = ""
            statusLayout!!.setBackgroundColor(resources.getColor(R.color.CSHGreen))
        }

        // Set the future status of the room
        if (nextTitle !== "") {
            nextText!!.visibility = View.VISIBLE
            nextTitleText!!.text = nextTitle
            nextTimeText!!.text = nextTime
        } else {
            nextText!!.visibility = View.INVISIBLE
            nextTitleText!!.text = "There are no upcoming events."
            nextTimeText!!.text = ""
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

        if (events == null || events!!.size == 0) {
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
        if (dateTime.isDateOnly) {
            return dateTime.toString()
        } else {
            val t = dateTime.toString().split("T".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val time = t[1].substring(0, 5)
            val date = t[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val dateString = date[0] + "/" + date[1] + "/" + date[2]

            return "$time on $dateString"
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
