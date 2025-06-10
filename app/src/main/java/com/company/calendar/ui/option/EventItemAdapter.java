package com.company.calendar.ui.option;
import com.company.calendar.ui.home.Event;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;

import java.util.List;

public class EventItemAdapter extends RecyclerView.Adapter<EventItemAdapter.EventViewHolder> {

    private List<Event> events;

    public EventItemAdapter() {
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // item_event_layout.xml을 사용하여 레이아웃을 인플레이트
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_layout, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.titleTextView.setText(event.getTitle());
        holder.timestampTextView.setText(String.valueOf(event.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView timestampTextView;

        public EventViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.event_title);
            timestampTextView = itemView.findViewById(R.id.event_timestamp);
        }
    }
}
