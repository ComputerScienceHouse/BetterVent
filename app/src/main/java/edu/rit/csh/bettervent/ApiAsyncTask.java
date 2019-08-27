package edu.rit.csh.bettervent;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.util.List;

/**
 * An asynchronous task that handles the Google Calendar API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */

/**
 * Created by miguel on 5/29/15.
 */

public class ApiAsyncTask extends AsyncTask<Void, Void, Void> {
    private MainActivity mainActivity;

    private SharedPreferences appSettings;

    /**
     * Constructor.
     * @param activity MainActivity that spawned this task.
     */
    ApiAsyncTask(MainActivity activity) {
        this.mainActivity = activity;
    }

    /**
     * Background task to call Google Calendar API.
     * @param params no parameters needed for this task.
     */
    @Override
    protected Void doInBackground(Void... params) {
        try {
            mainActivity.clearResultsText();
            mainActivity.updateResultsText(getDataFromApi(mainActivity.calendarID));

        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
//            mainActivity.showGooglePlayServicesAvailabilityErrorDialog(
//                    availabilityException.getConnectionStatusCode()); //TODO: Display error when unable to fetch events.
            System.err.println("Error connecting to Google Play Services. Error code: "
                    + availabilityException.getConnectionStatusCode());


        } catch (UserRecoverableAuthIOException userRecoverableException) {
            mainActivity.startActivityForResult(
                    userRecoverableException.getIntent(),
                    MainActivity.REQUEST_AUTHORIZATION);

        } catch (IOException e) {
            mainActivity.updateStatus("The following error occurred: " +
                    e.getMessage());
        }
        return null;
    }

    /**
     * Fetch a list of the next 10 events from the primary calendar.
     * @return List of Strings describing returned events.
     * @throws IOException
     */
    private List<Event> getDataFromApi(String calendarID) throws IOException {
        // Load up app settings to fetch passwords and background colors.
//        System.out.println("*** Attempting to get data from API. ***");
        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = mainActivity.mService.events().list(calendarID)
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
//        System.out.println("*** items: " + items);
        return items;
    }

}