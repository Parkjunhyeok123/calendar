package com.company.calendar.ui.home;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.company.calendar.R;
import com.company.calendar.databinding.FragmentHomeBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private EventAdapter eventAdapter;
    private String selectedDate = null; // 선택된 날짜 저장
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private Spinner eventSpinner;

    private boolean isSharedMode = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();  // Firebase 인증 초기화
        currentUser = mAuth.getCurrentUser(); // 현재 로그인된 사용자 정보 가져오기

        // 로그인된 사용자 정보 확인
        if (currentUser != null) {
            String userId = currentUser.getUid(); // 로그인된 사용자의 UID
            Log.d("HomeFragment", "로그인된 사용자 UID: " + userId);
            loadEvents(false, userId); // 로그인된 사용자 ID로 이벤트 로드
        } else {
            Log.d("HomeFragment", "로그인되지 않음");
            // 로그인되지 않은 경우 사용자에게 안내
            Toast.makeText(getContext(), "로그인 후 사용할 수 있습니다.", Toast.LENGTH_SHORT).show();
        }

        eventAdapter = new EventAdapter();
        binding.recyclerView.setAdapter(eventAdapter);

        // 이벤트 추가 버튼 클릭
        final FloatingActionButton addEventBtn = binding.addEventBtn;
        addEventBtn.setOnClickListener(v -> {
            if (currentUser != null) {
                showEventDialog();  // 로그인된 사용자만 이벤트 추가 가능
            } else {
                Toast.makeText(getContext(), "로그인 후 이벤트를 추가할 수 있습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 스피너 초기화
        Spinner eventSpinner = binding.getRoot().findViewById(R.id.eventSpinner);

        // 스피너에 사용할 항목 목록 생성
        List<String> spinnerItems = new ArrayList<>();
        spinnerItems.add("팀 이벤트");
        spinnerItems.add("개인 이벤트");

        // 스피너 어댑터 설정
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, spinnerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventSpinner.setAdapter(spinnerAdapter);

        // 스피너 항목 선택 리스너 설정
        eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedItem = (String) parentView.getItemAtPosition(position);

                switch (selectedItem) {

                    case "팀 이벤트":
                        isSharedMode = true;
                        loadTeamEvents(); // 팀 이벤트 로드
                        break;
                    case "개인 이벤트":
                        isSharedMode = false;
                        loadEventsByDate(selectedDate); // 개인 이벤트 로드
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // 아무 것도 선택되지 않은 경우
            }
        });

        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;

            if (isSharedMode) {
                loadTeamEventsByDate(selectedDate); // 공유 일정용 필터 함수 필요
            } else {
                loadEventsByDate(selectedDate); // 기존 개인 일정 필터 함수
            }
        });

        // 팀 일정 버튼 클릭 시
        // 추가 기능 구현이 필요할 경우 여기에 작성

        return root;
    }






    // 기존 개인 이벤트 로딩
    private void loadEvents(boolean isSharedMode, @NonNull String userId) {
        db.collection("events")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> filteredEvents = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                    Date selectedDateObj = null;
                    try {
                        selectedDateObj = selectedDate != null ? sdf.parse(selectedDate) : null;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);

                        boolean isShared = Boolean.TRUE.equals(document.getBoolean("shared"));
                        String eventUserId = document.getString("userId");

                        boolean match =
                                (isSharedMode && isShared && !userId.equals(eventUserId)) ||
                                        (!isSharedMode && userId.equals(eventUserId) && !isShared);

                        if (match && event.getTimestamp() != null) {
                            Date eventDate = event.getTimestamp().toDate();
                            if (selectedDateObj == null || isSameDay(eventDate, selectedDateObj)) {
                                filteredEvents.add(event);
                            }
                        }
                    }

                    eventAdapter.setEvents(filteredEvents);
                });
    }




    // 팀 일정을 로드하는 메서드 추가
    private void loadTeamEvents() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인 후 팀 일정을 볼 수 있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events")
                .whereEqualTo("shared", true)  // shared가 true인 팀 일정만 로드
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> teamEvents = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        teamEvents.add(event);
                    }

                    if (teamEvents.isEmpty()) {
                        Toast.makeText(getContext(), "팀 일정을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                    eventAdapter.setEvents(teamEvents);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "팀 일정 로딩 실패", Toast.LENGTH_SHORT).show();
                });
    }



    private void loadTeamEventsByDate(String selectedDate) {
        db.collection("events")
                .whereEqualTo("shared", true)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> filteredEvents = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                    Date selectedDateObj = null;
                    try {
                        selectedDateObj = sdf.parse(selectedDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        if (event.getTimestamp() != null) {
                            Date eventDate = event.getTimestamp().toDate();
                            if (isSameDay(eventDate, selectedDateObj)) {
                                filteredEvents.add(event);
                            }
                        }
                    }

                    eventAdapter.setEvents(filteredEvents);
                    if (filteredEvents.isEmpty()) {
                        Toast.makeText(getContext(), "이 날짜에 팀 일정이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "팀 일정 필터 로딩 실패", Toast.LENGTH_SHORT).show();
                });
    }

    // 선택된 날짜에 맞는 이벤트만 로드
    private void loadEventsByDate(String selectedDate) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid(); // 현재 로그인한 사용자 UID

        db.collection("events")
                .whereEqualTo("userId", userId) // 사용자 ID로 필터링
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Event> filteredEvents = new ArrayList<>();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                        Date selectedDateObj = null;
                        try {
                            selectedDateObj = sdf.parse(selectedDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Event event = document.toObject(Event.class);

                            if (event.getTimestamp() != null) {
                                Date eventDate = event.getTimestamp().toDate();
                                if (isSameDay(eventDate, selectedDateObj)) {
                                    filteredEvents.add(event);
                                }
                            }
                        }

                        eventAdapter.setEvents(filteredEvents);

                        if (filteredEvents.isEmpty()) {
                            Toast.makeText(getContext(), "이 날짜에 저장된 이벤트가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        eventAdapter.setEvents(new ArrayList<>());
                        Toast.makeText(getContext(), "이 날짜에 저장된 이벤트가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "데이터 로딩 실패", Toast.LENGTH_SHORT).show();
                });
    }

    // 날짜 비교 메서드: 날짜만 비교 (시간 제외)
    private boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date1).equals(sdf.format(date2));
    }

    private void showEventDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("새 이벤트 추가");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_event, null);
        final EditText titleInput = dialogView.findViewById(R.id.eventTitleInput);
        final TextView dateInput = dialogView.findViewById(R.id.eventDateInput);
        final TextView timeInput = dialogView.findViewById(R.id.eventTimeInput); // 시간 입력 필드
        final CheckBox sharedCheckbox = dialogView.findViewById(R.id.sharedCheckbox);

        builder.setView(dialogView);

        dateInput.setOnClickListener(v -> showDatePickerDialog(dateInput));
        timeInput.setOnClickListener(v -> showTimePickerDialog(timeInput)); // 시간 선택

        builder.setPositiveButton("추가", (dialog, which) -> {
            String eventTitle = titleInput.getText().toString();
            String eventDate = dateInput.getText().toString();  // yyyy-MM-dd
            String eventTime = timeInput.getText().toString();  // HH:mm
            boolean isShared = sharedCheckbox.isChecked();

            if (!eventTitle.isEmpty() && !eventDate.isEmpty() && !eventTime.isEmpty()) {
                addEventToFirestore(eventTitle, eventDate, eventTime, isShared);
            } else {
                Toast.makeText(getContext(), "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", (dialog, which) -> dialog.dismiss());

        builder.show();
    }


    private void showDatePickerDialog(final TextView dateInput) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    dateInput.setText(date);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void addEventToFirestore(String title, String dateStr, String timeStr, boolean isShared) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        try {
            String dateTimeStr = dateStr + " " + timeStr;  // "yyyy-MM-dd HH:mm"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            Date date = sdf.parse(dateTimeStr);

            Timestamp eventTimestamp = new Timestamp(date);

            Map<String, Object> event = new HashMap<>();
            event.put("title", title);
            event.put("userId", userId);
            event.put("shared", isShared);
            event.put("timestamp", eventTimestamp);

            db.collection("events")
                    .add(event)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "이벤트 추가 완료", Toast.LENGTH_SHORT).show();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // 현재 사용자의 팀 정보 조회
                        db.collection("Users").document(currentUserUid)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String team = documentSnapshot.getString("team");

                                        // 해당 팀 전체 멤버 상태를 이벤트 시작 시간 기준으로 "회의 중"으로 변경
                                        db.collection("Users")
                                                .whereEqualTo("team", team)
                                                .get()
                                                .addOnSuccessListener(teamQuerySnapshot -> {
                                                    for (DocumentSnapshot doc : teamQuerySnapshot) {
                                                        String uid = doc.getId();

                                                        db.collection("Users").document(uid)
                                                                .update("status", "회의 중",
                                                                        "meetingStartAt", eventTimestamp);
                                                    }

                                                    // 팀 상태 변경 후 모든 유저 상태 자동 갱신 함수 호출
                                                    updateUserStatusBasedOnMeetingTime();
                                                });
                                    }
                                });

                        // 이벤트 로드 (공유 모드인지에 따라)
                        if (isSharedMode) {
                            loadTeamEventsByDate(selectedDate);
                        } else {
                            loadEventsByDate(selectedDate);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "이벤트 추가 실패", Toast.LENGTH_SHORT).show();
                    });
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "날짜/시간 형식 오류", Toast.LENGTH_SHORT).show();
        }
    }

    // 유저 상태 자동 갱신 함수
    private void updateUserStatusBasedOnMeetingTime() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users")
                .get()
                .addOnSuccessListener(allUsersSnapshot -> {
                    Timestamp now = Timestamp.now();
                    long twoHoursMillis = 2 * 60 * 60 * 1000;

                    for (DocumentSnapshot doc : allUsersSnapshot) {
                        String status = doc.getString("status");
                        Timestamp meetingStart = doc.getTimestamp("meetingStartAt");

                        if (meetingStart != null) {
                            long elapsed = now.toDate().getTime() - meetingStart.toDate().getTime();

                            if (elapsed >= 0 && elapsed < twoHoursMillis && !"회의 중".equals(status)) {
                                // 이벤트 시작 후 2시간 이내면 '회의 중' 상태로 변경
                                db.collection("Users").document(doc.getId())
                                        .update("status", "회의 중");
                            } else if (elapsed >= twoHoursMillis && !"근무 중".equals(status)) {
                                // 2시간 경과 후 '근무 중' 상태로 변경
                                db.collection("Users").document(doc.getId())
                                        .update("status", "근무 중");
                            }
                        }
                    }
                });
    }


    private void showTimePickerDialog(TextView timeInput) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new android.app.TimePickerDialog(getContext(), (view, hourOfDay, minuteOfHour) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
            timeInput.setText(time);
        }, hour, minute, true).show();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}