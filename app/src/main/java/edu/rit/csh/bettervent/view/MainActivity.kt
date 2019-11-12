package edu.rit.csh.bettervent.view

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.services.calendar.CalendarScopes
import edu.rit.csh.bettervent.R
import edu.rit.csh.bettervent.view.companion.CompanionActivity
import edu.rit.csh.bettervent.view.kiosk.MainActivity
import edu.rit.csh.bettervent.viewmodel.CompanionActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_location_alert.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import java.util.*

class MainActivity : AppCompatActivity(){
    // This MainActivity gets the data from the API, and holds it
    // as a list. The Fragments then update themselves using that.
    private lateinit var mAppSettings: SharedPreferences
    private lateinit var calendar: CalendarInfo
    lateinit var signInClient: GoogleSignInClient
    lateinit var model: CompanionActivityViewModel


    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private val isGooglePlayServicesAvailable: Boolean
        get() {
            val connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
            if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
                showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
                return false
            } else if (connectionStatusCode != ConnectionResult.SUCCESS) {
                return false
            }
            return true
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load up app settings to fetch passwords and background colors.
        mAppSettings = applicationContext.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)!!

        model = ViewModelProviders.of(this).get(CompanionActivityViewModel::class.java)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope("https://www.googleapis.com/auth/calendar.readonly"))
                .requestEmail()
                .build()

        calendar = CalendarInfo(
                mAppSettings.getString("calendarName", "")!!,
                mAppSettings.getString("edu.rit.csh.bettervent.calendarid", "")!!
        )

        calendar_name_tv.text = calendar.name

        signInClient = GoogleSignIn.getClient(this, gso)

        companion_btn.setOnClickListener { startCompanionActivity() }
        kiosk_btn.setOnClickListener { startEventActivity() }
        checkForAccount()

        choose_account_btn.setOnClickListener { signOutThenIn() }

        choose_calendar_btn.setOnClickListener { promptChooseCalendar() }
    }

    private fun startCompanionActivity() {
        if (calendar.name.isBlank()) {
            alert{
                title = "You must select a calendar"
                okButton {  }
            }.show()
            return
        }

        mAppSettings.edit()
                .putString("edu.rit.csh.bettervent.calendarid", calendar.id)
                .putString("calendarName", calendar.name)
                .apply()

        val intent = Intent(this, CompanionActivity::class.java)
        startActivity(intent)
    }

    private fun startEventActivity() {
        if (calendar.name.isBlank()) {
            alert{
                title = "You must input a valid calendar ID"
                okButton {  }
            }.show()
            return
        }
        
        mAppSettings.edit()
                .putString("edu.rit.csh.bettervent.calendarid", calendar.id)
                .putString("calendarName", calendar.name)
                .apply()


        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     * activity result.
     * @param data Intent (containing result data) returned by incoming
     * activity result.
     */
    override fun onActivityResult( requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        infoPrint("API Request code returned: $requestCode")
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                isGooglePlayServicesAvailable
            }
            RC_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>){
        try {
            val account = task.getResult(ApiException::class.java)
            mAppSettings.edit().putString(PREF_ACCOUNT_NAME, account?.email).apply()
            checkForAccount()
            model.refreshCalendarOptions()
        } catch(e: ApiException){
            Log.w("MainActivity", "signInResult failed; code=${e.statusCode}")
        }
    }

    private fun signIn(){
        startActivityForResult(signInClient.signInIntent, RC_SIGN_IN)
    }

    /**
     * Allow the user to change accounts
     */

    private fun signOutThenIn(){
        signInClient.signOut()
                .addOnCompleteListener { signIn() }
    }

    /**
     * Checks for a signed in account in the app; if one exists, it starts the MainActivity.
     * Otherwise, it allows the user to choose an account
    */

    private fun checkForAccount(){
        val accountName = mAppSettings.getString(PREF_ACCOUNT_NAME, "")!!
        account_name_tv.text = accountName

        if (accountName.isEmpty()){
            signIn()
            Log.i("MainActivity", "Begin chooseAccount")
        } else {
            enableUI()
        }
    }

    /**
     * Enables the UI to allow the user to choose which version they want to use,
     * Kiosk or Companion
     */
    private fun enableUI() {
        main_root.visibility = View.VISIBLE
    }

    private fun promptChooseCalendar() {
        lateinit var dialog: DialogInterface
        dialog = alert {
            val v = layoutInflater.inflate(R.layout.add_location_alert, null)
            v.add_location_rv.adapter =
                    CalendarInfoAdapter(applicationContext,
                            model.calendarItems) { item ->
                        dialog.dismiss()
                        selectCalendar(item)
                    }
            v.add_location_rv.layoutManager = LinearLayoutManager(applicationContext)
            customView = v
        }.show()
    }

    private fun selectCalendar(cal: CalendarInfo){
        calendar = cal
        calendar_name_tv.text = cal.name
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     * Google Play Services on this device.
     */
    private fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
        runOnUiThread {
            val dialog = GooglePlayServicesUtil.getErrorDialog(
                    connectionStatusCode,
                    this@MainActivity,
                    REQUEST_GOOGLE_PLAY_SERVICES)
            dialog.show()
        }
    }

    private fun infoPrint(info: Any) {
        println("MAIN_: $info")
    }

    companion object {
        internal const val REQUEST_ACCOUNT_PICKER = 1000
        internal const val RC_SIGN_IN = 1001
        internal const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        private const val PREF_ACCOUNT_NAME = "accountName"
        val SCOPES = arrayOf(CalendarScopes.CALENDAR_READONLY)
    }
}

data class Event(val summary: String, val start: Date,
                 val end: Date, val location: String)
{
    val isHappeningNow = hasStarted and !isOver

    private val isOver: Boolean
        get() {
            val now = Date()
            return now.after(end)
        }


    private val hasStarted: Boolean
        get(){
            val now = Date()
            return start.before(now)
        }
}

data class CalendarInfo(var name: String, var id: String)