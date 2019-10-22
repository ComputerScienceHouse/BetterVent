package edu.rit.csh.bettervent.view.companion

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.rit.csh.bettervent.R
import kotlinx.android.synthetic.main.location_view.view.*

class LocationAdapter(val context: Context, private val statuses: List<RoomStatus>):
        RecyclerView.Adapter<LocationAdapter.ViewHolder>(){

    override fun getItemCount(): Int = statuses.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.location_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val status = statuses[position]

        holder.itemView.location_tv.text = status.location
        if (status.isBusy){
            holder.itemView.card_view.setCardBackgroundColor(context.getColor(R.color.CSHRed))
            holder.itemView.title_tv.text = status.title
            holder.itemView.time_tv.text = status.timeString
        } else {
            holder.itemView.card_view.setCardBackgroundColor(context.getColor(R.color.CSHGreen))
            holder.itemView.title_tv.text = context.getString(R.string.room_open)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}