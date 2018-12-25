package edu.rit.csh.bettervent;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StatusFragment extends Fragment {

    private ConstraintLayout mStatusLayout;
    private TextView mReserved;
    private TextView mFree;
    private TextView mNext;

    private TextView mEventTitle;
    private TextView mEventTime;
    private TextView mNextTitle;
    private TextView mNextTime;

    // TODO: Delet. See instructions below.
    public String textMessage;
    public String eventTitle;
    public String eventTime;
    public String nextTitle;
    public String nextTime;

    // TODO: Serialize the events and then do all the parsing in here. Simplify the shit out of the
    // TODO: MainActivity. Its only job should be to get the events and control the fragments.

    /**
     * Create a new instance of StatusFragment, passing in variables and Objects by bundling them and
     * setting arguments.
     * @param textMessage: A message output by the API for diagnostic information.
     * @param eventTitle: The title of the current event.
     * @param eventTime: The start time and end time of the current event.
     * @param nextTitle: The title of the next event.
     * @param nextTime: The start time and end time of the next event.
     * @return: A StatusFragment with the above information bundled into it.
     */
    public static StatusFragment newInstance(String textMessage, String eventTitle, String eventTime, String nextTitle, String nextTime){
        StatusFragment f = new StatusFragment();
        Bundle args = new Bundle();
        args.putString("textMessage", textMessage);
        args.putString("eventTitle", eventTitle);
        args.putString("eventTime", eventTime);
        args.putString("nextTitle", nextTitle);
        args.putString("nextTime", nextTime);
        f.setArguments(args);
        return f;
    }

    /**
     * Extract information from the bundle that may have been provided with the StatusFragment,
     * inflate status_layout and set it as the currently active view, then make references to all of
     * the various pieces of the UI so that the class can update the UI with the API data.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        System.out.println("STAT_: Loaded Status Fragment.");

        Bundle args = getArguments();
        if (args != null) {
            System.out.println("STAT_: Found data: " + args.getString("eventTitle"));
            textMessage = args.getString("textMessage");
            eventTitle = args.getString("eventTitle");
            eventTime = args.getString("eventTime");
            nextTitle = args.getString("nextTitle");
            nextTime = args.getString("nextTime");
        }else{
            System.out.println("STAT_: An impossibility has occurred. Go get your Nobel Prize.");
        }

        View view = inflater.inflate(R.layout.fragment_status, container, false);

        mStatusLayout = view.findViewById(R.id.status_layout);

        mReserved = view.findViewById(R.id.reserved_label);
        mFree = view.findViewById(R.id.free_label);
        mNext = view.findViewById(R.id.next_label);

        mEventTitle = view.findViewById(R.id.event_title);
        mEventTime = view.findViewById(R.id.event_time);

        mNextTitle = view.findViewById(R.id.next_event_title);
        mNextTime = view.findViewById(R.id.next_event_time);

        if (eventTitle == null){
            textMessage = eventTitle = eventTime = nextTitle = nextTime = "";
        }

        if (nextTitle == null) nextTitle = "";

        return view;
    }

    /**
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        System.out.println("STAT_: Fragment Event Title: " + eventTitle); // TODO: The fragment is STILL not getting updates. Fuck.
        setRoomStatus();
    }

    /**
     *
     */
    private void setRoomFree() {
        mReserved.setVisibility(View.INVISIBLE);
        mFree.setVisibility(View.VISIBLE);
        mEventTitle.setText("");
        mEventTime.setText("");
        mStatusLayout.setBackgroundColor(getResources().getColor(R.color.CSHGreen));
    }

    /**
     *
     */
    private void setRoomStatus(){
        if (!eventTitle.equals("")){
            mFree.setVisibility(View.INVISIBLE);
            mReserved.setVisibility(View.VISIBLE);
            mEventTitle.setText(eventTitle); // mEventTitle is null when this method is caused, thus invoking a NullPointerException.
            mEventTime.setText(eventTime);
            mStatusLayout.setBackgroundColor(getResources().getColor(R.color.CSHRed));
        }else {
            setRoomFree();
        }
        setRoomFuture();
    }

    /**
     *
     */
    private void setRoomFuture(){
        if (nextTitle != ""){
            mNext.setVisibility(View.VISIBLE);
            mNextTitle.setText(nextTitle);
            mNextTime.setText(nextTime);
        }else{
            mNext.setVisibility(View.INVISIBLE);
            mNextTitle.setText("There are no upcoming events.");
            mNextTime.setText("");
        }
    }
}
