package com.campustribune.event.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.campustribune.R;
import com.campustribune.beans.Event;
import com.campustribune.event.utility.Utility;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by aditi on 08/07/16.
 */
public class ViewEventAdapter extends ArrayAdapter<Event>{

    private Context ctx;

    public interface ViewEachEventInterface{
        void viewEventDetail(Event event);
    }

    private ViewEachEventInterface myEventInterface;

    public ViewEventAdapter(Context ctx, ArrayList<Event> events, Activity activity){
        super(ctx, 0, events);
        this.ctx=ctx;
        try{
            this.myEventInterface = (ViewEachEventInterface)activity;
        }catch (ClassCastException ex){
            System.out.println("ViewAllEventsActivity must implement ViewEachEventInterface!");
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Event event = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_event, parent, false);
        }

        TextView date = (TextView)convertView.findViewById(R.id.each_event_date);
        TextView title = (TextView)convertView.findViewById(R.id.each_event_title);
        TextView location = (TextView)convertView.findViewById(R.id.each_event_location);
        TextView time = (TextView)convertView.findViewById(R.id.each_event_time);
        ImageView image = (ImageView)convertView.findViewById(R.id.each_event_image);

        date.setText(Utility.getFormattedDate(event.getStartDate(), event.getEndDate()));
        title.setText(event.getTitle());
        location.setText("at "+ event.getAddress());
        time.setText(Utility.getFormattedTime(event.getStartDate(), event.getEndDate()));
        if(event.getEventImageS3URL()!=null){
            Picasso.with(ctx).load(event.getEventImageS3URL()).into(image);
        }
        else{
            image.setVisibility(View.INVISIBLE);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewEventAdapter.this.myEventInterface.viewEventDetail(event);
            }
        });

        return convertView;
    }
}
