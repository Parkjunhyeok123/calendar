package com.company.calendar.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.company.calendar.R;


public class FindIdActivity extends AppCompatActivity {
    EditText nameEditText, numberEditText;
    Button findIdBtn, cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findid);

        nameEditText = findViewById(R.id.findId_nameEditText);
        numberEditText = findViewById(R.id.findId_numberEditText);
        findIdBtn = findViewById(R.id.findIdBtn);
        cancelBtn = findViewById(R.id.cancelBtn);

        findIdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameEditText.getText().toString();
                String number = numberEditText.getText().toString();

                // 이름과 전화번호 정보를 Intent에 담아서 FindIdActivity2로 전달
                Intent intent = new Intent(FindIdActivity.this, FindIdActivity2.class);
                intent.putExtra("name", name);
                intent.putExtra("number", number);
                startActivity(intent);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 취소 버튼 클릭 시 MainActivity로 돌아감
                Intent intent = new Intent(FindIdActivity.this, AppActivity.class);
                startActivity(intent);
            }
        });
    }
}