package com.company.calendar.ui.mail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.company.calendar.R;

public class MailActivity extends AppCompatActivity {

    private static final String TAG = "MailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail); // XML 파일 이름 변경 필요

        Button emailButton = findViewById(R.id.btn_email);
        if (emailButton != null) {
            emailButton.setOnClickListener(v -> {
                Log.d(TAG, "이메일 버튼 클릭됨");
                openEmailApp();
            });
        } else {
            Log.e(TAG, "이메일 버튼을 찾을 수 없습니다.");
        }

        Button kakaoButton = findViewById(R.id.btn_kakao);
        if (kakaoButton != null) {
            kakaoButton.setOnClickListener(v -> {
                Log.d(TAG, "카카오톡 버튼 클릭됨");
                openKakaoTalk();
            });
        } else {
            Log.e(TAG, "카카오톡 버튼을 찾을 수 없습니다.");
        }
    }

    private void openEmailApp() {
        Uri uri = Uri.parse("mailto:");
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e(TAG, "이메일 앱을 찾을 수 없습니다.");
            Uri playStoreUri = Uri.parse("market://details?id=com.google.android.gm");
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, playStoreUri);

            if (playStoreIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(playStoreIntent);
            } else {
                playStoreUri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gm");
                playStoreIntent = new Intent(Intent.ACTION_VIEW, playStoreUri);
                startActivity(playStoreIntent);
            }
        }
    }

    private void openKakaoTalk() {
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.kakao.talk");

        if (intent != null) {
            startActivity(intent);
        } else {
            Log.e(TAG, "카카오톡 앱을 찾을 수 없습니다.");
            Uri uri = Uri.parse("market://details?id=com.kakao.talk");
            Intent storeIntent = new Intent(Intent.ACTION_VIEW, uri);

            if (storeIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(storeIntent);
            } else {
                uri = Uri.parse("https://play.google.com/store/apps/details?id=com.kakao.talk");
                storeIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(storeIntent);
            }
        }
    }
}
