package edu.rit.csh.bettervent;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An asynchronous task that handles the Google Calendar API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */

/**
 * Created by miguel on 5/29/15.
 */

public class ApiAsyncTask extends AsyncTask<Void, Void, Void> {
    private MainActivity mActivity;

    private SharedPreferences appSettings;

    /**
     * Constructor.
     * @param activity MainActivity that spawned this task.
     */
    ApiAsyncTask(MainActivity activity) {
        this.mActivity = activity;
    }

    /**
     * Background task to call Google Calendar API.
     * @param params no parameters needed for this task.
     */
    @Override
    protected Void doInBackground(Void... params) {
        try {
            mActivity.clearResultsText();
            mActivity.updateResultsText(getDataFromApi());

        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
//            mActivity.showGooglePlayServicesAvailabilityErrorDialog(
//                    availabilityException.getConnectionStatusCode()); //TODO: Make this work :3
            System.err.println("Error connecting to Google Play Services. Error code: "
                    + availabilityException.getConnectionStatusCode());


        } catch (UserRecoverableAuthIOException userRecoverableException) {
            mActivity.startActivityForResult(
                    userRecoverableException.getIntent(),
                    MainActivity.REQUEST_AUTHORIZATION);

        } catch (IOException e) {
//            System.err.println("*** The following error occurred: \n" + e.getMessage());
            mActivity.updateStatus("The following error occurred: " +
                    e.getMessage());
        }
        return null;
    }

    /**
     * Fetch a list of the next 10 events from the primary calendar.
     * @return List of Strings describing returned events.
     * @throws IOException
     */
    private List<Event> getDataFromApi() throws IOException {
//        System.out.println("*** Attempting to get data from API. ***");
        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
//        List<String> eventStrings = new ArrayList<String>();
        Events events = mActivity.mService.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
//        System.out.println("*** items: " + items);
        return items;
//        for (Event event : items) {
//            DateTime start = event.getStart().getDateTime();
//            if (start == null) {
//                // All-day events don't have start times, so just use
//                // the start date.
//                start = event.getStart().getDate();
//            }
//
//            // Add event's title and date to the event list. index n and n+1 respectively.
//            eventStrings.add(event.getSummary());
//            eventStrings.add(String.valueOf(start));
//            // So it will be added to the list as a pair. Honestly it might be better to do a tuple or something.
//
//        }
//        return eventStrings;
    }

}