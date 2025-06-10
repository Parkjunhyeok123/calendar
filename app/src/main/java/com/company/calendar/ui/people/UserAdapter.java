package com.company.calendar.ui.people;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.company.calendar.ui.login.UserAccount;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<UserAccount> userList;
    private OnUserClickListener listener;

    // 생성자에서 OnUserClickListener를 받아옵니다.
    public UserAdapter(List<UserAccount> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textEmail, textTeam, textNumber,textStatus;

        public UserViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textEmail = itemView.findViewById(R.id.textEmail);
            textTeam = itemView.findViewById(R.id.textTeam);
            textNumber = itemView.findViewById(R.id.textNumber);
            textStatus = itemView.findViewById(R.id.textStatus);
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserAccount user = userList.get(position);
        holder.textName.setText("이름: " + user.getName());
        holder.textEmail.setText("아이디: " + user.getId());
        holder.textTeam.setText("팀: " + user.getTeam());
        holder.textNumber.setText("전화번호: " + user.getNumber());



        String status = "";
        try {
            status = user.getStatus();
            if (status == null) status = "정보없음";
        } catch (Exception e) {
            status = "정보없음";
        }
        holder.textStatus.setText("상태: " + status);


        // 클릭 리스너 처리
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateList(List<UserAccount> filteredList) {
        this.userList = filteredList;
        notifyDataSetChanged();
    }

    // 인터페이스 정의
    public interface OnUserClickListener {
        void onUserClick(UserAccount user);
    }
}
