package edu.rit.csh.bettervent

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*

class SettingsFragment : Fragment() {

    //Preferences file.
    private lateinit var appSettings: SharedPreferences

    private var calendarIDString = "edu.rit.csh.bettervent.calendarid"
    private var maxResultsString = "edu.rit.csh.bettervent.maxresults"
    private var filterKeywordsString = "edu.rit.csh.bettervent.filterkeywords"
    private var filterByTitleString = "edu.rit.csh.bettervent.filterbytitle"
    private var filterByLocationString = "edu.rit.csh.bettervent.filterbylocation"
    private var freeColorString = "edu.rit.csh.bettervent.freecolor"
    private var reservedColorString = "edu.rit.csh.bettervent.reservedcolor"
    private var passwordString = "edu.rit.csh.bettervent.password"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        infoPrint("Loaded Settings Fragment.")
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        appSettings = context!!.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        Log.i("test", appSettings.getString(filterKeywordsString, "test"))

        // Log the current settings and load them into the settings fields.
        infoPrint("Keywords: " + appSettings.getString(filterKeywordsString, "") + "\n" +
                "Filtering by Title/Location " + appSettings.getBoolean(filterByTitleString, false) + " / " + appSettings.getBoolean(filterByLocationString, false) + "\n" +
                "ColorFree: " + appSettings.getString(freeColorString, "") + "\n" +
                "ColorReserved: " + appSettings.getString(reservedColorString, "") + "\n" +
                "Password: [REDACTED]"
        )

        EventActivity.centralClock.setTextColor(-0x1000000)

        view.calendar_id_prompt.setText(appSettings.getString(calendarIDString, ""))
        view.max_results_prompt.setText(appSettings.getString(maxResultsString, ""))
        view.filtering_keywords_prompt.setText(appSettings.getString(filterKeywordsString, ""))
        if (appSettings.getBoolean(filterByTitleString, false)) {
            view.title_filter_option.isChecked = true
            view.location_filter_option.isChecked = false
        } else if (appSettings.getBoolean(filterByLocationString, false)) {
            view.title_filter_option.isChecked = false
            view.location_filter_option.isChecked = true
        }
        view.free_color.setText(appSettings.getString(freeColorString, ""))
        view.reserved_color.setText(appSettings.getString(reservedColorString, ""))
        view.password_prompt.setText(appSettings.getString(passwordString, ""))


        // Update settings when they lose focus
        view.calendar_id_prompt.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            // When focus is lost run code.
            if (!hasFocus) appSettings.edit().putString(calendarIDString, view.calendar_id_prompt.text.toString()).apply()
        }

        view.max_results_prompt.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            // When focus is lost run code.
            if (!hasFocus) appSettings.edit().putString(maxResultsString, view.max_results_prompt.text.toString()).apply()
        }

        view.filtering_keywords_prompt.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            // When focus is lost run code.
            if (!hasFocus) appSettings.edit().putString(filterKeywordsString, view.filtering_keywords_prompt.text.toString()).apply()
        }

        view.title_filter_option.setOnClickListener {
            appSettings.edit().putBoolean(filterByTitleString, true).apply()
            appSettings.edit().putBoolean(filterByLocationString, false).apply()
        }

        view.location_filter_option.setOnClickListener {
            appSettings.edit().putBoolean(filterByTitleString, false).apply()
            appSettings.edit().putBoolean(filterByLocationString, true).apply()
        }

        view.free_color.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus -> if (!hasFocus) appSettings.edit().putString(freeColorString, view.free_color.text.toString()).apply() }

        view.reserved_color.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus -> if (!hasFocus) appSettings.edit().putString(reservedColorString, view.reserved_color.text.toString()).apply() }

        view.password_prompt.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus -> if (!hasFocus) appSettings.edit().putString(passwordString, view.password_prompt.text.toString()).apply() }

        return view
    }

    private fun infoPrint(info: String) {
        println("SETT_: $info")
    }
}