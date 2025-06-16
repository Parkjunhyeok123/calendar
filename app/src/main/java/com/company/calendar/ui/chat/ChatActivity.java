package com.company.calendar.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend;

    private DatabaseReference dbRef;
    private DatabaseReference messagesRef;

    private String currentUserId;
    private String currentUserName;
    private String currentUserTeam;

    private MessageAdapter adapter;
    private ArrayList<ChatMessage> messageList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        ImageButton btnSend = findViewById(R.id.btnSend);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        dbRef = FirebaseDatabase.getInstance().getReference();

        // 1. 사용자 정보 가져오기 (이름, 부서)
        dbRef.child("UserAccount").child(currentUserId)
                .get().addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        currentUserName = dataSnapshot.child("name").getValue(String.class);
                        currentUserTeam = dataSnapshot.child("team").getValue(String.class);

                        if (currentUserTeam == null || currentUserTeam.isEmpty()) {
                            Toast.makeText(this, "부서 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        setupChatRoom(currentUserTeam);
                    } else {
                        Toast.makeText(this, "사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "사용자 정보 가져오기 실패", Toast.LENGTH_SHORT).show();
                    finish();
                });

        btnSend.setOnClickListener(v -> {
            String msgText = etMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(msgText)) {
                sendMessage(msgText);
                etMessage.setText("");
            }
        });

        adapter = new MessageAdapter(messageList, currentUserId);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);
    }

    private void setupChatRoom(String team) {
        messagesRef = dbRef.child("chat_rooms").child(team).child("messages");

        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                ChatMessage msg = snapshot.getValue(ChatMessage.class);
                if (msg != null) {
                    messageList.add(msg);
                    adapter.notifyItemInserted(messageList.size() - 1);
                    rvMessages.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendMessage(String text) {
        String msgId = messagesRef.push().getKey();
        if (msgId == null) return;

        ChatMessage msg = new ChatMessage(currentUserId, currentUserName, text, System.currentTimeMillis());
        messagesRef.child(msgId).setValue(msg);
    }
}
