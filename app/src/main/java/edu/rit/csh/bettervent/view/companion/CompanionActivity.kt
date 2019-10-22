package edu.rit.csh.bettervent.view.companion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import edu.rit.csh.bettervent.R
import edu.rit.csh.bettervent.view.Event
import edu.rit.csh.bettervent.viewmodel.CompanionActivityViewModel
import edu.rit.csh.bettervent.viewmodel.EventActivityViewModel
import kotlinx.android.synthetic.main.activity_companion.*
import java.text.SimpleDateFormat
import java.util.*

class CompanionActivity : AppCompatActivity() {

    lateinit var viewModel: CompanionActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_companion)

        locations_rv.layoutManager = LinearLayoutManager(this)

        viewModel = ViewModelProviders.of(this).get(CompanionActivityViewModel::class.java)

        viewModel.refresh {
            locations_rv.adapter = LocationAdapter(this, getRoomStatusesFromMap(viewModel.eventsByLocation))
        }
    }

    private fun getRoomStatusesFromMap(map: Map<String, Event?>): List<RoomStatus> {
        return map.entries.map { entry ->
            entry.value?.let { findRoomStatus(it) } ?:
                RoomStatus(entry.key, false, "", "")
        }
    }

    private fun findRoomStatus(event: Event): RoomStatus{
        val start = formatDate(event.start)
        val end = formatDate(event.end)

        return if (event.isHappeningNow()) {
            RoomStatus(event.location, true, event.summary, "$start - $end")
        } else {
            RoomStatus(event.location, false, event.summary, "$start - $end")
        }
    }

    private fun formatDate(inputDate: Date): String {
        val simpleTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val time = simpleTimeFormat.format(inputDate)
        val date = simpleDateFormat.format(inputDate)
        return "$time on $date"
    }
}

data class RoomStatus(val location: String, val isBusy: Boolean, val title: String, val timeString: String)