package edu.rit.csh.bettervent.view

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextClock
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProviders
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Events
import edu.rit.csh.bettervent.R
import edu.rit.csh.bettervent.viewmodel.EventActivityViewModel
import kotlinx.android.synthetic.main.activity_event.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.collections.ArrayList

class EventActivity : AppCompatActivity(), OpenSettingsListener {
    private lateinit var statusFragment: StatusFragment
    private lateinit var scheduleFragment: ScheduleFragment
    private lateinit var quickModeFragment: QuickModeFragment
    private lateinit var fragments: List<Fragment>
    private lateinit var viewModel: EventActivityViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(EventActivityViewModel::class.java)

        setContentView(R.layout.activity_event)
        Log.i("EventActivity", "Started activity")

        refresh_button.setOnClickListener {
            viewModel.refresh()
        }

        viewModel.refresh()

        statusFragment = StatusFragment()
        scheduleFragment = ScheduleFragment()
        quickModeFragment = QuickModeFragment()
        fragments = listOf(statusFragment, scheduleFragment, quickModeFragment)

        pager.adapter = SlidingPagerAdapter(supportFragmentManager)
        centralClock = findViewById(R.id.central_clock)
    }

    private inner class SlidingPagerAdapter(fm: FragmentManager): FragmentStatePagerAdapter(fm){
        override fun getCount(): Int = fragments.size
        override fun getItem(p0: Int): Fragment {
            Log.i("MainActivity", "Swipe index: $p0")
            bottom_navigation.selectedItemId = p0
            return fragments[p0]
        }
    }

    override fun openSettings() {
        Log.i("EventActivity", "Open settings")
    }

    companion object {
        lateinit var centralClock: TextClock
    }
}
