package com.company.calendar.ui.event;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WeekDayAdapter extends RecyclerView.Adapter<WeekDayAdapter.WeekViewHolder> {
    private List<WeekDay> days;
    private OnDateSelectedListener listener;
    private Map<String, String> holidays;

    public interface OnDateSelectedListener {
        void onDateSelected(Date selectedDate);
    }

    public WeekDayAdapter(List<WeekDay> days, Map<String, String> holidays, OnDateSelectedListener listener) {
        this.days = days;
        this.holidays = holidays;
        this.listener = listener;
    }

    @Override
    public WeekViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_week_day, parent, false);
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WeekViewHolder holder, int position) {
        WeekDay day = days.get(position);
        String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(day.date);
        holder.textView.setText(new SimpleDateFormat("dd(E)", Locale.KOREA).format(day.date));
        holder.textView.setBackgroundColor(day.isSelected ? Color.LTGRAY : Color.TRANSPARENT);

        if (holidays != null && holidays.containsKey(dateKey)) {
            holder.textView.setTextColor(Color.RED);
        } else {
            holder.textView.setTextColor(Color.BLACK);
        }

        holder.textView.setOnClickListener(v -> {
            for (WeekDay d : days) d.isSelected = false;
            day.isSelected = true;
            notifyDataSetChanged();
            listener.onDateSelected(day.date);
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class WeekViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public WeekViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewDay);
        }
    }
}
