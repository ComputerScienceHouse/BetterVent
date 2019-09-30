package edu.rit.csh.bettervent

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView

import java.util.ArrayList

class ParticipantListAdapter// data is passed into the constructor
internal constructor(private val adapterContext: Context, private val mData: ArrayList<String>) : RecyclerView.Adapter<ParticipantListAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater
    private var mClickListener: ItemClickListener? = null

    init {
        this.mInflater = LayoutInflater.from(adapterContext)
    }

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.participant_name, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val animal = mData[position]
        holder.myTextView.text = animal
    }

    // total number of rows
    override fun getItemCount(): Int {
        return mData.size
    }


    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        internal var myTextView: TextView = itemView.findViewById(R.id.participant_name_text_view)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)
            println("QUIC_: Item clicked.")

            val builder = AlertDialog.Builder(adapterContext)
            val options = arrayOf("Edit", "Delete", "Cancel")
            builder.setTitle("Choose an action")
                    .setItems(options) { dialog, which ->
                        // The 'which' argument contains the index position
                        // of the selected item
                        when (which) {
                            0 // Edit
                            -> {
                                val builder = AlertDialog.Builder(adapterContext)
                                builder.setTitle("Edit " + mData[adapterPosition] + "'s name")

                                // Set up the input
                                val input = EditText(adapterContext)
                                // Specify the type of input expected.
                                input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                                builder.setView(input)

                                // Set up the buttons
                                builder.setPositiveButton("OK") { dialog, which ->
                                    val nameToAdd = input.text.toString()
                                    mData[adapterPosition] = nameToAdd
                                    notifyItemChanged(adapterPosition)
                                }
                                builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

                                builder.show()
                            }
                            1 // Delete
                            -> {
                                mData.removeAt(adapterPosition)
                                notifyItemRemoved(adapterPosition)
                            }
                            2 // Cancel
                            -> dialog.cancel()
                        }
                    }
            builder.show()
        }
    }

    // convenience method for getting data at click position
    internal fun getItem(id: Int): String {
        return mData[id]
    }

    // allows clicks events to be caught
    internal fun setClickListener(itemClickListener: ItemClickListener) {
        this.mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}