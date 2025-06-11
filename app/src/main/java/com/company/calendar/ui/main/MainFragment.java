package com.company.calendar.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.company.calendar.ui.approval.LeaveRequestListActivity;
import com.company.calendar.ui.attendance.AttendanceActivity;
import com.company.calendar.ui.card.CardEditorActivity;
import com.company.calendar.ui.chat.ChatActivity;
import com.company.calendar.ui.email.EmailActivity;
import com.company.calendar.ui.event.EventListActivity;
import com.company.calendar.ui.menu.MenuActivity;
import com.company.calendar.ui.people.OrganizationActivity;

import com.company.calendar.ui.mail.MailActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainFragment extends Fragment {
    private TextView tvDepartmentUser, tvWeatherLocation;

    private RecyclerView attendanceRecyclerView;
    private AttendanceAdapter attendanceAdapter;
    private List<AttendanceAdapter.Attendance> attendanceList;

    private RecyclerView shortcutRecyclerView;
    private ShortcutAdapter shortcutAdapter;
    private List<Shortcut> shortcutList;

    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        tvDepartmentUser = view.findViewById(R.id.tvDepartmentUser);
        tvWeatherLocation = view.findViewById(R.id.tvWeatherLocation);

        dbRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        loadUserInfo();

        attendanceRecyclerView = view.findViewById(R.id.attendanceRecyclerView);
        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        attendanceList = new ArrayList<>();
        attendanceAdapter = new AttendanceAdapter(attendanceList);
        attendanceRecyclerView.setAdapter(attendanceAdapter);

        loadWeeklyAttendance(); // 출퇴근 내역 로드

        shortcutRecyclerView = view.findViewById(R.id.shortcutRecyclerView);
        shortcutRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        shortcutList = new ArrayList<>();
        shortcutList.add(new Shortcut("출퇴근 관리", R.drawable.ic_go));
        shortcutList.add(new Shortcut("이메일/카카오톡", R.drawable.ic_kakao));
        shortcutList.add(new Shortcut("일정", R.drawable.ic_schedule));
        shortcutList.add(new Shortcut("조직도", R.drawable.ic_organization));
        shortcutList.add(new Shortcut("이메일", R.drawable.ic_email));
        shortcutList.add(new Shortcut("식당", R.drawable.ic_food));
        shortcutList.add(new Shortcut("전자결재", R.drawable.ic_ok));
        shortcutList.add(new Shortcut("부서별 채팅", R.drawable.ic_chat));
        shortcutList.add(new Shortcut("명함", R.drawable.ic_chat));


        shortcutAdapter = new ShortcutAdapter(getContext(), shortcutList, position -> {
            switch (position) {
                case 0:
                    startActivity(new Intent(getContext(), AttendanceActivity.class));
                    break;
                case 1:
                    startActivity(new Intent(getContext(), MailActivity.class));
                    break;
                case 2:
                    startActivity(new Intent(getContext(), EventListActivity.class));
                    break;
                case 3:
                    startActivity(new Intent(getContext(), OrganizationActivity.class));
                    break;
                case 4:
                    startActivity(new Intent(getContext(), EmailActivity.class));
                    break;
                case 5:
                    startActivity(new Intent(getContext(), MenuActivity.class));
                    break;
                case 6:
                    startActivity(new Intent(getContext(), LeaveRequestListActivity.class)); // ← 여기 추가
                    break;
                case 7:
                    startActivity(new Intent(getContext(), ChatActivity.class)); // 부서별 채팅 연결
                    break;
                case 8:
                    startActivity(new Intent(getContext(), CardEditorActivity.class));
                    break;
            }
        });
        shortcutRecyclerView.setAdapter(shortcutAdapter);

        requestLocationAndWeather();

        return view;
    }

    private void loadUserInfo() {
        if (auth.getCurrentUser() == null) {
            tvDepartmentUser.setText("로그인 정보 없음");
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        dbRef.child("UserAccount").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue(String.class);
                            String team = snapshot.child("team").getValue(String.class);
                            if (name == null) name = "이름 없음";
                            if (team == null) team = "팀 정보 없음";
                            tvDepartmentUser.setText(team + " / " + name);
                        } else {
                            tvDepartmentUser.setText("사용자 정보가 없습니다.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvDepartmentUser.setText("팀 정보 불러오기 실패");
                    }
                });
    }

    private void loadWeeklyAttendance() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        DatabaseReference attendanceRef = dbRef.child("attendance").child(uid);

        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceList.clear();

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // 주 시작: 월요일

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String[] dayNames = {"월", "화", "수", "목", "금", "토", "일"};

                for (int i = 0; i < 7; i++) {
                    String dateKey = sdf.format(cal.getTime());

                    String status = "";
                    String checkIn = "-";
                    String checkOut = "-";

                    if (snapshot.hasChild(dateKey)) {
                        DataSnapshot daySnapshot = snapshot.child(dateKey);

                        if (daySnapshot.hasChild("status")) {
                            status = daySnapshot.child("status").getValue(String.class);
                        }
                        if (daySnapshot.hasChild("checkIn")) {
                            checkIn = daySnapshot.child("checkIn").getValue(String.class);
                        }
                        if (daySnapshot.hasChild("checkOut")) {
                            checkOut = daySnapshot.child("checkOut").getValue(String.class);
                        }
                    }

                    if (status.isEmpty()) {
                        status = "휴무중"; // 데이터 없으면 휴무중 표시
                    }

                    String timeDisplay = (checkIn.equals("-") && checkOut.equals("-")) ? "-" : (checkIn + " - " + checkOut);

                    Log.d("Attendance", "Date: " + dateKey + ", status: " + status + ", timeDisplay: " + timeDisplay);

                    attendanceList.add(new AttendanceAdapter.Attendance(dayNames[i], status, timeDisplay));
                    cal.add(Calendar.DAY_OF_MONTH, 1);  // 날짜를 한 칸씩 이동
                }

                attendanceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Attendance", "DB Error: " + error.getMessage());
            }
        });
    }




    private void requestLocationAndWeather() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocationAndFetchWeather();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocationAndFetchWeather() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        fetchWeather(lat, lon);
                    } else {
                        tvWeatherLocation.setText("위치 정보 없음");
                    }
                })
                .addOnFailureListener(e -> tvWeatherLocation.setText("위치 정보 요청 실패"));
    }

    private void fetchWeather(double lat, double lon) {
        String apiKey = "48540d31653c6bbc5296a90e0ce7126f";
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat +
                "&lon=" + lon + "&appid=" + apiKey + "&units=metric&lang=kr";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> tvWeatherLocation.setText("날씨 정보 불러오기 실패"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        String cityName = json.getString("name");
                        JSONObject weatherObj = json.getJSONArray("weather").getJSONObject(0);
                        String description = weatherObj.getString("description");
                        JSONObject main = json.getJSONObject("main");
                        double temp = main.getDouble("temp");

                        String weatherText = cityName + " · " + description + " · " + Math.round(temp) + "°C";

                        requireActivity().runOnUiThread(() -> tvWeatherLocation.setText(weatherText));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        requireActivity().runOnUiThread(() -> tvWeatherLocation.setText("날씨 정보 분석 실패"));
                    }
                } else {
                    requireActivity().runOnUiThread(() -> tvWeatherLocation.setText("날씨 정보 불러오기 실패"));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocationAndFetchWeather();
            } else {
                tvWeatherLocation.setText("위치 권한 필요");
            }
        }
    }
}
