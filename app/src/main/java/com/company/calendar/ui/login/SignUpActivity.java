package com.company.calendar.ui.login;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.company.calendar.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    Button signUpBtn;

    EditText edtId, edtPw, edtPwChk, edtName, edtNumber;

    private FirebaseAuth fAuth;
    private DatabaseReference dRef; // 실시간 데이터베이스

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fAuth = FirebaseAuth.getInstance(); // FirebaseAuth 초기화
        dRef = FirebaseDatabase.getInstance().getReference();

        signUpBtn = findViewById(R.id.signUpBtn);

        edtId = findViewById(R.id.signUp_idEditText);
        edtPw = findViewById(R.id.signUp_pwEditText);
        edtPwChk = findViewById(R.id.signUp_pwChkEditText);
        edtName = findViewById(R.id.signUp_nameEditText);
        edtNumber = findViewById(R.id.signUp_numberEditText);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String id = edtId.getText().toString().trim();
                final String pw = edtPw.getText().toString().trim();
                final String pwChk = edtPwChk.getText().toString().trim();
                final String name = edtName.getText().toString().trim();
                final String number = edtNumber.getText().toString().trim();

                if (id.isEmpty() || pw.isEmpty() || pwChk.isEmpty() || name.isEmpty() || number.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!pw.equals(pwChk)) {
                    Toast.makeText(SignUpActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Firebase Authentication을 사용하여 사용자 등록
                fAuth.createUserWithEmailAndPassword(id, pw)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // 사용자 등록 성공
                                    FirebaseUser user = fAuth.getCurrentUser();
                                    String userId = user.getUid();

                                    // UserAccount 객체 생성 및 속성 설정
                                    UserAccount userAccount = new UserAccount();
                                    userAccount.setId(id);
                                    userAccount.setName(name);
                                    userAccount.setNumber(number);

                                    // Firebase Realtime Database에 사용자 정보 저장
                                    dRef.child("UserAccount").child(userId).setValue(userAccount);

                                    // 회원가입 성공 후 AppActivity로 이동
                                    Intent intent = new Intent(SignUpActivity.this, AppActivity.class);
                                    startActivity(intent);

                                } else {
                                    // 사용자 등록 실패
                                    Toast.makeText(SignUpActivity.this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}