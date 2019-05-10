package edu.rit.csh.bettervent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.io.Serializable;
import java.util.List;

public class StatusFragment extends Fragment {

    private SharedPreferences appSettings; // Settings object containing user preferences.

    // Oops, all UI!
    private ConstraintLayout statusLayout;
    private TextView reservedText;
    private TextView freeText;
    private TextView nextText;
    private TextView eventTitleText;
    private TextView eventTimeText;
    private TextView nextTitleText;
    private TextView nextTimeText;

    private Button leaveButton;
    private Button settingsButton;

    public List<Event> events;

    // Variables for storing what the status should read out as
    public String currentTitle;
    public String currentTime;
    public String nextTitle;
    public String nextTime;

    public static StatusFragment newInstance(List<Event> events) {
        StatusFragment f = new StatusFragment();
        Bundle args = new Bundle();
        args.putSerializable("events", (Serializable) events);
        f.setArguments(args);
        return f;
    }

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
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        infoPrint("Loaded Status Fragment.");

        // Load up app settings to fetch passwords and background colors.
        appSettings = getContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        Bundle args = getArguments();
        if (args != null) {
            events = (List<Event>) args.getSerializable("events");
            getCurrentAndNextEvents();

        } else {
            infoPrint("ERROR! NO DATA FOUND!");
        }

        View view = inflater.inflate(R.layout.fragment_status, container, false);

        statusLayout = view.findViewById(R.id.status_layout);

        reservedText   = view.findViewById(R.id.reserved_label);
        freeText       = view.findViewById(R.id.free_label);
        nextText       = view.findViewById(R.id.next_label);
        eventTitleText = view.findViewById(R.id.event_title);
        eventTimeText  = view.findViewById(R.id.event_time);
        nextTitleText  = view.findViewById(R.id.next_event_title);
        nextTimeText   = view.findViewById(R.id.next_event_time);

        leaveButton    = view.findViewById(R.id.leave_button);
        settingsButton = view.findViewById(R.id.settings_button);

        MainActivity.centralClock.setTextColor(0xffffffff);

        //TODO: There has to be a better way to implement the same password box
        //TODO: for two different things.
        leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Enter Password:");

                // Set up the input
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input.setTransformationMethod(PasswordTransformationMethod.getInstance());

                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = input.getText().toString();
                        if (password.equals(appSettings.getString("edu.rit.csh.bettervent.password", ""))){
                            System.exit(0);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Enter Password:");

                // Set up the input
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = input.getText().toString();
                        if (password.equals(appSettings.getString("edu.rit.csh.bettervent.password", ""))){
                            MainActivity.selectedFragment = new SettingsFragment();
                            getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                    MainActivity.selectedFragment).commit();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
        if (currentTitle == null) currentTitle = currentTime = nextTitle = nextTime = "";
        if (nextTitle == null) nextTitle = "";
        return view;
    }

    /**
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        infoPrint("Fragment Event Title: " + currentTitle);
        setRoomStatus();
    }

    /**
     *
     */
    private void setRoomStatus() {
        // Set current status of the room
        if (!currentTitle.equals("")) {
            freeText.setVisibility(View.INVISIBLE);
            reservedText.setVisibility(View.VISIBLE);
            eventTitleText.setText(currentTitle);
            eventTimeText.setText(currentTime);
            statusLayout.setBackgroundColor(getResources().getColor(R.color.CSHRed));
        } else {
            reservedText.setVisibility(View.INVISIBLE);
            freeText.setVisibility(View.VISIBLE);
            eventTitleText.setText("");
            eventTimeText.setText("");
            statusLayout.setBackgroundColor(getResources().getColor(R.color.CSHGreen));
        }

        // Set the future status of the room
        if (nextTitle != "") {
            nextText.setVisibility(View.VISIBLE);
            nextTitleText.setText(nextTitle);
            nextTimeText.setText(nextTime);
        } else {
            nextText.setVisibility(View.INVISIBLE);
            nextTitleText.setText("There are no upcoming events.");
            nextTimeText.setText("");
        }
    }

    /**
     * Looks at the APIOutList (the List of Events generated by the API),
     * and based on how many there are and when they are, sets the string
     * values for currentEventTitle, currentEventTime, nextEventTitle, and
     * nextEventTime.
     */
    private void getCurrentAndNextEvents() {
        if (events == null)
            infoPrint("There may have been an issue getting the data." +
                    "\nor maybe there was no data.");

        if (events == null || events.size() == 0) {
            currentTitle = currentTime = nextTitle = nextTime = "";
        } else {
            //Here's all the data we'll need.
            String summary = events.get(0).getSummary();
            DateTime start = events.get(0).getStart().getDateTime();
            DateTime end = events.get(0).getEnd().getDateTime();

            if (start == null) {
                // If the event will last all day then only use the event title.
                start = events.get(0).getStart().getDate();
                currentTitle = summary;
                currentTime = "All day";
            } else {
                // If the event has a set start and end time then check if it's now or later.
                DateTime now = new DateTime(System.currentTimeMillis());
                if (start.getValue() > now.getValue()) {
                    // If the first event will happen in the future
                    // Then there is no current event.
                    currentTitle = "";
                    currentTime = "";
                    nextTitle = summary;
                    nextTime = formatDateTime(start) + " — " + formatDateTime(end);
                } else {
                    // Set current event to first event if it's happening right now.
                    currentTitle = summary;
                    currentTime = formatDateTime(start) + " — " + formatDateTime(end);
                    if (events.size() > 1) // Get the next event after this one
                        getNextEvent();
                }
            }
        }
    }

    /**
     * Takes the second index of APIOutList (the List of Events generated by the API)
     * and sets nextEventTitle and nextEventTime.
     */
    private void getNextEvent() {
        try {
            String nextEventSummary = events.get(1).getSummary();
            DateTime nextEventStart = events.get(1).getStart().getDateTime();
            DateTime nextEventEnd = events.get(1).getEnd().getDateTime();
            if (nextEventStart == null) {
                // All-day events don't have start times, so just use
                // the start date.
                nextEventStart = events.get(1).getStart().getDate();
            }
            nextTitle = nextEventSummary;
            nextTime = formatDateTime(nextEventStart) + " — " + formatDateTime(nextEventEnd);
        } catch (Exception e) {
            nextTitle = "";
            nextTime = "";
        }
    }

    /**
     * Method to format DateTimes into human-readable strings
     *
     * @param dateTime: DateTime to make readable
     * @return: HH:MM on YYYY/MM/DD
     */
    private String formatDateTime(DateTime dateTime) {
        if (dateTime.isDateOnly()){
            return dateTime.toString();
        }else{
            String[] t = dateTime.toString().split("T");
            String time = t[1].substring(0, 5);
            String[] date = t[0].split("-");
            String dateString = date[0] + "/" + date[1] + "/" + date[2];

            return time + " on " + dateString;
        }
    }

    private void infoPrint(String info) {
        System.out.println("STAT_: " + info);
    }
}
