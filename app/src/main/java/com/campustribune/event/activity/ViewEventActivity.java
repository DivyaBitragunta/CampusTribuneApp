package com.campustribune.event.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.campustribune.BaseActivity;
import com.campustribune.R;
import com.campustribune.beans.Event;
import com.campustribune.beans.EventComment;
import com.campustribune.event.adapter.EventCommentsAdapter;
import com.campustribune.event.utility.EventRestCallThread;
import com.campustribune.event.utility.Utility;
import com.campustribune.helper.ImageUploader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aditi on 16/07/16.
 * Reference to setup calendar account on an emulator: http://samuelhaddad.com/2015/06/16/add-a-calendar-on-android-emulator/
 */
public class ViewEventActivity extends BaseActivity implements OnMapReadyCallback, EventCommentsAdapter.CommentsUpdator {

    Event event = null;
    Event copyOfEvent=null;
    String previousActivity = new String();
    ProgressDialog progressDialog=null;
    static EventCommentsAdapter adapter=null;
    Menu mymenu = null;
    String token=null;
    String userId=null;

    private static final int ADD_TO_CALENDAR_REQUEST=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_event_details);

        //Retrieve the user token
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.token = new String("Token "+settings.getString("authToken", "").toString());
        this.userId = new String(settings.getString("loggedInUserId", "").toString());

        event = (Event)getIntent().getSerializableExtra("new_event");
        previousActivity = getIntent().getStringExtra("prev_activity");

        progressDialog = new ProgressDialog(this);

        if(event!=null) {
            ArrayList<EventComment> listOfEventComments = null;
            ArrayList<EventComment> listOfDeletedComments = null;

            if(event.getListOfComments()!=null && event.getListOfComments().size()>0){
                listOfEventComments = new ArrayList<>(event.getListOfComments().size());
                for(EventComment each: event.getListOfComments()){
                    EventComment comment = new EventComment(each.getId(), each.getEventId(), each.getCreatedBy(),
                            each.getCreatedOn(), each.getComment(), each.getReportedBy());
                    listOfEventComments.add(comment);
                }
            }

            if(event.getListOfDeletedComments()!=null)
                listOfDeletedComments = new ArrayList<>();

            copyOfEvent = new Event(event.getId(), event.getTitle(), event.getDescription(), event.getCategory(), event.getUrl(),
                    event.getStartDate(), event.getEndDate(), event.getLatitude(), event.getLongitude(), event.getAddress(),
                    event.getEventImageS3URL(), event.isUpvoted(), event.isDownvoted(), event.isFollow(), event.isGoing(),
                    event.isNotGoing(), event.isReported(), event.getUpVoteCount(), event.getDownVoteCount(),
                    event.getGoingCount(), event.getNotGoingCount(), event.isUpdateEvent(), event.isUpdateComments(), event.isDeleteComments(),
                    event.getCreatedBy(), event.getUpdatedBy(), event.getCreatedOn(), listOfEventComments, listOfDeletedComments);  // To be used with the update event operation

            System.out.println("EQUALS RESULT --------------> "+ event.equals(copyOfEvent));

            TextView textView = (TextView) findViewById(R.id.view_event_title);
            textView.setText(event.getTitle());

            textView = (TextView)findViewById(R.id.view_event_description);
            textView.setText(event.getDescription());

            ImageView imageView = (ImageView)findViewById(R.id.event_image_1);
            if(event.getEventImageS3URL()!=null){
                Picasso.with(getApplicationContext()).load(event.getEventImageS3URL()).into(imageView);
            }
            else{
                imageView.setVisibility(View.INVISIBLE);
            }

            ImageButton upvote = (ImageButton)findViewById(R.id.event_upvote);
            upvote.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if(((String)view.getTag()).equals("not_upvoted")) {
                       // ((ImageButton)view).setImageResource(R.drawable.ic_action_upvoted);
                        ((ImageButton)view).setColorFilter(Color.argb(255, 0, 153, 51));
                        view.setTag(new String("upvoted"));
                        event.setUpvoted(true);
                    }
                    else if(((String)view.getTag()).equals("upvoted")){
                        //((ImageButton)view).setImageResource(R.drawable.ic_action_upvote);
                        ((ImageButton)view).setColorFilter(Color.argb(255, 255, 255, 255));
                        view.setTag(new String("not_upvoted"));
                        event.setUpvoted(false);
                    }

                }
            });

            ImageButton downvote = (ImageButton)findViewById(R.id.event_downvote);
            downvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(((String)view.getTag()).equals("not_downvoted")) {
                        //((ImageButton)view).setImageResource(R.drawable.ic_action_downvoted);
                        ((ImageButton)view).setColorFilter(Color.argb(255, 255, 0, 0));
                        view.setTag(new String("downvoted"));
                        event.setDownvoted(true);
                    }
                    else if(((String)view.getTag()).equals("downvoted")){
                       // ((ImageButton)view).setImageResource(R.drawable.ic_action_downvote);
                        ((ImageButton)view).setColorFilter(Color.argb(255, 255, 255, 255));
                        view.setTag(new String("not_downvoted"));
                        event.setDownvoted(false);
                    }
                }
            });

            ImageButton calendar = (ImageButton)findViewById(R.id.event_addToCalendar);
            calendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(event.getStartDate()!=null && event.getEndDate()!=null
                            && (!event.getStartDate().isEmpty()) && (!event.getEndDate().isEmpty())) {
                        Intent addToCalendarIntent = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);
                        addToCalendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, Utility.getTimeInMiliSeconds(event.getStartDate()));
                        addToCalendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, Utility.getTimeInMiliSeconds(event.getEndDate()));
                        addToCalendarIntent.putExtra(CalendarContract.Events.TITLE, event.getTitle());
                        addToCalendarIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.getAddress());
                        addToCalendarIntent.putExtra(new String("finishActivityOnSaveCompleted"), true);

                        PackageManager packageManager = getPackageManager();
                        List activities = packageManager.queryIntentActivities(addToCalendarIntent, PackageManager.MATCH_DEFAULT_ONLY);
                        if(activities.size()>0){
                            startActivityForResult(addToCalendarIntent, ViewEventActivity.ADD_TO_CALENDAR_REQUEST);
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "No calendar app found on your device, you can download it from Google Play Store", Toast.LENGTH_LONG).show();
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Cannot add the event to the calendar, start and end dates are not defined", Toast.LENGTH_LONG).show();
                    }

                }
            });

            ImageButton follow = (ImageButton)findViewById(R.id.event_follow);
            follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(((String)view.getTag()).equals("not_follow")) {
                        //((ImageButton)view).setImageResource(R.drawable.ic_action_following);
                        ((ImageButton)view).setColorFilter(Color.argb(255, 0, 102, 255));
                        view.setTag(new String("follow"));
                        event.setFollow(true);
                    }
                    else if(((String)view.getTag()).equals("follow")){
                       // ((ImageButton)view).setImageResource(R.drawable.ic_action_follow);
                        ((ImageButton)view).setColorFilter(Color.argb(255, 255, 255, 255));
                        view.setTag(new String("not_follow"));
                        event.setFollow(false);
                    }
                }
            });

            ImageButton going = (ImageButton)findViewById(R.id.event_going);
            going.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(((String)view.getTag()).equals("not_going")) {
                        //((ImageButton)view).setImageResource(R.drawable.ic_action_markedgoing);
                        ((ImageButton)view).setColorFilter(Color.argb(255, 0, 153, 51));
                        view.setTag(new String("going"));
                        event.setGoing(true);
                    }
                    else if(((String)view.getTag()).equals("going")){
                        //((ImageButton)view).setImageResource(R.drawable.ic_action_markgoing);
                        ((ImageButton)view).setColorFilter(Color.argb(255, 255, 255, 255));
                        view.setTag(new String("not_going"));
                        event.setGoing(false);
                    }
                }
            });

            ImageButton notgoing = (ImageButton)findViewById(R.id.event_notgoing);
            notgoing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(((String)view.getTag()).equals("not_notgoing")) {
                       // ((ImageButton)view).setImageResource(R.drawable.ic_action_markednotgoing);
                        ((ImageButton)view).setColorFilter(Color.argb(255, 255, 0, 0));
                        view.setTag(new String("notgoing"));
                        event.setNotGoing(true);
                    }
                    else if(((String)view.getTag()).equals("notgoing")){
                       // ((ImageButton)view).setImageResource(R.drawable.ic_action_marknotgoing);
                        ((ImageButton)view).setColorFilter(Color.argb(255, 255, 255, 255));
                        view.setTag(new String("not_notgoing"));
                        event.setNotGoing(false);
                    }
                }
            });

            ImageButton addComment = (ImageButton)findViewById(R.id.event_add_comment);
            addComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCommentsPopup(v);
                }
            });

            MapFragment map = (MapFragment)getFragmentManager().findFragmentById(R.id.view_event_location_map);
            map.getMapAsync(this);

            if(previousActivity.equals("CreateEventActivity"))
                Toast.makeText(this,"Event created successfully", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_event_menu, menu);
        if(this.previousActivity.equals("ViewAllEventsActivity")){
            MenuItem home = menu.findItem(R.id.view_event_home_action);
            home.setVisible(false);

            MenuItem back = menu.findItem(R.id.view_event_back_action);
            back.setVisible(true);
        }
        this.mymenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.view_event_home_action:
            {
                EventRestCallThread myRestClient = new EventRestCallThread(getApplicationContext(), new String("create"), event, this.token);
                myRestClient.start();
                // Add code to navigate to the front page
                ViewEventActivity.this.finish();
                return true;
            }
            case R.id.view_event_back_action:
            {

                if(event.isUpvoted() || event.isDownvoted() || event.isFollow() || event.isGoing()
                        || event.isNotGoing())
                    event.setUpdateEvent(true);

                if(event.isUpdateEvent() || event.isUpdateComments() || event.isDeleteComments()) {
                    event.setUpdatedBy(this.userId);
                    if (event.getId() != null && event.getCreatedBy() != null && (!event.getCreatedBy().isEmpty())) {
                        EventRestCallThread myRestClient = new EventRestCallThread(getApplicationContext(), new String("update"), event, this.token);
                        myRestClient.start();
                        event.setListOfDeletedComments(null);
                        ViewAllEventsActivity.updateEventList(copyOfEvent, event);

                    } else {
                        Toast.makeText(getApplicationContext(), "Something went wrong..Event couldn't be updated.", Toast.LENGTH_LONG).show();
                    }
                }

                Intent viewAllEventsIntent = new Intent(ViewEventActivity.this, ViewAllEventsActivity.class);
                viewAllEventsIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(viewAllEventsIntent);

                ViewEventActivity.this.finish();
                return true;
            }
            case R.id.view_event_edit_action:
            {
                if(this.mymenu!=null){
                    MenuItem saveItem = this.mymenu.findItem(R.id.view_event_editsave_action);
                    saveItem.setVisible(true);
                    item.setVisible(false);
                }

                TextView titleView = (TextView)findViewById(R.id.view_event_title);
                titleView.setVisibility(View.INVISIBLE);

                EditText editTitle = (EditText)findViewById(R.id.view_event_edit_title);
                editTitle.setText(event.getTitle());
                editTitle.setVisibility(View.VISIBLE);

                TextView descView = (TextView)findViewById(R.id.view_event_description);
                descView.setVisibility(View.INVISIBLE);

                EditText editDesc = (EditText)findViewById(R.id.view_event_edit_desc);
                editDesc.setText(event.getDescription());
                editDesc.setVisibility(View.VISIBLE);

                return true;
            }
            case R.id.view_event_editsave_action:
            {
                EditText editTitle = (EditText)findViewById(R.id.view_event_edit_title);
                TextView titleView = (TextView)findViewById(R.id.view_event_title);
                titleView.setText(editTitle.getText());
                editTitle.setVisibility(View.INVISIBLE);
                titleView.setVisibility(View.VISIBLE);
                event.setTitle(editTitle.getText().toString());

                EditText editDesc = (EditText)findViewById(R.id.view_event_edit_desc);
                TextView descView = (TextView)findViewById(R.id.view_event_description);
                descView.setText(editDesc.getText());
                editDesc.setVisibility(View.INVISIBLE);
                descView.setVisibility(View.VISIBLE);
                event.setDescription(editDesc.getText().toString());

                event.setUpdateEvent(true);

                if(this.mymenu!=null){
                    MenuItem editItem = this.mymenu.findItem(R.id.view_event_edit_action);
                    editItem.setVisible(true);
                    item.setVisible(false);
                }

                Toast.makeText(getApplicationContext(), "Event saved successfully", Toast.LENGTH_LONG).show();

                return true;
            }
            case R.id.view_event_delete_action:
            {
                if(event.getId()!=null){
                    EventRestCallThread myRestClient = new EventRestCallThread(getApplicationContext(), new String("delete"), event, this.token);
                    myRestClient.start();
                    ViewAllEventsActivity.removeFromList(copyOfEvent);

                    Intent viewAllEventsIntent = new Intent(ViewEventActivity.this, ViewAllEventsActivity.class);
                    viewAllEventsIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(viewAllEventsIntent);

                    ViewEventActivity.this.finish();
                }
                else{
                    // Add code to navigate to the front page
                }

                Toast.makeText(getApplicationContext(),"Event deleted successfully!", Toast.LENGTH_LONG).show();
                return true;
            }
            default:
                return true;

        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(event.getLatitude(), event.getLongitude()))
                .zoom(8)
                .build();
        googleMap.clear();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(event.getLatitude(), event.getLongitude()))
                .title(event.getTitle()));
    }

    private void showCommentsPopup(View view){
        final PopupWindow popupWindow = new PopupWindow(getApplicationContext());
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.events_comments, null);
        popupWindow.setContentView(popupView);
        popupWindow.setFocusable(true);
        popupWindow.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setBackgroundDrawable(getDrawable(R.drawable.popupwindowbackground));
        popupWindow.showAtLocation(view.getRootView(), Gravity.NO_GRAVITY, 0, 0);

        View close = popupView.findViewById(R.id.popup_close_action);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        ListView listView = (ListView)popupView.findViewById(R.id.list_event_comments);
        adapter = new EventCommentsAdapter(this.getApplicationContext(), event.getListOfComments(),
                                                            view.getRootView(), ViewEventActivity.this);
        listView.setAdapter(adapter);

        View addComment = popupView.findViewById(R.id.add_event_comment_action);
        addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View rootView = v.getRootView();
                EditText comment = (EditText) rootView.findViewById(R.id.new_event_comment);
                EventComment eventComment = new EventComment(comment.getText().toString(), ViewEventActivity.this.userId);
                if (event != null) {
                    event.addComment(eventComment);
                    adapter.notifyDataSetChanged();
                    event.setUpdateComments(true);
                }
                comment.setText("");
                comment.setHint(R.string.event_new_comment);
            }
        });


    }

    @Override
    public void updateEventComment(EventComment oldComment, EventComment newComment) {
        ArrayList<EventComment> listOfComments = event.getListOfComments();
        if(listOfComments!=null && listOfComments.size()>0){
            try {
                int index = listOfComments.indexOf(oldComment);
                listOfComments.remove(index);
                listOfComments.add(index, newComment);
                event.setListOfComments(listOfComments);
                event.setUpdateComments(true);
                adapter.notifyDataSetChanged();
            }catch (Exception ex){
                System.out.println("Could not update the event comment due to "+ ex.getMessage());
            }
        }
    }

    @Override
    public void deleteEventComment(EventComment comment) {
        ArrayList<EventComment> listOfComments = event.getListOfComments();
        if(listOfComments!=null && listOfComments.size()>0){
            if(!listOfComments.remove(comment)){
                System.out.println("Event comment could not be deleted... ");
            }
            else {
                event.setListOfComments(listOfComments);
                ArrayList<EventComment> listOfDeletedComments = event.getListOfDeletedComments();
                if(listOfDeletedComments==null){
                    listOfDeletedComments = new ArrayList<EventComment>();
                }
                listOfDeletedComments.add(comment);
                event.setListOfDeletedComments(listOfDeletedComments);
                event.setDeleteComments(true);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ViewEventActivity.ADD_TO_CALENDAR_REQUEST && resultCode == RESULT_OK){
            Toast.makeText(getApplicationContext(), "Event added successfully", Toast.LENGTH_LONG).show();
        }
    }
}
