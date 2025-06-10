package com.company.calendar.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.company.calendar.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;




public class FindIdActivity2 extends AppCompatActivity {
    TextView idTextView;
    Button backBtn;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findid2);

        idTextView = findViewById(R.id.findIdtv1);
        backBtn = findViewById(R.id.backBtn);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String number = intent.getStringExtra("number");

        findUserId(name, number); // 사용자 이메일 찾는 메서드 호출

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(FindIdActivity2.this, AppActivity.class);
                startActivity(intent);
            }
        });
    }

    // 사용자의 이름과 전화번호를 이용하여 이메일(아이디)을 찾는 메서드
    private void findUserId(String name, String number) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("UserAccount");
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean found = false;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userName = (String) userSnapshot.child("name").getValue();
                    String userNumber = (String) userSnapshot.child("number").getValue();
                    if (userName != null && userName.equals(name) && userNumber != null && userNumber.equals(number)) {
                        String userId = (String) userSnapshot.child("id").getValue(); // 사용자의 아이디
                        idTextView.setText("'" + userId + "' 입니다.");
                        found = true;

                        break;
                    }
                }


                if (!found) {
                    idTextView.setText("해당하는 사용자를 찾을 수 없습니다.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                idTextView.setText("오류가 발생했습니다. 다시 시도해주세요.");
            }
        });
    }

}