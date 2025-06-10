package com.company.calendar.ui.people;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.company.calendar.ui.login.UserAccount;
import com.company.calendar.ui.people.TeamGroup;
import com.company.calendar.ui.people.TeamGroupAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrganizationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeamGroupAdapter teamGroupAdapter;

    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization);

        recyclerView = findViewById(R.id.recyclerViewOrganization);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        teamGroupAdapter = new TeamGroupAdapter(new ArrayList<>());

        recyclerView.setAdapter(teamGroupAdapter);

        databaseRef = FirebaseDatabase.getInstance().getReference("UserAccount");

        loadUsersFromRealtimeDatabase();
    }

    private void loadUsersFromRealtimeDatabase() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<UserAccount> allUsers = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String name = userSnapshot.child("name").getValue(String.class);
                    String number = userSnapshot.child("number").getValue(String.class);
                    String team = userSnapshot.child("team").getValue(String.class);
                    String status = userSnapshot.child("status").getValue(String.class);
                    String id = userSnapshot.child("id").getValue(String.class);

                    // 팀이 없으면 미정팀 처리 가능
                    if (team == null) {
                        team = "미정팀";
                    }

                    // UserAccount 생성 (id, avatarId 등은 null 또는 기본값 처리)
                    UserAccount user = new UserAccount(id, name, number, 0, team, null, status);
                    allUsers.add(user);

                      Log.d("FirebaseDebug", "유저: " + name + " / 팀: " + team);
                }

                // 팀별 그룹핑
                Map<String, List<UserAccount>> teamMap = new HashMap<>();
                for (UserAccount user : allUsers) {
                    String team = user.getTeam();
                    if (!teamMap.containsKey(team)) {
                        teamMap.put(team, new ArrayList<>());
                    }
                    teamMap.get(team).add(user);
                }

                List<TeamGroup> teamGroups = new ArrayList<>();
                for (Map.Entry<String, List<UserAccount>> entry : teamMap.entrySet()) {
                    teamGroups.add(new TeamGroup(entry.getKey(), entry.getValue()));
                    Log.d("FirebaseDebug", "팀: " + entry.getKey() + " / 인원수: " + entry.getValue().size());
                }

                teamGroupAdapter.updateList(teamGroups);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrganizationActivity.this, "데이터 로드 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
