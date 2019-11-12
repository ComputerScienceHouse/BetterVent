package edu.rit.csh.bettervent.view.companion

import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.PopupMenu
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import edu.rit.csh.bettervent.R
import edu.rit.csh.bettervent.view.Event
import edu.rit.csh.bettervent.viewmodel.CompanionActivityViewModel
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_companion.*
import kotlinx.android.synthetic.main.add_location_alert.view.*
import kotlinx.android.synthetic.main.location_view.view.*
import kotlinx.android.synthetic.main.fragment_status.view.event_time
import org.jetbrains.anko.alert
import java.text.SimpleDateFormat
import java.util.*

class CompanionActivity : AppCompatActivity() {

    lateinit var viewModel: CompanionActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_companion)

        locations_rv.layoutManager = LinearLayoutManager(this)

        viewModel = ViewModelProviders.of(this).get(CompanionActivityViewModel::class.java)

        refreshViewModel()

        srl.setOnRefreshListener { refreshViewModel() }

        add_fab.setOnClickListener { promptAddLocation() }
    }

    private fun refreshViewModel() {
        srl.isRefreshing = true
        viewModel.refresh {
            locations_rv.adapter = LocationCardAdapter(this, getRoomStatusesFromMap(viewModel.eventsByLocation)) { openLocationFragment(it) }
            if (viewModel.usedLocations.isEmpty()) {
                tooltip.visibility = View.VISIBLE
                vertical_glue.visibility = View.VISIBLE
            } else {
                tooltip.visibility = View.GONE
                vertical_glue.visibility = View.GONE
            }
            srl.isRefreshing = false

            registerForContextMenu(locations_rv)
        }
    }

    private fun getRoomStatusesFromMap(map: Map<String, Event?>): List<RoomStatus> {
        return map.entries.map { entry ->
            entry.value?.let { findRoomStatus(it) }
                    ?: RoomStatus(entry.key, false, "No upcoming events", "", getColor(R.color.CSHGreen))
        }
    }

    private fun findRoomStatus(event: Event): RoomStatus {
        val timeString = formatDates(event.start, event.end)

        return if (event.isHappeningNow) {
            RoomStatus(event.location, true, "Happening now: ${event.summary}", timeString, getColor(R.color.CSHRed))
        } else {
            RoomStatus(event.location, false, "Upcoming: ${event.summary}", timeString, getColor(R.color.CSHGreen))
        }
    }

    private fun promptAddLocation() {
        lateinit var dialog: DialogInterface
        dialog = alert {
            val v = layoutInflater.inflate(R.layout.add_location_alert, null)
            v.add_location_rv.adapter =
                    LocationTextAdapter(applicationContext,
                            viewModel.allLocations.minus(viewModel.usedLocations).toList()) { location ->
                        dialog.dismiss()
                        viewModel.addUsedLocation(location)
                        refreshViewModel()
                    }
            v.add_location_rv.layoutManager = LinearLayoutManager(applicationContext)
            customView = v
        }.show()
    }

    private fun openLocationFragment(roomStatus: RoomStatus) {
        lateinit var dialog: DialogInterface
        dialog = alert {
            val v = layoutInflater.inflate(R.layout.location_view, null)
            v.location_name.text = roomStatus.location
            v.event_name.text = roomStatus.title
            v.event_time.text = roomStatus.timeString
            v.rootView.setBackgroundColor(roomStatus.color)
            v.menu_ib.setOnClickListener {
                PopupMenu(this@CompanionActivity, v.menu_ib).apply{
                    setOnMenuItemClickListener { item ->
                        when(item.itemId) {
                            R.id.delete_location -> {
                                viewModel.removeUsedLocation(roomStatus.location)
                                refreshViewModel()
                                dialog.dismiss()
                                true
                            }
                            else -> false
                        }
                    }
                    inflate(R.menu.location_menu)
                    show()
                }
            }

            customView = v
        }.show()
    }
}

fun formatDates(d1: Date, d2: Date): String {
    return when {
        d2.isToday() -> "${d1.formatJustTime()} - ${d2.formatJustTime()}"
        d1.isToday() -> "${d1.formatJustTime()} - ${d2.formatWithDay()}"
        isSameDay(d1, d2) -> "${d1.formatWithDay()} - ${d2.formatJustTime()}"
        else -> "${d1.formatWithDay()} - ${d2.formatWithDay()}"
    }
}

fun isSameDay(d1: Date, d2: Date): Boolean {

    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal1.time = d1
    cal2.time = d2
    return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
}

fun Date.isToday(): Boolean {
    return isSameDay(this, Date())
}

fun Date.formatJustTime(): String {
    val simpleTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return simpleTimeFormat.format(this)
}

fun Date.formatWithDay(): String {
    val simpleTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    return "${simpleDateFormat.format(this)} ${simpleTimeFormat.format(this)}"
}

@Parcelize
data class RoomStatus(val location: String,
                      val isBusy: Boolean,
                      val title: String,
                      val timeString: String,
                      val color: Int): Parcelable