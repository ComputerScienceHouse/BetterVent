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

    private TextView mTextMessage;
    private TextView mEventTitle;
    private TextView mEventTime;
    private TextView mNextTitle;
    private TextView mNextTime;

    // AAA
    public String textMessage;
    public String eventTitle;
    public String eventTime;
    public String nextTitle;
    public String nextTime;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        System.out.println("########Loaded Status Fragment.");

        Bundle args = getArguments();
        if (args != null) {
            System.out.println("*** Found data: " + args.getString("eventTitle"));
            textMessage = args.getString("textMessage");
            eventTitle = args.getString("eventTitle");
            eventTime = args.getString("eventTime");
            nextTitle = args.getString("nextTitle");
            nextTime = args.getString("nextTime");
        }else{
            System.out.println("*** An impossibility has occurred. Go get your Nobel Prize.");
        }

        View view = inflater.inflate(R.layout.fragment_status, container, false);

        mStatusLayout = view.findViewById(R.id.status_layout);

        mTextMessage = view.findViewById(R.id.message);
        mReserved = view.findViewById(R.id.reserved_label);
        mFree = view.findViewById(R.id.free_label);

        mEventTitle = view.findViewById(R.id.event_title);
        mEventTime = view.findViewById(R.id.event_time);

        mNextTitle = view.findViewById(R.id.next_event_title);
        mNextTime = view.findViewById(R.id.next_event_time);

        if (eventTitle == null){
            textMessage = eventTitle = eventTime = nextTitle = nextTime = "";
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        System.out.println("*** Fragment Event Title: " + eventTitle); // TODO: The fragment is STILL not getting updates. Fuck.
        if (!eventTitle.equals("")) {
            setRoomStatus(eventTitle, eventTime);
            if (!nextTitle.equals("")) {
                setRoomFuture(nextTitle, nextTime);
            }
        } else setRoomFree();
    }

    public void updateViews(String textMessage, String eventTitle, String eventTime, String nextTitle, String nextTime){
        this.textMessage = textMessage;
        this.eventTitle = eventTitle;
        this.eventTime = eventTime;
        this.nextTitle = nextTitle;
        this.nextTime = nextTime;
        System.out.println("*** Ran updateViews.");
    }

    public void setRoomFree() {
        mReserved.setVisibility(View.INVISIBLE);
        mFree.setVisibility(View.VISIBLE);
        mEventTitle.setText("");
        mEventTime.setText("");
        if (!nextTitle.equals("")){
            mNextTitle.setText(nextTime);
            mNextTime.setText("");
        }else{
            mNextTitle.setText("There are no upcoming events.");
            mNextTime.setText("");
        }
        mStatusLayout.setBackgroundColor(getResources().getColor(R.color.CSHGreen));
    }

    public void setRoomStatus(String currentTitle, String currentTime){
        mFree.setVisibility(View.INVISIBLE);
        mReserved.setVisibility(View.VISIBLE);
        mEventTitle.setText(currentTitle); // mEventTitle is null when this method is caused, thus invoking a NullPointerException.
        mEventTime.setText(currentTime);
        mStatusLayout.setBackgroundColor(getResources().getColor(R.color.CSHRed));
    }

    public void setRoomFuture(String nextTitle, String nextTime){
        mNextTitle.setText(nextTitle);
        mNextTime.setText(nextTime);
    }
}
