package edu.rit.csh.bettervent.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders

import com.alamkanak.weekview.DateTimeInterpreter
import com.alamkanak.weekview.MonthLoader
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import edu.rit.csh.bettervent.R
import edu.rit.csh.bettervent.viewmodel.EventActivityViewModel

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale

class ScheduleFragment : Fragment(){

    lateinit var weekView: WeekView<Any>
    private lateinit var viewModel: EventActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(requireActivity()).get(EventActivityViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        infoPrint("Loaded Schedule Fragment.")
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)


        EventActivity.centralClock.setTextColor(-0x1000000)

        weekView = view.findViewById(R.id.week_view)
        weekView.setMonthChangeListener(MonthChangeListener() as MonthLoader.MonthChangeListener<Any>)
        weekView.numberOfVisibleDays = 7

        // Lets change some dimensions to best fit the view.
        weekView.columnGap = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt()
        weekView.setTimeColumnTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, resources.displayMetrics).toInt())
        weekView.eventTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, resources.displayMetrics).toInt()

        setupDateTimeInterpreter()

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
                return weekday.toUpperCase(Locale.getDefault()) + format.format(date.time)
            }

            override fun interpretTime(hour: Int): String {
                return if (hour > 11) (hour - 12).toString() + " PM" else if (hour == 0) "12 AM" else "$hour AM"
            }
        }
    }

    private fun getEvents(cal: Calendar): ArrayList<Event>{
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        val events = arrayListOf<Event>()

        for (event in viewModel.events){
            val eventCal = Calendar.getInstance()
            eventCal.timeInMillis = event.start.time
            val eventMonth = eventCal.get(Calendar.MONTH)
            val eventYear = eventCal.get(Calendar.YEAR)

            if (month == eventMonth && year == eventYear){
                events.add(event)
            }
        }
        return events
    }

    protected fun getEventTitle(time: Calendar): String {
        val hour = time.get(Calendar.HOUR_OF_DAY)
        val minute = time.get(Calendar.MINUTE)
        val month = time.get(Calendar.MONTH) + 1
        val dayOfMonth = time.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.getDefault(), "Event of %02d:%02d %s/%d", hour, minute, month, dayOfMonth)
    }

    private fun infoPrint(info: String) {
        Log.i("ScheduleFragment", info)
    }

    inner class MonthChangeListener: MonthLoader.MonthChangeListener<Event>{

        override fun onMonthChange(startDate: Calendar, endDate: Calendar): List<WeekViewDisplayable<Event>> {

            val weekViewEvents = ArrayList<WeekViewDisplayable<Event>>()

            val color1 = resources.getColor(R.color.colorPrimaryDark)
            infoPrint("event size : " + viewModel.events.size)

            val events = getEvents(startDate)
            for (i in events.indices) {
                val event = viewModel.events[i]
                val wve = WeekViewEvent<Event>()

                infoPrint(event.toString())

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
