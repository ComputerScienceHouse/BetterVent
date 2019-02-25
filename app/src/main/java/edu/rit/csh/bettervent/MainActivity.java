package edu.rit.csh.bettervent;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;

import java.util.ArrayList;
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
    public boolean isReserved = true;

    public Fragment mSelectedFragment;
    public BottomNavigationView mBottomNav;
    public FloatingActionButton mRefreshButton;

    // So here's the strat. This MainActivity gets the data from the API, and holds it
    // as various strings and Booleans and all that. The Fragments then update themselves using
    // that.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Following code allow the app packages to lock task in true kiosk mode
        // get policy manager
        DevicePolicyManager myDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        // get this app package name
        ComponentName mDPM = new ComponentName(this, AdminReceiver.class);

        if (myDevicePolicyManager.isDeviceOwnerApp(this.getPackageName())) {
            // get this app package name
            String[] packages = {this.getPackageName()};
            // mDPM is the admin package, and allow the specified packages to lock task
            myDevicePolicyManager.setLockTaskPackages(mDPM, packages);
        } else {
            Toast.makeText(getApplicationContext(),"Not owner", Toast.LENGTH_LONG).show();
        }

        startLockTask();


        mBottomNav = findViewById(R.id.bottom_navigation);
        mBottomNav.setOnNavigationItemSelectedListener(navListener);

        mRefreshButton = findViewById(R.id.refresh_button);

        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: figure out why you have to do this twice to make anything happen.
                    refreshResults();
                    refreshUI();
            }
        });

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

        refreshResults();
        if (mSelectedFragment == null) {
            mSelectedFragment = StatusFragment.newInstance(APIOutList);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    mSelectedFragment).commit();
        }

        // Initialize API Refresher
//        final Handler handler = new Handler();
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
////                if (credential.getSelectedAccountName() != null){
//                    refreshResults();
//                    System.out.println(" *** Refreshed.");
//                    refreshUI();
//                    handler.postDelayed(this, 30000);
////                }
//            }
//        };
//
//        //Start API Refresher
//        handler.postDelayed(runnable, 1000);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    mSelectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.navigation_status:
//                            mSelectedFragment = new StatusFragment();
                            mSelectedFragment = StatusFragment.newInstance(APIOutList);
                            break;
                        case R.id.navigation_schedule:
                            mSelectedFragment = ScheduleFragment.newInstance(APIOutList);
                            break;
                        case R.id.navigation_quick_mode:
                            mSelectedFragment = new QuickModeFragment();
                            break;
                    }

                    System.out.println("*** currentEventTitle: " + currentEventTitle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            mSelectedFragment).commit();

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
        infoPrint("API Request code returned: " + requestCode);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                    refreshResults();
                } else {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                infoPrint("Pick your account.");
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    infoPrint("Result = " + resultCode);
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    infoPrint("Account name = " + accountName);
                    if (accountName != null) {
//                        credential.setSelectedAccountName(accountName);
                        credential.setSelectedAccount(new Account(accountName, "edu.rit.csh.bettervent"));
                        infoPrint("Account name set. Account name = " + accountName);
                        infoPrint(credential.getSelectedAccountName());
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        refreshResults();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    infoPrint("Account Unspecified");
                    APIStatusMessage = "Account unspecified.";
                }

                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    if (credential.getSelectedAccountName().length() < 1)
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
        System.out.println("*** Refreshing results... ***");
        if (credential.getSelectedAccountName() == null) {
            infoPrint("No account selected.");
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                System.out.println("*** Executing APIAsyncTask. ***");
                new ApiAsyncTask(this).execute();
                // TODO: java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState ???
            } else {
                System.out.println("*** Can't refresh calendar. ***");
                APIStatusMessage = "No network connection available.";
            }
        }
    }

    public void refreshUI(){
        try {
            if (mSelectedFragment.getClass() == StatusFragment.class){
                mSelectedFragment = StatusFragment.newInstance(APIOutList);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        mSelectedFragment).commit();
                System.out.println(" *** Refreshed Status UI");
            }else if (mSelectedFragment.getClass() == ScheduleFragment.class){
                mSelectedFragment = ScheduleFragment.newInstance(APIOutList);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        mSelectedFragment).commit();
                System.out.println(" *** Refreshed Schedule UI");
            }else {
                System.out.println(" *** UI is not status.");
            }
        }catch (Exception e ){
            System.err.println("Caught Exception\n" + e.toString());
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
                APIStatusMessage = "";
                APIResultsMessage = "";
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
                    System.out.println("*** No data found. ***");
                    APIResultsMessage = "Free" ;
                    APIOutList = new ArrayList<>();
                    currentEventTitle = "";
                    currentEventTime = "";
                    nextEventTitle = "";
                    nextEventTime = "";
                } else {
                    APIStatusMessage = "API Call Complete.";
                    System.out.println("*** Events found.  *** " + dataEvents);
                    APIOutList = dataEvents;
                    isFree();
//                    getCurrentAndNextEvents();
//                    getCurrentEvent();
//                    getNextEvent();
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

    /**
     * Checks the times of the first event in APIOutList (the List of Events generated by the API)
     * and if the current time is within those times, then the room is booked
     * and if the current time is not within those times, the room is free.
     * @return: true if the current time is outside of the time of the
     * next event, and false if vice-versa.
     */
    private boolean isFree(){
        try{
            DateTime now = new DateTime(System.currentTimeMillis());
            DateTime firstEventStart = APIOutList.get(0).getStart().getDateTime();
            DateTime firstEventEnd = APIOutList.get(0).getEnd().getDateTime();
            if (now.getValue() > firstEventStart.getValue() && now.getValue() < firstEventEnd.getValue()) {
                // Then the room is currently in use.
                isReserved = true;
                return false;
            }else {
                isReserved = false;
                return true;
            }
        }catch(Exception e){
            // If something weird happens, just assume the room is free.
            isReserved = false;
            return true;
        }
    }

    private void infoPrint(Object info){
        System.out.println("MAIN_: " + info);
    }
}