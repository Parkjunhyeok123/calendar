package com.company.calendar.ui.attendance;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.company.calendar.R;
import com.company.calendar.ui.login.UserAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class AttendanceActivity extends AppCompatActivity {

    private TextView statusTextView, currentTimeTextView, workTimeTextView, infoTextView;
    private Button checkInButton, checkOutButton;
    private UserAccount currentUser;

    private Handler timeHandler = new Handler();
    private Runnable timeRunnable;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference attendanceRef;
    private DatabaseReference userRef;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private String todayDateString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        statusTextView = findViewById(R.id.statusTextView);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        workTimeTextView = findViewById(R.id.workTimeTextView);
        infoTextView = findViewById(R.id.infoTextView);

        checkInButton = findViewById(R.id.checkInButton);
        checkOutButton = findViewById(R.id.checkOutButton);

        // Firebase 인증 초기화 및 현재 로그인 유저 가져오기
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(this, "로그인된 사용자가 없습니다. 다시 로그인 해주세요.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String uid = firebaseUser.getUid();

        // 기본 UserAccount 객체 생성 (추후 DB에서 실제 정보 로드)
        currentUser = new UserAccount(uid, "사용자", "", 0, "부서", uid, "휴무중");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        todayDateString = sdf.format(Calendar.getInstance().getTime());

        firebaseDatabase = FirebaseDatabase.getInstance();

        attendanceRef = firebaseDatabase.getReference("attendance")
                .child(currentUser.getUid())
                .child(todayDateString);

        userRef = firebaseDatabase.getReference("UserAccount")
                .child(currentUser.getUid());

        // 실제 유저 정보 불러오기 (이름, 번호, 부서, 상태 등)
        loadUserInfo();

        // 출퇴근 기록 불러오기
        loadAttendanceDataFromFirebase();

        // 출근 가능 시간 지났으면 상태 자동 변경
        checkAutoUpdateStatus();

        checkInButton.setOnClickListener(v -> {
            if ("근무중".equals(currentUser.getStatus())) {
                Toast.makeText(this, "이미 출근하셨습니다", Toast.LENGTH_SHORT).show();
                return;
            }
            String now = getCurrentTimeString();
            currentUser.setStatus("근무중");
            saveAttendanceToFirebase("근무중", now, null);
            Toast.makeText(this, "출근 완료: " + now, Toast.LENGTH_SHORT).show();
        });

        checkOutButton.setOnClickListener(v -> {
            if (!"근무중".equals(currentUser.getStatus())) {
                Toast.makeText(this, "출근하지 않았습니다", Toast.LENGTH_SHORT).show();
                return;
            }
            String now = getCurrentTimeString();
            currentUser.setStatus("휴무중");
            // 저장된 출근 시간 사용
            saveAttendanceToFirebase("휴무중", savedCheckInTime, now);
            Toast.makeText(this, "퇴근 완료: " + now, Toast.LENGTH_SHORT).show();
        });


        timeRunnable = new Runnable() {
            @Override
            public void run() {
                String currentTime = getCurrentTimeString();
                currentTimeTextView.setText("현재 시간: " + currentTime);
                updateButtonVisibility();
                timeHandler.postDelayed(this, 1000);
            }
        };
        timeHandler.post(timeRunnable);
    }

    private void loadUserInfo() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String number = snapshot.child("number").getValue(String.class);
                String team = snapshot.child("team").getValue(String.class);
                String status = snapshot.child("status").getValue(String.class);

                if (name != null) currentUser.setName(name);
                if (number != null) currentUser.setNumber(number);
                if (team != null) currentUser.setTeam(team);
                if (status != null) currentUser.setStatus(status);

                statusTextView.setText("상태: " + currentUser.getStatus());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AttendanceActivity.this, "유저 정보 로드 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String savedCheckInTime;  // 필드로 추가

    private void loadAttendanceDataFromFirebase() {
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.child("status").getValue(String.class);
                String checkIn = snapshot.child("checkIn").getValue(String.class);
                String checkOut = snapshot.child("checkOut").getValue(String.class);

                if (status == null) status = "휴무중";

                currentUser.setStatus(status);
                statusTextView.setText("상태: " + status);

                savedCheckInTime = checkIn;  // 출근 시간 저장

                updateWorkTimeText(checkIn, checkOut);
                updateButtonVisibility();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AttendanceActivity.this, "출퇴근 데이터 로드 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void saveAttendanceToFirebase(String status, String checkInTime, String checkOutTime) {
        attendanceRef.child("status").setValue(status);

        if (checkInTime != null) {
            attendanceRef.child("checkIn").setValue(checkInTime);
        }

        if (checkOutTime != null) {
            attendanceRef.child("checkOut").setValue(checkOutTime);
        }

        userRef.child("status").setValue(status);

        statusTextView.setText("상태: " + status);
        updateWorkTimeText(checkInTime, checkOutTime);
        updateButtonVisibility();
    }

    private void updateWorkTimeText(String checkIn, String checkOut) {
        if (checkIn == null) {
            workTimeTextView.setText("출퇴근 기록이 없습니다");
        } else if (checkOut == null) {
            workTimeTextView.setText("출근: " + checkIn + " / 퇴근: -");
        } else {
            workTimeTextView.setText("출근: " + checkIn + " / 퇴근: " + checkOut);
        }
    }

    private void updateButtonVisibility() {
        Calendar now = Calendar.getInstance(Locale.KOREA);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        boolean checkInAllowed = (hour == 9 && minute <= 20);
        boolean isWorking = "근무중".equals(currentUser.getStatus());

        if (checkInAllowed && !isWorking) {
            checkInButton.setVisibility(View.VISIBLE);
            checkOutButton.setVisibility(View.GONE);
            infoTextView.setVisibility(View.GONE);
        } else if (isWorking) {
            checkInButton.setVisibility(View.GONE);
            checkOutButton.setVisibility(View.VISIBLE);
            infoTextView.setVisibility(View.GONE);
        } else {
            checkInButton.setVisibility(View.GONE);
            checkOutButton.setVisibility(View.GONE);
            infoTextView.setVisibility(View.VISIBLE);
            infoTextView.setText("출근 가능 시간은 오전 9시부터 9시 20분까지입니다.");
        }
    }

    private String getCurrentTimeString() {
        Calendar cal = Calendar.getInstance();
          // 9시간 더하기 (UTC -> KST)
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(cal.getTime());
    }


    private void checkAutoUpdateStatus() {
        Calendar now = Calendar.getInstance(Locale.KOREA);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        boolean isAfterCheckInTime =  (hour > 9) || (hour == 9 && minute > 20);
        boolean isNotWorking = !"근무중".equals(currentUser.getStatus());

        if (isAfterCheckInTime && isNotWorking) {
            currentUser.setStatus("휴무중");
            saveAttendanceToFirebase("휴무중", null, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timeHandler.removeCallbacks(timeRunnable);
    }
}
