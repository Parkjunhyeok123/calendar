package com.company.calendar.ui.event;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.company.calendar.ui.home.Event;
import com.company.calendar.ui.home.EventAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewWeekDays, recyclerViewPersonal, recyclerViewShared;
    private EventAdapter personalAdapter, sharedAdapter;
    private WeekDayAdapter weekDayAdapter;

    private List<WeekDay> weekDays = new ArrayList<>();
    private Map<String, String> holidays = new HashMap<>(); // 공휴일 날짜 문자열 -> 공휴일 이름

    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        recyclerViewWeekDays = findViewById(R.id.recyclerViewWeekDays);
        recyclerViewPersonal = findViewById(R.id.recyclerViewPersonal);
        recyclerViewShared = findViewById(R.id.recyclerViewShared);

        recyclerViewWeekDays.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewPersonal.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewShared.setLayoutManager(new LinearLayoutManager(this));

        personalAdapter = new EventAdapter();
        sharedAdapter = new EventAdapter();

        recyclerViewPersonal.setAdapter(personalAdapter);
        recyclerViewShared.setAdapter(sharedAdapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        weekDayAdapter = new WeekDayAdapter(weekDays, holidays, this::loadEventsForDate);
        recyclerViewWeekDays.setAdapter(weekDayAdapter);


        fetchHolidays();
    }

    private void fetchHolidays() {
        // 공휴일 데이터를 RealtimeDB에서 불러오고 UI 업데이트
        loadHolidaysFromRealtimeDB();
        // 또는 새로 공휴일 API 호출 후 저장하려면:
         fetchAndSaveAllHolidaysToRealtimeDB();
    }

    private void generateWeekDays() {
        weekDays.clear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        for (int i = 0; i < 7; i++) {
            Date date = calendar.getTime();
            boolean selected = (i == 0);
            weekDays.add(new WeekDay(new SimpleDateFormat("E", Locale.KOREA).format(date), date, selected));
            calendar.add(Calendar.DATE, 1);
        }

        weekDayAdapter.notifyDataSetChanged();
        loadEventsForDate(weekDays.get(0).date);
    }

    private void loadEventsForDate(Date date) {
        List<Event> personalEvents = new ArrayList<>();
        List<Event> sharedEvents = new ArrayList<>();

        Calendar start = Calendar.getInstance();
        start.setTime(date);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = (Calendar) start.clone();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);

        Query personalQuery = db.collection("events")
                .whereEqualTo("userId", currentUserId)
                .whereGreaterThanOrEqualTo("timestamp", start.getTime())
                .whereLessThanOrEqualTo("timestamp", end.getTime());

        Query sharedQuery = db.collection("events")
                .whereEqualTo("shared", true)
                .whereGreaterThanOrEqualTo("timestamp", start.getTime())
                .whereLessThanOrEqualTo("timestamp", end.getTime());

        personalQuery.get().addOnSuccessListener(personalSnap -> {
            for (DocumentSnapshot doc : personalSnap.getDocuments()) {
                Event event = doc.toObject(Event.class);
                if (event != null) personalEvents.add(event);
            }

            sharedQuery.get().addOnSuccessListener(sharedSnap -> {
                for (DocumentSnapshot doc : sharedSnap.getDocuments()) {
                    Event event = doc.toObject(Event.class);
                    if (event != null) sharedEvents.add(event);
                }

                // 공휴일 이벤트 추가
                String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(date);
                if (holidays.containsKey(dateKey)) {
                    Event holidayEvent = new Event();
                    holidayEvent.setTitle("[공휴일] " + holidays.get(dateKey));
                    personalEvents.add(0, holidayEvent);
                }

                if (personalEvents.isEmpty()) {
                    recyclerViewPersonal.setVisibility(View.GONE);
                } else {
                    recyclerViewPersonal.setVisibility(View.VISIBLE);
                    personalAdapter.setEvents(personalEvents);
                }

                if (sharedEvents.isEmpty()) {
                    recyclerViewShared.setVisibility(View.GONE);
                } else {
                    recyclerViewShared.setVisibility(View.VISIBLE);
                    sharedAdapter.setEvents(sharedEvents);
                }

                if (personalEvents.isEmpty() && sharedEvents.isEmpty()) {
                    Toast.makeText(this, "선택한 날짜에 일정이 없습니다.", Toast.LENGTH_SHORT).show();
                }

            }).addOnFailureListener(e -> Log.e("EventListActivity", "공유 일정 불러오기 실패: " + e.getMessage()));

        }).addOnFailureListener(e -> Log.e("EventListActivity", "개인 일정 불러오기 실패: " + e.getMessage()));
    }

    void fetchAndSaveAllHolidaysToRealtimeDB() {
        new Thread(() -> {
            try {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);

                Map<String, String> allHolidaysMap = new HashMap<>();

                for (int month = 1; month <= 12; month++) {
                    String apiUrl = "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getHoliDeInfo";
                    String serviceKey = "GuXDYuYdVKEeTRbyzCfTase5ll6SFguEznh49E5YNJOMQTKoT50lock%2F3Izsw9FkJykE9%2F1VrRriHWlKvGZK%2FQ%3D%3D";  // 반드시 URL 인코딩된 값 사용
                    String urlStr = apiUrl
                            + "?solYear=" + year
                            + "&solMonth=" + (month < 10 ? "0" + month : month)
                            + "&ServiceKey=" + serviceKey;

                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    reader.close();

                    String xmlResponse = responseBuilder.toString();
                    Map<String, String> monthlyHolidayMap = parseHolidayXMLToMap(xmlResponse);

                    allHolidaysMap.putAll(monthlyHolidayMap);
                }

                DatabaseReference holidaysRef = FirebaseDatabase.getInstance().getReference("holidays");
                holidaysRef.setValue(allHolidaysMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                       // runOnUiThread(() -> Toast.makeText(this, "연간 공휴일 저장 완료", Toast.LENGTH_SHORT).show());
                    } else {
                       // runOnUiThread(() -> Toast.makeText(this, "연간 공휴일 저장 실패", Toast.LENGTH_SHORT).show());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
              //  runOnUiThread(() -> Toast.makeText(this, "공휴일 데이터 로드 실패", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private Map<String, String> parseHolidayXMLToMap(String xml) throws Exception {
        Map<String, String> holidays = new HashMap<>();

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new java.io.StringReader(xml));

        String tagName = "";
        String locdate = null;
        String dateName = null;

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    tagName = parser.getName();
                    break;


                case XmlPullParser.TEXT:
                    String text = parser.getText();
                    if ("locdate".equals(tagName)) {
                        locdate = text;
                    } else if ("dateName".equals(tagName)) {
                        dateName = text;
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if ("item".equals(parser.getName())) {
                        if (locdate != null && dateName != null) {
                            String formattedDate = locdate.substring(0, 4) + "-" + locdate.substring(4, 6) + "-" + locdate.substring(6, 8);
                            holidays.put(formattedDate, dateName);
                        }
                        locdate = null;
                        dateName = null;
                    }
                    tagName = "";
                    break;
            }
            eventType = parser.next();
        }

        return holidays;
    }

    private void loadHolidaysFromRealtimeDB() {
        DatabaseReference holidaysRef = FirebaseDatabase.getInstance().getReference("holidays");
        holidaysRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holidays.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String date = child.getKey();
                    String name = child.getValue(String.class);
                    holidays.put(date, name);
                }
                generateWeekDays();  // 공휴일 정보 업데이트 후 주간 요일 생성 및 일정 로드
                //Toast.makeText(EventListActivity.this, "공휴일 데이터 불러오기 완료: " + holidays.size() + "개", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Toast.makeText(EventListActivity.this, "공휴일 불러오기 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
