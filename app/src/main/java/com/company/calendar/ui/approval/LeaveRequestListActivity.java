package com.company.calendar.ui.approval;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LeaveRequestListActivity extends AppCompatActivity implements LeaveRequestAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LeaveRequestAdapter adapter;
    private List<LeaveRequest> leaveRequests = new ArrayList<>();
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    private boolean isAdmin = false;

    private Button btnWriteLeaveRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_request_list);

        recyclerView = findViewById(R.id.recyclerViewLeaveRequests);
        progressBar = findViewById(R.id.progressBar);
        btnWriteLeaveRequest = findViewById(R.id.btnWriteLeaveRequest);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && currentUser.getEmail() != null) {
            isAdmin = currentUser.getEmail().endsWith("@admincompany.com");
        }

        adapter = new LeaveRequestAdapter(leaveRequests, isAdmin, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadLeaveRequests();

        btnWriteLeaveRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LeaveRequestListActivity.this, LeaveRequestActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadLeaveRequests() {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference = FirebaseDatabase.getInstance().getReference("leave_requests");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                leaveRequests.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    LeaveRequest request = dataSnapshot.getValue(LeaveRequest.class);
                    if (request != null) {
                        request.setDocumentId(dataSnapshot.getKey());
                        leaveRequests.add(request);
                    }
                }

                // 시작일 기준 내림차순 정렬 (필요 없으면 삭제해도 됨)
                leaveRequests.sort((r1, r2) -> r2.getStartDate().compareTo(r1.getStartDate()));

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LeaveRequestListActivity.this, "데이터 불러오기 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onItemClick(int position) {
        LeaveRequest selectedRequest = leaveRequests.get(position);

        Intent intent = new Intent(this, LeaveRequestDetailActivity.class);
        intent.putExtra("documentId", selectedRequest.getDocumentId());
        intent.putExtra("isAdmin", isAdmin);
        startActivity(intent);
    }
}
