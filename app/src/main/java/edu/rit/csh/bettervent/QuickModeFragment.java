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

    private ConstraintLayout mQuickModeLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private TextView mParticipantsLabel;
    private TextView mNameSetLabel;
    private TextView mEventName;
    private Button mAddButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        infoPrint("Loaded QuickMode Fragment.");
        View view = inflater.inflate(R.layout.fragment_quick_mode, container, false);

        mQuickModeLayout = view.findViewById(R.id.quick_mode_layout);

        mRecyclerView = view.findViewById(R.id.participants_list);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mAdapter = new ParticipantListAdapter(this.getContext(), participants);
        mRecyclerView.setAdapter(mAdapter);

        mParticipantsLabel = view.findViewById(R.id.label_participants);

        mNameSetLabel = view.findViewById(R.id.name_set_label);
        mEventName = view.findViewById(R.id.event_name);
        mEventName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Enter event title");

// Set up the input
                final EditText input = new EditText(getContext());
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                builder.setView(input);

// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = input.getText().toString();
                        mAddButton.setEnabled(true);
                        mQuickModeLayout.setBackgroundColor(getResources().getColor(R.color.CSHRed));

                        mNameSetLabel.setTextColor(getResources().getColor(R.color.white));
                        mEventName.setTextColor(getResources().getColor(R.color.white));
                        mParticipantsLabel.setTextColor(getResources().getColor(R.color.white));

                        mNameSetLabel.setVisibility(View.VISIBLE);
                        mEventName.setText(title);
                        mEventName.setTypeface(null, Typeface.BOLD);
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

        mAddButton = view.findViewById(R.id.add_participant_button);

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Add a new participant");

// Set up the input
                final EditText input = new EditText(getContext());
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                builder.setView(input);

// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nameToAdd = input.getText().toString();
                        participants.add(nameToAdd);
                        mAdapter.notifyDataSetChanged();
                        mAdapter.notifyItemInserted(participants.size()-1);
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

    public void infoPrint(String info){
        System.out.println("QUIC_: " + info);
    }
}
