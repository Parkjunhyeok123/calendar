package com.company.calendar.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events = new ArrayList<>();

    // 외부에서 리스트를 세팅할 수 있도록 하는 메서드
    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged(); // 데이터가 바뀌었으니 갱신
    }



    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.titleTextView.setText(event.getTitle());

        // Event의 Timestamp를 가져와서 Date로 변환하고, 그 날짜를 표시
        if (event.getTimestamp() != null) {
            Timestamp timestamp = event.getTimestamp();
            Date eventDate = timestamp.toDate();

            // 날짜 + 시/분 포맷 설정 (예: 2025-04-29 14:30)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); // KST 시간대로 설정
            String formattedDate = sdf.format(eventDate);

            holder.dateTextView.setText(formattedDate); // 날짜 + 시간 표시
        }
    }


    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView dateTextView; // 날짜를 표시할 TextView 추가

        EventViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.eventTitle); // item_event.xml 안의 TextView ID
            dateTextView = itemView.findViewById(R.id.eventDate); // 날짜를 표시할 TextView ID
        }
    }
}
