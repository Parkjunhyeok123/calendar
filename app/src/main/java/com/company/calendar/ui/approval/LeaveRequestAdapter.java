package com.company.calendar.ui.approval;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;

import java.util.List;

public class LeaveRequestAdapter extends RecyclerView.Adapter<LeaveRequestAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private List<LeaveRequest> leaveRequestList;
    private boolean isAdmin;
    private OnItemClickListener listener;

    public LeaveRequestAdapter(List<LeaveRequest> leaveRequestList, boolean isAdmin, OnItemClickListener listener) {
        this.leaveRequestList = leaveRequestList;
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LeaveRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leave_request, parent, false);
        return new ViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaveRequestAdapter.ViewHolder holder, int position) {
        LeaveRequest request = leaveRequestList.get(position);

        holder.tvLeaveType.setText(request.getLeaveType());
        holder.tvDateRange.setText(request.getStartDate() + " ~ " + request.getEndDate());
        holder.tvStatus.setText(request.getStatus());

        // 일반 사용자는 사유(reason)를 안 보여주고, 관리자만 보여줌
        if (isAdmin) {
            holder.tvReason.setVisibility(View.VISIBLE);
            holder.tvReason.setText("사유: " + request.getReason());
        } else {
            holder.tvReason.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return leaveRequestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLeaveType, tvDateRange, tvStatus, tvReason;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            tvLeaveType = itemView.findViewById(R.id.tvLeaveType);
            tvDateRange = itemView.findViewById(R.id.tvDateRange);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvReason = itemView.findViewById(R.id.tvReason);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(pos);
                    }
                }
            });
        }
    }
}
