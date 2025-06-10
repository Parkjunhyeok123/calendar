package com.company.calendar.ui.menu;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.company.calendar.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;

public class MenuActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private TextView textBreakfast, textLunch, textDinner, textSelectedDate; // ← 추가
    private Button btnSelectDate;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        textBreakfast = findViewById(R.id.textBreakfast);
        textLunch = findViewById(R.id.textLunch);
        textDinner = findViewById(R.id.textDinner);
        textSelectedDate = findViewById(R.id.textSelectedDate); // ← 추가
        btnSelectDate = findViewById(R.id.btnSelectDate);

        btnSelectDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    MenuActivity.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            dpd.show(getSupportFragmentManager(), "Datepickerdialog");
        });
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        selectedDate = Calendar.getInstance();
        selectedDate.set(year, monthOfYear, dayOfMonth);

        String key = String.format("%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
        String display = String.format("선택한 날짜: %d년 %d월 %d일", year, monthOfYear + 1, dayOfMonth);

        // 선택한 날짜 텍스트뷰에 표시
        textSelectedDate.setText(display);

        showMealsForDate(key);
    }

    private void showMealsForDate(String key) {
        DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference("meals").child(key);
        mealRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                String breakfast = snapshot.child("breakfast").getValue(String.class);
                String lunch = snapshot.child("lunch").getValue(String.class);
                String dinner = snapshot.child("dinner").getValue(String.class);

                textBreakfast.setText("조식: " + breakfast);
                textLunch.setText("중식: " + lunch);
                textDinner.setText("석식: " + dinner);
            } else {
                textBreakfast.setText("조식: -");
                textLunch.setText("중식: -");
                textDinner.setText("석식: -");
            }
        });
    }
}
