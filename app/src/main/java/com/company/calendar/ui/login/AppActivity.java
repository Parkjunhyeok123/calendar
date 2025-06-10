package com.company.calendar.ui.login;



import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.*;
import com.google.firebase.auth.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.company.calendar.R;
import com.company.calendar.subActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AppActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private DatabaseReference dRef;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    Button loginBtn, googleLoginBtn;
    TextView findIdTextView, findPwTextView, signUpTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        EditText edtId = findViewById(R.id.login_idEditText);
        EditText edtPw = findViewById(R.id.login_pwEditText);
        loginBtn = findViewById(R.id.loginBtn);
        googleLoginBtn = findViewById(R.id.googleLoginBtn);
        findIdTextView = findViewById(R.id.findIdTextView);
        findPwTextView = findViewById(R.id.findPwTextView);
        signUpTextView = findViewById(R.id.signUpTextView);

        fAuth = FirebaseAuth.getInstance();
        dRef = FirebaseDatabase.getInstance().getReference();


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String id = edtId.getText().toString();
                final String pw = edtPw.getText().toString();

                if (id.isEmpty() || pw.isEmpty()) {
                    Toast.makeText(AppActivity.this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                fAuth.signInWithEmailAndPassword(id, pw).addOnCompleteListener(AppActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("AppActivity", "로그인 시도 완료");
                        if (task.isSuccessful()) {
                            Log.d("AppActivity", "로그인 성공");

                            FirebaseUser user = fAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();
                                dRef.child("UserAccount").child(uid).child("uid").setValue(uid);
                                dRef.child("UserAccount").child(uid).child("status").setValue("근무 중"); // 추가

                                String email = user.getEmail();
                                getSharedPreferences("user", MODE_PRIVATE)
                                        .edit()
                                        .putString("email", email)
                                        .apply();
                            }


                            Intent intent = new Intent(AppActivity.this, subActivity.class);
                            startActivity(intent);


                        } else {
                            Log.d("AppActivity", "로그인 실패", task.getException());
                            Toast.makeText(AppActivity.this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1068008482856-j0jb349kn3s2up254csr90sadn033rcv.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile() // 프로필 정보 요청
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleLoginBtn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        findIdTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // findIdTextView를 클릭했을 때의 동작
                Intent intent = new Intent(com.company.calendar.ui.login.AppActivity.this, com.company.calendar.ui.login.FindIdActivity.class);
                startActivity(intent);
            }
        });

        findPwTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // findPwTextView를 클릭했을 때의 동작
                Intent intent = new Intent(com.company.calendar.ui.login.AppActivity.this, com.company.calendar.ui.login.FindPwActivity.class);
                startActivity(intent);
            }
        });

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // signUpTextView를 클릭했을 때의 동작
                Intent intent = new Intent(com.company.calendar.ui.login.AppActivity.this, com.company.calendar.ui.login.SignUpActivity.class);
                startActivity(intent);
            }

        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // 필수
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("GoogleLogin", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("GoogleLogin", "firebaseAuthWithGoogle: " + acct.getEmail()); // 추가

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("GoogleLogin", "signInWithCredential:success");
                        FirebaseUser user = fAuth.getCurrentUser();
                        Log.d("GoogleLogin", "로그인 사용자: " + user.getEmail());

                        String uid = user.getUid();
                        dRef.child("UserAccount").child(uid).child("uid").setValue(uid);
                        dRef.child("UserAccount").child(uid).child("status").setValue("근무 중");


                        String email = user.getEmail();
                        getSharedPreferences("user", MODE_PRIVATE)
                                .edit()
                                .putString("email", email)
                                .apply();

                        startActivity(new Intent(AppActivity.this, subActivity.class));
                        finish();
                    } else {
                        Log.w("GoogleLogin", "signInWithCredential:failure", task.getException());
                        Toast.makeText(AppActivity.this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}