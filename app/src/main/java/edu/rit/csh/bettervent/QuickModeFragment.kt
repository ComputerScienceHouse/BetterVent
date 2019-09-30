package edu.rit.csh.bettervent

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_quick_mode.*
import kotlinx.android.synthetic.main.fragment_quick_mode.view.*

import java.util.ArrayList

class QuickModeFragment : Fragment() {

    private val participants = ArrayList<String>()

    private var quickModeLayout: ConstraintLayout? = null

    private var adapter: RecyclerView.Adapter<*>? = null
    private var layoutManager: RecyclerView.LayoutManager? = null

    private var participantsLabel: TextView? = null
    private var nameSetLabel: TextView? = null
    private var addButton: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        infoPrint("Loaded QuickMode Fragment.")
        val view = inflater.inflate(R.layout.fragment_quick_mode, container, false)
        MainActivity.centralClock.setTextColor(-0x1000000)

        quickModeLayout = view.findViewById(R.id.quick_mode_layout)


        // use a linear layout manager
        layoutManager = LinearLayoutManager(this.context)
        participants_list.layoutManager = layoutManager

        // specify an adapter
        adapter = ParticipantListAdapter(this.context!!, participants)
        view.participants_list.adapter = adapter

        participantsLabel = view.findViewById(R.id.label_participants)

        nameSetLabel = view.findViewById(R.id.name_set_label)
        view.event_name.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Enter event title")

            // Set up the input
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            builder.setView(input)

            // Set up the button
            builder.setPositiveButton("OK") { dialog, which ->
                val title = input.text.toString()
                view.event_name.text = title
                //Change appearance of UI to indicate the room is reserved
                addButton!!.isEnabled = true
                quickModeLayout!!.setBackgroundColor(resources.getColor(R.color.CSHRed))
                nameSetLabel!!.setTextColor(resources.getColor(R.color.white))
                view.event_name.setTextColor(resources.getColor(R.color.white))
                participantsLabel!!.setTextColor(resources.getColor(R.color.white))
                nameSetLabel!!.visibility = View.VISIBLE
                MainActivity.centralClock.setTextColor(-0x1)
                view.event_name.setTypeface(null, Typeface.BOLD)
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
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

            builder.show()
        }

        return view
    }

    fun infoPrint(info: String) {
        println("QUIC_: $info")
    }
}
