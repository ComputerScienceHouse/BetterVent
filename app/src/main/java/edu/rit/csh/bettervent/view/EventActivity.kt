package edu.rit.csh.bettervent.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextClock
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.rit.csh.bettervent.R
import edu.rit.csh.bettervent.viewmodel.EventActivityViewModel
import kotlinx.android.synthetic.main.activity_event.*
import java.util.*

class EventActivity : AppCompatActivity(), OpenSettingsListener {
    private lateinit var statusFragment: StatusFragment
    private lateinit var scheduleFragment: ScheduleFragment
    private lateinit var quickModeFragment: QuickModeFragment
    private lateinit var fragments: List<Fragment>
    private lateinit var viewModel: EventActivityViewModel


    private val onNavigationListener = BottomNavigationView.OnNavigationItemSelectedListener {item ->
        when (item.itemId){
            R.id.navigation_status -> {
                pager.currentItem = fragments.indexOf(statusFragment)
                true
            }
            R.id.navigation_schedule -> {
                pager.currentItem = fragments.indexOf(scheduleFragment)
                true
            }
            R.id.navigation_quick_mode -> {
                pager.currentItem = fragments.indexOf(quickModeFragment)
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(EventActivityViewModel::class.java)

        setContentView(R.layout.activity_event)
        Log.i("EventActivity", "Started activity")

        refresh_button.setOnClickListener {
            viewModel.refresh { statusFragment.updateCurrentAndNextEventsInUI() }
        }

        viewModel.refresh {
            statusFragment.updateCurrentAndNextEventsInUI()
            scheduleFragment.weekView.monthChangeListener?.onMonthChange(Calendar.getInstance(), Calendar.getInstance())
            scheduleFragment.weekView.notifyDataSetChanged()
        }

        statusFragment = StatusFragment()
        scheduleFragment = ScheduleFragment()
        quickModeFragment = QuickModeFragment()
        fragments = listOf(statusFragment, scheduleFragment, quickModeFragment)

        pager.adapter = SlidingPagerAdapter(supportFragmentManager)
        centralClock = findViewById(R.id.central_clock)

        bottom_navigation.setOnNavigationItemSelectedListener(onNavigationListener)
        pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageSelected(position: Int) {
                val id = getIdFromIndex(position)
                bottom_navigation.menu.findItem(id).isChecked = true
            }

            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        })
    }

    private fun getIdFromIndex(index: Int): Int{
        return when (index){
            0 -> R.id.navigation_status
            1 -> R.id.navigation_schedule
            2 -> R.id.navigation_quick_mode
            else -> R.id.navigation_status
        }
    }

    private inner class SlidingPagerAdapter(fm: FragmentManager): FragmentStatePagerAdapter(fm){
        override fun getCount(): Int = fragments.size
        override fun getItem(p0: Int) = fragments[p0]
    }

    override fun openSettings() {
        Log.i("EventActivity", "Open settings")
    }

    companion object {
        lateinit var centralClock: TextClock
    }
}
