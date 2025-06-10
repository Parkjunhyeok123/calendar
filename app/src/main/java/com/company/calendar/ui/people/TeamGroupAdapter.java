package com.company.calendar.ui.people;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.company.calendar.ui.login.UserAccount;

import java.util.ArrayList;
import java.util.List;

public class TeamGroupAdapter extends RecyclerView.Adapter<TeamGroupAdapter.TeamViewHolder> {

    private List<TeamGroup> teamGroups;
    private UserAdapter.OnUserClickListener listener;

    // listener 있는 생성자
    public TeamGroupAdapter(List<TeamGroup> teamGroups, UserAdapter.OnUserClickListener listener) {
        this.teamGroups = teamGroups;
        this.listener = listener;
    }

    // listener 없는 생성자 (선택적 클릭 리스너)
    public TeamGroupAdapter(List<TeamGroup> teamGroups) {
        this.teamGroups = teamGroups;
        this.listener = null;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_group, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        TeamGroup teamGroup = teamGroups.get(position);
        holder.teamNameTextView.setText(teamGroup.getTeamName());

        UserAdapter userAdapter = new UserAdapter(teamGroup.getMembers(), listener);
        holder.recyclerViewMembers.setAdapter(userAdapter);
    }

    @Override
    public int getItemCount() {
        return teamGroups != null ? teamGroups.size() : 0;
    }

    public void updateList(List<TeamGroup> newTeamGroups) {
        if (newTeamGroups == null) {
            this.teamGroups = new ArrayList<>();
        } else {
            this.teamGroups = newTeamGroups;
        }
        notifyDataSetChanged();
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView teamNameTextView;
        RecyclerView recyclerViewMembers;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            teamNameTextView = itemView.findViewById(R.id.textViewTeamName);
            recyclerViewMembers = itemView.findViewById(R.id.recyclerViewMembers);
            recyclerViewMembers.setLayoutManager(new LinearLayoutManager(itemView.getContext()));  // LayoutManager는 여기서 한 번만 설정
        }
    }
}
