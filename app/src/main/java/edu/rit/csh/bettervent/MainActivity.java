package edu.rit.csh.bettervent;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    com.google.api.services.calendar.Calendar mService;

    GoogleAccountCredential credential;
//    private TextView mStatusText;
//    private TextView mResultsText;
    private String APIOut;
    private List<Event> APIOutList;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    public String APIStatusMessage;
    public String APIResultsMessage;
    public String currentEventTitle;
    public String currentEventTime;
    public String nextEventTitle;
    public String nextEventTime;
    public boolean isReserved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new StatusFragment()).commit();
        }


        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build();

        // Initialize API Refresher
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                refreshResults();
                handler.postDelayed(this, 60000);
            }
        };

        //Start API Refresher
        handler.postDelayed(runnable, 1000);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.navigation_status:
                            selectedFragment = new StatusFragment();
                            break;
                        case R.id.navigation_schedule:
                            selectedFragment = new ScheduleFragment();
                            break;
                        case R.id.navigation_quick_mode:
                            selectedFragment = new QuickModeFragment();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();

                    return true;
                }
            };

    /**
     * Called whenever this activity is pushed to the foreground, such as after
     * a call to onCreate().
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        } else {
            APIStatusMessage = "Google Play Services required: " +
                    "after installing, close and relaunch this app.";
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                    refreshResults();
                } else {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        refreshResults();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    APIStatusMessage = "Account unspecified.";
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    refreshResults();
                } else {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempt to get a set of data from the Google Calendar API to display. If the
     * email address isn't known yet, then call chooseAccount() method so the
     * user can pick an account.
     */
    private void refreshResults() {
        if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new ApiAsyncTask(this).execute();
            } else {
                APIStatusMessage = "No network connection available.";
            }
        }
    }

    /**
     * Clear any existing Google Calendar API data from the TextView and update
     * the header message; called from background threads and async tasks
     * that need to update the UI (in the UI thread).
     */
    public void clearResultsText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                APIStatusMessage = "Retrieving data…";
                // TODO: WTF does this do?
//                APIOut(APIStatusMessage, "");
//                mResultsText.setText("");
            }
        });
    }

    /**
     * Fill the data TextView with the given List of Strings; called from
     * background threads and async tasks that need to update the UI (in the
     * UI thread).
     * @param dataEvents a List of Strings to populate the main TextView with.
     */
    public void updateResultsText(final List<Event> dataEvents) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dataEvents == null) {
                    APIStatusMessage = "Error retrieving data!";
                } else if (dataEvents.size() == 0) {
                    // TODO: Call a "setFree" method in StatusFragment or something.
                    APIStatusMessage = "No data found.";
                    APIResultsMessage = "Free" ;
                    currentEventTime = "";
//                    mHomeLayout.setBackgroundColor(getResources().getColor(R.color.CSHGreen));
                    nextEventTitle = "There are no upcoming events.";
                    nextEventTime = "";
                } else {
                    APIStatusMessage = "API Call Complete.";
//                    APIOutList = dataStrings;
//                    APIOutList(APIResultsMessage, dataEvents);
//                    isFree(APIResultsMessage);
                    // TODO: Put this into StatusFragment.
//                    getNextEvent(nextEventTitle, nextEventTime);
                }
            }
        });
    }

    /**
     * Show a status message in the list header TextView; called from background
     * threads and async tasks that need to update the UI (in the UI thread).
     * @param message a String to display in the UI header TextView.
     */
    public void updateStatus(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                APIStatusMessage = message;
            }
        });
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount() {
        startActivityForResult(
                credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode,
                        MainActivity.this,
                        REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

//    private void APIOut(String input){
//        APIOut = input;
//    }

//    private void APIOut(TextView mTargetView, String input){
//        APIOut = input;
//        mTargetView.setText(APIOut);
//    }

    private void APIOutList(TextView mTargetView, List<Event> input){
        APIOutList = input;
        DateTime start = APIOutList.get(0).getStart().getDateTime();
        if (start == null) {
            // All-day events don't have start times, so just use
            // the start date.
            start = APIOutList.get(0).getStart().getDate();
        }
        mTargetView.setText(APIOutList.get(0).getSummary());
        currentEventTime = formatDateTime(start);
    }

    private void getNextEvent(TextView mTargetView, TextView mTargetTimeView){
        try{
            String nextEvent = APIOutList.get(1).getSummary();
            DateTime nextTime = APIOutList.get(1).getStart().getDateTime();
            if (nextTime == null) {
                // All-day events don't have start times, so just use
                // the start date.
                nextTime = APIOutList.get(1).getStart().getDate();
            }

            mTargetView.setText(nextEvent);
            mTargetTimeView.setText(formatDateTime(nextTime));
        }catch (Exception e){
            mTargetView.setText("There are no upcoming events.");
            mTargetTimeView.setText("");
        }
    }

    private boolean isFree(){
        DateTime now = new DateTime(System.currentTimeMillis());
        DateTime firstEvent = APIOutList.get(0).getStart().getDateTime();
        if (now == firstEvent) return false;
        else return true;
    }

    private boolean isFree(TextView mTargetView){
        try{
            DateTime now = new DateTime(System.currentTimeMillis());
            DateTime firstEventStart = APIOutList.get(0).getStart().getDateTime();
            DateTime firstEventEnd = APIOutList.get(0).getEnd().getDateTime();
            if (now.getValue() > firstEventStart.getValue() && now.getValue() < firstEventEnd.getValue()) {
                // Then the room is currently in use.
                // TODO: Make this a method in the fragment to change
                // TODO: the layout accordingly.
//                mReservedLabel.setVisibility(View.VISIBLE);
                isReserved = true;
//            mTargetView.setText("Reserved\n" + mTargetView.getText());
//                mHomeLayout.setBackgroundColor(getResources().getColor(R.color.CSHRed));
                return false;
            }else {
                setFree(mTargetView);
                return true;
            }
        }catch(Exception e){
            setFree(mTargetView);
            return true;
        }
    }

    private void setFree(TextView mTargetView){
        // TODO: This is mostly UI stuff. Chuck this into StatusFragment.java
//        mTargetView.setText("Free");
        currentEventTitle = "Free"; // TODO: Just have a separate layout for if the room is free?
        nextEventTime = "";
//        mReservedLabel.setVisibility(View.GONE);
        isReserved = false;
//        mHomeLayout.setBackgroundColor(getResources().getColor(R.color.CSHGreen));
    }

    private String formatDateTime(DateTime dateTime){
        String[] t = dateTime.toString().split("T");
        // UGH.
//        StringBuilder dateBuilder = new StringBuilder();
//        dateBuilder.append(t[0]);
//        dateBuilder.reverse();
//
        String time = t[1].substring(0, 5);
        String[] date = t[0].toString().split("-");
        String dateString = date[0] + "-" + date[1] + "-" + date[2];

        return time + " on " + dateString;
    }

}