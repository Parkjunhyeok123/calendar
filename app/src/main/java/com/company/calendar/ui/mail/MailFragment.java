package com.company.calendar.ui.mail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // 버튼 추가

import com.company.calendar.R;

public class MailFragment extends Fragment {

    public MailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // fragment_mail.xml 레이아웃 사용
        View rootView = inflater.inflate(R.layout.fragment_mail, container, false);

        // 이메일 버튼
        Button emailButton = rootView.findViewById(R.id.btn_email);
        if (emailButton != null) {
            emailButton.setOnClickListener(v -> {
                Log.d("MailFragment", "이메일 버튼 클릭됨");
                openEmailApp();
            });
        } else {
            Log.e("MailFragment", "이메일 버튼을 찾을 수 없습니다.");
        }

        // 카카오톡 버튼
        Button kakaoButton = rootView.findViewById(R.id.btn_kakao);
        if (kakaoButton != null) {
            kakaoButton.setOnClickListener(v -> {
                Log.d("MailFragment", "카카오톡 버튼 클릭됨");
                openKakaoTalk();
            });
        } else {
            Log.e("MailFragment", "카카오톡 버튼을 찾을 수 없습니다.");
        }

        return rootView;
    }

    // 이메일 앱 열기
    private void openEmailApp() {
        Uri uri = Uri.parse("mailto:");
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e("MailFragment", "이메일 앱을 찾을 수 없습니다.");
            Uri playStoreUri = Uri.parse("market://details?id=com.google.android.gm");
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, playStoreUri);

            if (playStoreIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(playStoreIntent);
            } else {
                playStoreUri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gm");
                playStoreIntent = new Intent(Intent.ACTION_VIEW, playStoreUri);
                startActivity(playStoreIntent);
            }
        }
    }

    // 카카오톡 앱 열기
    private void openKakaoTalk() {
        Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage("com.kakao.talk");

        if (intent != null) {
            startActivity(intent); // 카카오톡 앱 열기
        } else {
            Log.e("MailFragment", "카카오톡 앱을 찾을 수 없습니다.");
            Uri uri = Uri.parse("market://details?id=com.kakao.talk");
            Intent storeIntent = new Intent(Intent.ACTION_VIEW, uri);

            if (storeIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(storeIntent); // Play 스토어 열기
            } else {
                uri = Uri.parse("https://play.google.com/store/apps/details?id=com.kakao.talk");
                storeIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(storeIntent); // 웹 브라우저로 열기
            }
        }
    }
}