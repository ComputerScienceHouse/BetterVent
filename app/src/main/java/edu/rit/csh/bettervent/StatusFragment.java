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

    private ConstraintLayout mHomeLayout;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        System.err.println("########Loaded Status Fragment.");
        View view = inflater.inflate(R.layout.fragment_status, container, false);

        mHomeLayout = view.findViewById(R.id.home_layout);

        mTextMessage = view.findViewById(R.id.message);
        mTextMessage = view.findViewById(R.id.message);

        mEventTitle = view.findViewById(R.id.event_title);
        mEventTime = view.findViewById(R.id.event_time);

        mNextTitle = view.findViewById(R.id.next_event_title);
        mNextTime = view.findViewById(R.id.next_event_time);

        textMessage = eventTitle = eventTime = nextTitle = nextTime = "";

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
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
    }

    public void setRoomFree() {
        mEventTitle.setText("Free");
        mEventTime.setText("");
        if (!nextTitle.equals("")){
            mNextTitle.setText(nextTime);
            mNextTime.setText("");
        }else{
            mNextTitle.setText("There are no upcoming events.");
            mNextTime.setText("");
        }
//        mReservedLabel.setVisibility(View.GONE);
        mHomeLayout.setBackgroundColor(getResources().getColor(R.color.CSHGreen));
    }

    public void setRoomStatus(String currentTitle, String currentTime){
        mEventTitle.setText(currentTitle); // mEventTitle is null when this method is caused, thus invoking a NullPointerException.
        mEventTime.setText(currentTime);
        mHomeLayout.setBackgroundColor(getResources().getColor(R.color.CSHRed));
    }

    public void setRoomFuture(String nextTitle, String nextTime){
        mNextTitle.setText(nextTitle);
        mNextTime.setText(nextTime);
    }
}
