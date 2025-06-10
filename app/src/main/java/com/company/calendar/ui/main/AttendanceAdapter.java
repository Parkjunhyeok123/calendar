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
    public static class Attendance {
        public String day;
        public String checkIn;
        public String checkOut;

        public Attendance(String day, String checkIn, String checkOut) {
            this.day = day;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
        }
    }

    private List<Attendance> attendanceList;

    public AttendanceAdapter(List<Attendance> attendanceList) {
        this.attendanceList = attendanceList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvCheckIn, tvCheckOut;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvCheckIn = itemView.findViewById(R.id.tvCheckIn);
            tvCheckOut = itemView.findViewById(R.id.tvCheckOut);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attendance attendance = attendanceList.get(position);
        holder.tvDay.setText(attendance.day);
        holder.tvCheckIn.setText("출 " + attendance.checkIn);
        holder.tvCheckOut.setText("퇴 " + attendance.checkOut);
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }
}