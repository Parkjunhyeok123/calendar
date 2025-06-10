package com.company.calendar.ui.option;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.company.calendar.R;
import com.company.calendar.ui.home.Event;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OptionFirstFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private LinearLayout personalEventContainer;
    private LinearLayout sharedEventContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_option_first, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        personalEventContainer = root.findViewById(R.id.personalEventContainer);
        sharedEventContainer = root.findViewById(R.id.sharedEventContainer);

        if (currentUser != null) {
            String userId = currentUser.getUid();
            loadEvents(userId);
        } else {
            Toast.makeText(getContext(), "로그인 후 사용할 수 있습니다.", Toast.LENGTH_SHORT).show();
        }

        return root;
    }

    private void loadEvents(@NonNull String userId) {
        // 개인 일정
        db.collection("events")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> personalEventList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        personalEventList.add(event);
                    }
                    showEvents(personalEventList, personalEventContainer);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "개인 일정 로딩 실패", Toast.LENGTH_SHORT).show());

        // 공유 일정
        db.collection("events")
                .whereEqualTo("shared", true)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> sharedEventList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        sharedEventList.add(event);
                    }
                    showEvents(sharedEventList, sharedEventContainer);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "공유 일정 로딩 실패", Toast.LENGTH_SHORT).show());
    }

    private void showEvents(List<Event> events, LinearLayout container) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        Date now = new Date();
        long oneWeekLaterMillis = now.getTime() + (7L * 24 * 60 * 60 * 1000);
        Date oneWeekLater = new Date(oneWeekLaterMillis);

        for (Event event : events) {
            Timestamp timestamp = event.getTimestamp();
            if (timestamp == null) continue;

            Date eventDate = timestamp.toDate();

            // 현재보다 과거나, 7일 이후면 제외
            if (eventDate.before(now) || eventDate.after(oneWeekLater)) {
                continue;
            }

            TextView textView = new TextView(getContext());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String formattedDate = sdf.format(eventDate);

            textView.setText(event.getTitle() + "\n" + formattedDate);
            textView.setPadding(16, 16, 16, 16);
            textView.setTextSize(16);
            textView.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, 16);
            textView.setLayoutParams(layoutParams);

            container.addView(textView);
        }
    }




}
