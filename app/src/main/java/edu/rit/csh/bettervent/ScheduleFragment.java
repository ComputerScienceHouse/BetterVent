package edu.rit.csh.bettervent;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewDisplayable;
import com.alamkanak.weekview.WeekViewEvent;
import com.google.api.services.calendar.model.Event;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ScheduleFragment extends Fragment implements MonthLoader.MonthChangeListener<Event>{

    WeekView mWeekView;

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;

    private int mWeekViewType = TYPE_WEEK_VIEW;

    List<Event> events;
    List<WeekViewDisplayable<Event>> weekViewEvents;

    public static ScheduleFragment newInstance(List<Event> events){
        ScheduleFragment f = new ScheduleFragment();
        Bundle args = new Bundle();
        // I guess you can serialize events. Huh. That's OP.
        args.putSerializable("events", (Serializable) events);
        f.setArguments(args);
        return f;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        infoPrint("Loaded Schedule Fragment.");
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        Bundle args = getArguments();
        if (args != null) {
            infoPrint("Found events data");
            events = (List<Event>) args.getSerializable("events");
            infoPrint("First event title: " + events.get(0).getSummary());
        }else{
            infoPrint("ERROR! NO DATA FOUND!");
        }
        mWeekView = view.findViewById(R.id.week_view);
        weekViewEvents = new ArrayList<>();
        mWeekView.setMonthChangeListener(this);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a reference for the week view in the layout.

        // Set an action when any event is clicked.
//        mWeekView.setOnEventClickListener(mEventClickListener);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
//        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
//        mWeekView.setEventLongPressListener(mEventLongPressListener);

//        mWeekView.setWeekViewLoader(null);
    }

    private void openDayView(MenuItem item) {
        if (mWeekViewType == TYPE_DAY_VIEW) {
            return;
        }

        item.setChecked(!item.isChecked());
        mWeekViewType = TYPE_DAY_VIEW;
        mWeekView.setNumberOfVisibleDays(1);

        // Lets change some dimensions to best fit the view.
        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        mWeekView.setTimeColumnTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
    }

    private void openThreeDayView(MenuItem item) {
        if (mWeekViewType == TYPE_THREE_DAY_VIEW) {
            return;
        }

        item.setChecked(!item.isChecked());
        mWeekViewType = TYPE_THREE_DAY_VIEW;
        mWeekView.setNumberOfVisibleDays(3);

        // Lets change some dimensions to best fit the view.
        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        mWeekView.setTimeColumnTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
    }

    private void openWeekView(MenuItem item) {
        if (mWeekViewType == TYPE_WEEK_VIEW) {
            return;
        }

        item.setChecked(!item.isChecked());
        mWeekViewType = TYPE_WEEK_VIEW;
        mWeekView.setNumberOfVisibleDays(7);

        // Lets change some dimensions to best fit the view.
        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
        mWeekView.setTimeColumnTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     */
    private void setupDateTimeInterpreter() {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {

            SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

            @Override
            public String interpretDate(Calendar date) {
                String weekday = weekdayNameFormat.format(date.getTime());
                if (mWeekView.getNumberOfVisibleDays() == 7) {
                    weekday = String.valueOf(weekday.charAt(0));
                }
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
            }
        });
    }

    protected String getEventTitle(Calendar time) {
        int hour = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);
        int month = time.get(Calendar.MONTH) + 1;
        int dayOfMonth = time.get(Calendar.DAY_OF_MONTH);
        return String.format(Locale.getDefault(), "Event of %02d:%02d %s/%d", hour, minute, month, dayOfMonth);
    }

    private void infoPrint(String info){
        System.out.println("SCHE_: " + info);
    }

    @Override
    public List<WeekViewDisplayable<Event>> onMonthChange(Calendar startDate, Calendar endDate) {
        for (int i = 0; i < events.size(); i++){
            Event e = events.get(i);
            WeekViewEvent wve = new WeekViewEvent();

            // Set ID (not the Google Calendar ID. I guess. Long.parseLong(e.getId()))
            wve.setId(i);

            // Set Title
            wve.setTitle(e.getSummary());

            // Start Time
            Calendar startCal = Calendar.getInstance();
            startCal.setTimeInMillis(e.getStart().getDateTime().getValue());
            wve.setStartTime(startCal);

            // End Time
            Calendar endCal = Calendar.getInstance();
            endCal.setTimeInMillis(e.getEnd().getDateTime().getValue());
            wve.setEndTime(endCal);
            this.weekViewEvents.add(wve);
        }
        //        return mDatabase.getEventsInRange(startDate, endDate);
        return this.weekViewEvents;
    }
}
