package edu.rit.csh.bettervent

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.util.DateTime

import com.google.api.services.calendar.model.*

import java.io.IOException

/**
 * An asynchronous task that handles the Google Calendar API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */

/**
 * Created by miguel on 5/29/15.
 */

class ApiAsyncTask
/**
 * Constructor.
 * @param activity MainActivity that spawned this task.
 */
internal constructor(private val mainActivity: MainActivity) : AsyncTask<Void, Void, Void>() {

    private val appSettings: SharedPreferences? = null

    /**
     * Background task to call Google Calendar API.
     * @param params no parameters needed for this task.
     */
    override fun doInBackground(vararg params: Void): Void? {
        try {
            mainActivity.clearResultsText()
            mainActivity.updateResultsText(getDataFromApi(mainActivity.calendarID!!, mainActivity.maxResults))

        } catch (availabilityException: GooglePlayServicesAvailabilityIOException) {
            //            mainActivity.showGooglePlayServicesAvailabilityErrorDialog(
            //                    availabilityException.getConnectionStatusCode()); //TODO: Display error when unable to fetch events.
            System.err.println("Error connecting to Google Play Services. Error code: " + availabilityException.connectionStatusCode)


        } catch (userRecoverableException: UserRecoverableAuthIOException) {
            mainActivity.startActivityForResult(
                    userRecoverableException.intent,
                    MainActivity.REQUEST_AUTHORIZATION)

        } catch (e: IOException) {
            mainActivity.updateStatus("The following error occurred: " + e.message)
        }

        return null
    }

    /**
     * Fetch a list of the next 10 events from the primary calendar.
     * @return List of Strings describing returned events.
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun getDataFromApi(calendarID: String, maxResults: Int): List<Event> {
        // Load up app settings to fetch passwords and background colors.
        //        System.out.println("*** Attempting to get data from API. ***");
        // List the next 10 events from the primary calendar.
        val now = DateTime(System.currentTimeMillis())
        val events = mainActivity.mService.events().list(calendarID)
                .setMaxResults(maxResults)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute()
//        println("*** items: " + events)
        return ArrayList<Event>()
    }

}