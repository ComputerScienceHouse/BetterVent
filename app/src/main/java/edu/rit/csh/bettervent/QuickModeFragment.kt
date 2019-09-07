package edu.rit.csh.bettervent

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import java.util.ArrayList

class QuickModeFragment : Fragment() {

    private val participants = ArrayList<String>()

    private var quickModeLayout: ConstraintLayout? = null

    private var recyclerView: RecyclerView? = null
    private var adapter: RecyclerView.Adapter<*>? = null
    private var layoutManager: RecyclerView.LayoutManager? = null

    private var participantsLabel: TextView? = null
    private var nameSetLabel: TextView? = null
    private var eventName: TextView? = null
    private var addButton: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        infoPrint("Loaded QuickMode Fragment.")
        val view = inflater.inflate(R.layout.fragment_quick_mode, container, false)
        MainActivity.centralClock.setTextColor(-0x1000000)

        quickModeLayout = view.findViewById(R.id.quick_mode_layout)

        recyclerView = view.findViewById(R.id.participants_list)

        // use a linear layout manager
        layoutManager = LinearLayoutManager(this.context)
        recyclerView!!.layoutManager = layoutManager

        // specify an adapter
        adapter = ParticipantListAdapter(this.context!!, participants)
        recyclerView!!.adapter = adapter

        participantsLabel = view.findViewById(R.id.label_participants)

        nameSetLabel = view.findViewById(R.id.name_set_label)
        eventName = view.findViewById(R.id.event_name)
        eventName!!.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Enter event title")

            // Set up the input
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            builder.setView(input)

            // Set up the button
            builder.setPositiveButton("OK") { dialog, which ->
                val title = input.text.toString()
                eventName!!.text = title
                //Change appearance of UI to indicate the room is reserved
                addButton!!.isEnabled = true
                quickModeLayout!!.setBackgroundColor(resources.getColor(R.color.CSHRed))
                nameSetLabel!!.setTextColor(resources.getColor(R.color.white))
                eventName!!.setTextColor(resources.getColor(R.color.white))
                participantsLabel!!.setTextColor(resources.getColor(R.color.white))
                nameSetLabel!!.visibility = View.VISIBLE
                MainActivity.centralClock.setTextColor(-0x1)
                eventName!!.setTypeface(null, Typeface.BOLD)
            }
            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
            builder.show()
        }

        addButton = view.findViewById(R.id.add_participant_button)

        addButton!!.setOnClickListener {
            infoPrint("Add button clicked.")
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Add a new participant")

            // Set up the input
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            builder.setView(input)

            // Set up the button
            builder.setPositiveButton("OK") { dialog, which ->
                val nameToAdd = input.text.toString()
                participants.add(nameToAdd)
                adapter!!.notifyDataSetChanged()
                adapter!!.notifyItemInserted(participants.size - 1)
                infoPrint("Added new person.")
            }
            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

            builder.show()
        }

        return view
    }

    fun infoPrint(info: String) {
        println("QUIC_: $info")
    }
}
