package edu.rit.csh.bettervent;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewDisplayable;
import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.List;

public class ScheduleFragment extends Fragment {

    WeekView mWeekView;

    List<WeekViewDisplayable<Event>> events;

    public static ScheduleFragment newInstance(List<Event> events){
        ScheduleFragment f = new ScheduleFragment();
        Bundle args = new Bundle();
        // I guess you can serialize events. Huh.
        args.putSerializable("events", new ArrayList<>(events));
        f.setArguments(args);
        return f;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        System.err.println("########Loaded Schedule Fragment.");
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        mWeekView = view.findViewById(R.id.week_view);
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
//        mWeekView.setMonthChangeListener(mMonthChangeListener);

        // Set long press listener for events.
//        mWeekView.setEventLongPressListener(mEventLongPressListener);

//        mWeekView.setWeekViewLoader();
    }
}
