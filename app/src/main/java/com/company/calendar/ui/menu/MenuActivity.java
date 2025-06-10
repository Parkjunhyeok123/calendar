package com.company.calendar.ui.menu;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.company.calendar.R;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MenuActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private TextView textBreakfast, textLunch, textDinner;
    private Button btnSelectDate;

    private Calendar selectedDate;
    private final Map<String, String[]> mealDataMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        textBreakfast = findViewById(R.id.textBreakfast);
        textLunch = findViewById(R.id.textLunch);
        textDinner = findViewById(R.id.textDinner);
        btnSelectDate = findViewById(R.id.btnSelectDate);

        generateDummyMealData();

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
        showMealsForDate(key);
    }

    private void showMealsForDate(String key) {
        String[] meals = mealDataMap.get(key);
        if (meals != null) {
            textBreakfast.setText("조식: " + meals[0]);
            textLunch.setText("중식: " + meals[1]);
            textDinner.setText("석식: " + meals[2]);
        } else {
            textBreakfast.setText("조식: -");
            textLunch.setText("중식: -");
            textDinner.setText("석식: -");
        }
    }

    private void generateDummyMealData() {
        // yyyy-MM-dd 형식 키를 사용
        mealDataMap.put("2025-06-06", new String[]{"계란국, 토스트", "돈까스, 김치", "김치찌개, 계란말이"});
        mealDataMap.put("2025-06-07", new String[]{"미역국, 밥", "불고기, 콩나물", "된장찌개, 두부"});
        // ... 필요에 따라 추가
    }
}
