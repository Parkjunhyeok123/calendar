package com.company.calendar.ui.approval;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.company.calendar.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LeaveRequestActivity extends AppCompatActivity {

    private MaterialButton btnStartDate, btnEndDate, btnSubmit;
    private Spinner spinnerLeaveType;
    private TextInputEditText etReason;

    private int startYear, startMonth, startDay;
    private int endYear, endMonth, endDay;

    private DatabaseReference leaveRequestsRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval);

        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnSubmit = findViewById(R.id.btnSubmit);
        spinnerLeaveType = findViewById(R.id.spinnerLeaveType);
        etReason = findViewById(R.id.etReason);

        // Firebase Realtime Database 경로 (예: "leave_requests")
        leaveRequestsRef = FirebaseDatabase.getInstance().getReference("leave_requests");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 휴가 종류 Spinner 초기화
        String[] leaveTypes = {"연차", "반차", "병가", "출장", "기타"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, leaveTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLeaveType.setAdapter(adapter);

        // 날짜 선택 버튼 클릭 리스너
        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));

        // 신청하기 버튼 클릭 리스너
        btnSubmit.setOnClickListener(v -> submitLeaveRequest());
    }

    private void showDatePicker(boolean isStartDate) {
        final Calendar c = Calendar.getInstance();
        int year, month, day;
        if (isStartDate && startYear != 0) {
            year = startYear;
            month = startMonth;
            day = startDay;
        } else if (!isStartDate && endYear != 0) {
            year = endYear;
            month = endMonth;
            day = endDay;
        } else {
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    String dateStr = selectedYear + "-" + String.format("%02d", (selectedMonth + 1)) + "-" + String.format("%02d", selectedDay);
                    if (isStartDate) {
                        startYear = selectedYear;
                        startMonth = selectedMonth;
                        startDay = selectedDay;
                        btnStartDate.setText("시작 날짜: " + dateStr);
                    } else {
                        endYear = selectedYear;
                        endMonth = selectedMonth;
                        endDay = selectedDay;
                        btnEndDate.setText("종료 날짜: " + dateStr);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void submitLeaveRequest() {
        String leaveType = spinnerLeaveType.getSelectedItem().toString();
        String reason = etReason.getText() != null ? etReason.getText().toString().trim() : "";

        if (startYear == 0 || endYear == 0) {
            Toast.makeText(this, "시작 날짜와 종료 날짜를 모두 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar start = Calendar.getInstance();
        start.set(startYear, startMonth, startDay);
        Calendar end = Calendar.getInstance();
        end.set(endYear, endMonth, endDay);

        if (start.after(end)) {
            Toast.makeText(this, "시작 날짜가 종료 날짜보다 이후일 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reason.isEmpty()) {
            Toast.makeText(this, "사유를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase에 저장할 데이터 구조 만들기
        Map<String, Object> leaveRequest = new HashMap<>();
        leaveRequest.put("userId", userId);
        leaveRequest.put("leaveType", leaveType);
        leaveRequest.put("startDate", String.format("%04d-%02d-%02d", startYear, startMonth + 1, startDay));
        leaveRequest.put("endDate", String.format("%04d-%02d-%02d", endYear, endMonth + 1, endDay));
        leaveRequest.put("reason", reason);
        leaveRequest.put("status", "신청"); // 기본 상태 "신청"

        // Firebase DB에 push
        leaveRequestsRef.push().setValue(leaveRequest)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "휴가 신청이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();  // 액티비티 닫기
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "신청 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
