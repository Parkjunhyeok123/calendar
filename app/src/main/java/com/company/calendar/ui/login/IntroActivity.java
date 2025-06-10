package com.company.calendar.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.company.calendar.R;
import com.company.calendar.subActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class IntroActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // Firebase 사용자 가져오기
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // 2초 후에 MainActivity로 이동
        new Handler().postDelayed(() -> {
            if (currentUser != null) {
                // 사용자가 로그인한 경우, MainActivity로 이동
                startActivity(new Intent(IntroActivity.this, subActivity.class));
            } else {
                // 사용자가 로그인한 기록이 없는 경우, MainActivity로 이동
                startActivity(new Intent(IntroActivity.this, subActivity.class));
            }
            finish();
        }, 2000);
    }
}