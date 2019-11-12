package edu.rit.csh.bettervent.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.rit.csh.bettervent.R
import kotlinx.android.synthetic.main.add_location_item.view.*

class CalendarInfoAdapter(val context: Context, private val items: List<CalendarInfo>, val onItemSelected: (CalendarInfo) -> Unit): RecyclerView.Adapter<CalendarInfoAdapter.ViewHolder>() {

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.add_location_tv.text = items[position].name
        holder.itemView.setOnClickListener { onItemSelected.invoke(items[position]) }
        if (position == itemCount - 1) holder.itemView.divider.visibility = View.GONE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.add_location_item, parent, false))
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
}