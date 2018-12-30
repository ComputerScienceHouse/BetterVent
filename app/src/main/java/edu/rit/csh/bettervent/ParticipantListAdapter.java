package edu.rit.csh.bettervent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class ParticipantListAdapter extends RecyclerView.Adapter<ParticipantListAdapter.ViewHolder> {
    private ArrayList<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context adapterContext;

    // data is passed into the constructor
    ParticipantListAdapter(Context context, ArrayList<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.adapterContext = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.participant_name, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String animal = mData.get(position);
        holder.myTextView.setText(animal);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.participant_name_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
            System.out.println("QUIC_: Item clicked.");

            AlertDialog.Builder builder = new AlertDialog.Builder(adapterContext);
            String[] options = new String[]{"Edit", "Delete", "Cancel"};
            builder.setTitle("Choose an action")
                    .setItems(options, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            switch(which){
                                case 0: // Edit
                                    AlertDialog.Builder builder = new AlertDialog.Builder(adapterContext);
                                    builder.setTitle("Edit " + mData.get(getAdapterPosition()) + "'s name");

// Set up the input
                                    final EditText input = new EditText(adapterContext);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                                    builder.setView(input);

// Set up the buttons
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String nameToAdd = input.getText().toString();
                                            mData.set(getAdapterPosition(), nameToAdd);
                                            notifyItemChanged(getAdapterPosition());
                                        }
                                    });
                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });

                                    builder.show();
                                    break;
                                case 1: // Delete
                                    mData.remove(getAdapterPosition());
                                    notifyItemRemoved(getAdapterPosition());
                                    break;
                                case 2: // Cancel
                                    dialog.cancel();
                                    break;
                            }
                        }
                    });
            builder.show();
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

//    private String[] mDataset;
//
//    // Provide a reference to the views for each data item
//    // Complex data items may need more than one view per item, and
//    // you provide access to all the views for a data item in a view holder
//    public static class MyViewHolder extends RecyclerView.ViewHolder {
//        // each data item is just a string in this case
//        public TextView mTextView;
//        public MyViewHolder(TextView v) {
//            super(v);
//            mTextView = v;
//        }
//    }
//
//    // Provide a suitable constructor (depends on the kind of dataset)
//    public ParticipantListAdapter(ArrayList<String> myDataset) {
//        mDataset = myDataset.toArray(new String[0]);
//    }
//
//    // Create new views (invoked by the layout manager)
//    @Override
//    public ParticipantListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
//                                                                  int viewType) {
//        // create a new view
//        TextView v = new TextView(parent.getContext()); // Holy fucking shit that actually worked.
//        v.setTextSize(20);
//        MyViewHolder vh = new MyViewHolder(v);
//        return vh;
//    }
//
//    // Replace the contents of a view (invoked by the layout manager)
//    @Override
//    public void onBindViewHolder(MyViewHolder holder, int position) {
//        // - get element from your dataset at this position
//        // - replace the contents of the view with that element
//        holder.mTextView.setText(mDataset[position]);
//
//    }
//
//    // Return the size of your dataset (invoked by the layout manager)
//    @Override
//    public int getItemCount() {
//        return mDataset.length;
//    }
}