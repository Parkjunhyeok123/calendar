package com.company.calendar.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {
    private List<Attendance> attendanceList;

    public AttendanceAdapter(List<Attendance> list) {
        this.attendanceList = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvStatus, tvCheckIn, tvCheckOut;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCheckIn = itemView.findViewById(R.id.tvCheckIn);
            tvCheckOut = itemView.findViewById(R.id.tvCheckOut);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attendance item = attendanceList.get(position);

        holder.tvDay.setText(item.day);
        holder.tvStatus.setText(item.status);
        holder.tvCheckIn.setText(item.checkIn);
        holder.tvCheckOut.setText(item.checkOut);
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    // 내부 클래스
    public static class Attendance {
        public String day;
        public String status;
        public String checkIn;
        public String checkOut;

        public Attendance(String day, String status, String checkIn, String checkOut) {
            this.day = day;
            this.status = status;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
        }
    }
}
