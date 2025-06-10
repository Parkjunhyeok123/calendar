package com.company.calendar.ui.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.company.calendar.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class FindPwActivity extends AppCompatActivity {
    EditText nameEditText, idEditText;
    Button findPwBtn, cancelBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findpw);

        nameEditText = findViewById(R.id.findPw_nameEditText);
        idEditText = findViewById(R.id.findPw_IdEditText);
        findPwBtn = findViewById(R.id.findPwBtn);
        cancelBtn = findViewById(R.id.cancelBtn);

        findPwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = nameEditText.getText().toString().trim();
                final String email = idEditText.getText().toString().trim();

                // 이름과 이메일이 입력되었는지 확인
                if (name.isEmpty() || email.isEmpty()) {
                    Toast.makeText(FindPwActivity.this, "이름과 이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }


                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(FindPwActivity.this, "비밀번호 재설정 이메일을 전송했습니다.", Toast.LENGTH_SHORT).show();
                                // 비밀번호 재설정 이메일을 보냈으므로 메인 액티비티로 이동
                                Intent intent = new Intent(FindPwActivity.this, AppActivity.class);
                                startActivity(intent);
                            } else {
                                // Firebase의 예외 처리
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthInvalidUserException e) {
                                    Toast.makeText(FindPwActivity.this, "존재하지 않는 이메일입니다.", Toast.LENGTH_SHORT).show();
                                } catch (FirebaseAuthRecentLoginRequiredException e) {
                                    Toast.makeText(FindPwActivity.this, "최근에 로그인한 기기가 아닙니다.", Toast.LENGTH_SHORT).show();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    Toast.makeText(FindPwActivity.this, "사용자 충돌이 발생했습니다.", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(FindPwActivity.this, "비밀번호 재설정 이메일 전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 취소 버튼 클릭 시 메인 액티비티로 이동
                Intent intent = new Intent(FindPwActivity.this, AppActivity.class);
                startActivity(intent);
            }
        });
    }
}