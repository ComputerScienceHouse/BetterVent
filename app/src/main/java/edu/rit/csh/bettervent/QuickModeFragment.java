package edu.rit.csh.bettervent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class QuickModeFragment extends Fragment {

    private ArrayList<String> participants = new ArrayList<>();

    private ConstraintLayout quickModeLayout;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private TextView participantsLabel;
    private TextView nameSetLabel;
    private TextView eventName;
    private Button addButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        infoPrint("Loaded QuickMode Fragment.");
        View view = inflater.inflate(R.layout.fragment_quick_mode, container, false);
        MainActivity.centralClock.setTextColor(0xff000000);

        quickModeLayout = view.findViewById(R.id.quick_mode_layout);

        recyclerView = view.findViewById(R.id.participants_list);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        adapter = new ParticipantListAdapter(this.getContext(), participants);
        recyclerView.setAdapter(adapter);

        participantsLabel = view.findViewById(R.id.label_participants);

        nameSetLabel = view.findViewById(R.id.name_set_label);
        eventName = view.findViewById(R.id.event_name);
        eventName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Enter event title");

                // Set up the input
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                builder.setView(input);

                // Set up the button
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    String title = input.getText().toString();
                    eventName.setText(title);
                    //Change appearance of UI to indicate the room is reserved
                    addButton.setEnabled(true);
                    quickModeLayout.setBackgroundColor(getResources().getColor(R.color.CSHRed));
                    nameSetLabel.setTextColor(getResources().getColor(R.color.white));
                    eventName.setTextColor(getResources().getColor(R.color.white));
                    participantsLabel.setTextColor(getResources().getColor(R.color.white));
                    nameSetLabel.setVisibility(View.VISIBLE);
                    MainActivity.centralClock.setTextColor(0xffffffff);
                    eventName.setTypeface(null, Typeface.BOLD);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        addButton = view.findViewById(R.id.add_participant_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoPrint("Add button clicked.");
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Add a new participant");

                // Set up the input
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                builder.setView(input);

                // Set up the button
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nameToAdd = input.getText().toString();
                        participants.add(nameToAdd);
                        adapter.notifyDataSetChanged();
                        adapter.notifyItemInserted(participants.size() - 1);
                        infoPrint("Added new person.");
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        return view;
    }

    public void infoPrint(String info) {
        System.out.println("QUIC_: " + info);
    }
}
