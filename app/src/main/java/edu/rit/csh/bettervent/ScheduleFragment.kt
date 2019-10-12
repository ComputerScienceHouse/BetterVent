package edu.rit.csh.bettervent

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.alamkanak.weekview.DateTimeInterpreter
import com.alamkanak.weekview.MonthLoader
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import kotlinx.android.synthetic.main.fragment_status.*

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale

class ScheduleFragment : Fragment(){

    lateinit var weekView: WeekView<Any>
    private lateinit var events: ArrayList<Event>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        infoPrint("Loaded Schedule Fragment.")
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)
        val args = arguments
        if (args != null) {
            infoPrint("Found events data")
            events = args.getParcelableArrayList("events")!!
            if ( events.isNotEmpty())
                infoPrint("First event title: " + events[0].summary)
        } else {
            infoPrint("ERROR! NO DATA FOUND!")
        }

        EventActivity.centralClock.setTextColor(-0x1000000)

        weekView = view.findViewById(R.id.week_view)
        weekView.setMonthChangeListener(MonthChangeListener() as MonthLoader.MonthChangeListener<Any>)
        weekView.numberOfVisibleDays = 7

        // Lets change some dimensions to best fit the view.
        weekView.columnGap = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt()
        weekView.setTimeColumnTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, resources.displayMetrics).toInt())
        weekView.eventTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, resources.displayMetrics).toInt()
        return view
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     */
    private fun setupDateTimeInterpreter() {
        weekView.dateTimeInterpreter = object : DateTimeInterpreter {

            internal var weekdayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
            internal var format = SimpleDateFormat(" M/d", Locale.getDefault())

            override fun interpretDate(date: Calendar): String {
                var weekday = weekdayNameFormat.format(date.time)
                if (weekView.numberOfVisibleDays == 7) {
                    weekday = weekday[0].toString()
                }
                return weekday.toUpperCase() + format.format(date.time)
            }

            override fun interpretTime(hour: Int): String {
                return if (hour > 11) (hour - 12).toString() + " PM" else if (hour == 0) "12 AM" else "$hour AM"
            }
        }
    }

    protected fun getEventTitle(time: Calendar): String {
        val hour = time.get(Calendar.HOUR_OF_DAY)
        val minute = time.get(Calendar.MINUTE)
        val month = time.get(Calendar.MONTH) + 1
        val dayOfMonth = time.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.getDefault(), "Event of %02d:%02d %s/%d", hour, minute, month, dayOfMonth)
    }

    private fun infoPrint(info: String) {
        println("SCHE_: $info")
    }

    companion object {

        fun newInstance(events: List<Event>?): ScheduleFragment {
            val f = ScheduleFragment()
            val args = Bundle()
            args.putSerializable("events", events as Serializable)
            f.arguments = args
            return f
        }
    }

    inner class MonthChangeListener: MonthLoader.MonthChangeListener<edu.rit.csh.bettervent.Event>{

        override fun onMonthChange(startDate: Calendar, endDate: Calendar): List<WeekViewDisplayable<Event>> {

            val weekViewEvents = ArrayList<WeekViewDisplayable<Event>>()

            val color1 = resources.getColor(R.color.colorPrimaryDark)
            infoPrint("event size : " + events.size)
            for (i in events.indices) {
                val event = events[i]
                val wve = WeekViewEvent<Event>()

                // Set ID (not the Google Calendar ID).
                wve.id = i.toLong()

                // Set Title
                wve.title = event.summary

                val newYear = startDate.get(Calendar.YEAR)
                val newMonth = startDate.get(Calendar.MONTH)

                //TODO: NullPointerException still happens somewhere in WeekViewEvent.
                try {
                    // Start Time
                    val startCal = Calendar.getInstance()
                    startCal.timeInMillis = event.start.time
                    startCal.set(Calendar.MONTH, newMonth)
                    startCal.set(Calendar.YEAR, newYear)
                    Log.i("ScheduleFragment", "Startcal: $startCal")
                    wve.startTime = startCal

                    // End Time
                    val endCal = Calendar.getInstance()
                    endCal.timeInMillis = event.end.time
                    endCal.set(Calendar.MONTH, newMonth)
                    endCal.set(Calendar.YEAR, newYear)
                    wve.endTime = endCal
                } catch (error: NullPointerException) {
                    error.printStackTrace()
                    wve.setIsAllDay(true)
                }

                wve.color = color1

                wve.setIsAllDay(false)

                weekViewEvents.add(wve as WeekViewDisplayable<Event>)
            }
            return weekViewEvents
        }
    }
}
