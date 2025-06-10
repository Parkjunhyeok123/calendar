package com.company.calendar.ui.approval;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.company.calendar.R;
import com.company.calendar.ui.login.UserAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LeaveRequestDetailActivity extends AppCompatActivity {

    private TextView tvUserName, tvDateRange, tvLeaveType, tvReason, tvStatus;
    private Button btnApprove, btnReject;
    private DatabaseReference dbRef;

    private String documentId;
    private String currentUserId;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_request_detail);

        tvUserName = findViewById(R.id.tvUserName);
        tvDateRange = findViewById(R.id.tvDateRange);
        tvLeaveType = findViewById(R.id.tvLeaveType);
        tvReason = findViewById(R.id.tvReason);
        tvStatus = findViewById(R.id.tvStatus);
        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);

        dbRef = FirebaseDatabase.getInstance().getReference();

        documentId = getIntent().getStringExtra("documentId");

        // 현재 로그인된 사용자 UID 가져오기
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (documentId == null || currentUserId == null) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkAccessPermission(); // 권한 확인
    }

    private void checkAccessPermission() {
        dbRef.child("leave_requests").child(documentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        LeaveRequest request = snapshot.getValue(LeaveRequest.class);
                        if (request == null) {
                            Toast.makeText(LeaveRequestDetailActivity.this, "요청서 정보 없음", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        String requestUserId = request.getUserId();

                        // 현재 사용자가 본인 요청자이면 바로 접근 허용
                        if (currentUserId.equals(requestUserId)) {
                            isAdmin = false;
                            loadLeaveRequest(request);
                            return;
                        }

                        // 관리자 권한 확인
                        dbRef.child("UserAccount").child(currentUserId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot userSnap) {
                                        UserAccount user = userSnap.getValue(UserAccount.class);
                                        if (user != null && "admin".equals(user.getRole())) {
                                            isAdmin = true;
                                            loadLeaveRequest(request);
                                        } else {
                                            Toast.makeText(LeaveRequestDetailActivity.this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(LeaveRequestDetailActivity.this, "권한 확인 실패", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LeaveRequestDetailActivity.this, "데이터 확인 실패", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void loadLeaveRequest(LeaveRequest request) {
        tvDateRange.setText(request.getStartDate() + " ~ " + request.getEndDate());
        tvLeaveType.setText("휴가 종류: " + request.getLeaveType());
        tvReason.setText("사유: " + request.getReason());
        tvStatus.setText("상태: " + request.getStatus());

        loadUserName(request.getUserId());

        if (isAdmin) {
            btnApprove.setVisibility(View.VISIBLE);
            btnReject.setVisibility(View.VISIBLE);

            btnApprove.setOnClickListener(v -> updateStatus("승인됨"));
            btnReject.setOnClickListener(v -> updateStatus("반려됨"));
        } else {
            btnApprove.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
        }
    }

    private void loadUserName(String userId) {
        dbRef.child("UserAccount").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserAccount user = snapshot.getValue(UserAccount.class);
                        if (user != null) {
                            tvUserName.setText("신청자: " + user.getName());
                        } else {
                            tvUserName.setText("신청자 정보 없음");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LeaveRequestDetailActivity.this, "사용자 정보 불러오기 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateStatus(String newStatus) {
        dbRef.child("leave_requests").child(documentId).child("status").setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "상태 변경: " + newStatus, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "상태 변경 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
