package com.company.calendar.ui.people;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.company.calendar.R;
import com.company.calendar.ui.login.UserAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FriendFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private LinearLayout layoutRequests;
    private DatabaseReference dbRef;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        layoutRequests = view.findViewById(R.id.layout_friend_requests);

        dbRef = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadFriendRequests();

        // 친구 요청 보내기 버튼 설정
        Button btnAddFriend = view.findViewById(R.id.btn_add_friend);
        btnAddFriend.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FriendRequestFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }


    private void loadFriendRequests() {
        DatabaseReference requestsRef = dbRef.child("FriendRequests").child(currentUserId);

        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                layoutRequests.removeAllViews();
                for (DataSnapshot requestSnap : snapshot.getChildren()) {
                    String senderId = requestSnap.getKey();

                    // 사용자 정보 불러오기
                    dbRef.child("UserAccount").child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            UserAccount user = userSnapshot.getValue(UserAccount.class);
                            if (user != null) {
                                String senderName = user.getName();

                                // UI 구성
                                View requestView = LayoutInflater.from(getContext())
                                        .inflate(R.layout.item_friend_request, layoutRequests, false);

                                TextView txtName = requestView.findViewById(R.id.text_request_name);
                                Button btnAccept = requestView.findViewById(R.id.btn_accept);

                                txtName.setText(senderName + "님의 친구 요청");

                                btnAccept.setOnClickListener(v -> acceptFriendRequest(senderId, requestView));
                                layoutRequests.addView(requestView);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "친구 요청을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void acceptFriendRequest(String senderId, View requestView) {
        DatabaseReference friendsRef = dbRef.child("Friends");

        // 양측 친구 추가
        friendsRef.child(currentUserId).child(senderId).setValue(true);
        friendsRef.child(senderId).child(currentUserId).setValue(true);

        // 요청 제거
        dbRef.child("FriendRequests").child(currentUserId).child(senderId).removeValue();
        dbRef.child("FriendRequests").child(senderId).child(currentUserId).removeValue();

        // UI 업데이트
        layoutRequests.removeView(requestView);
        Toast.makeText(getContext(), "친구 요청을 수락했습니다.", Toast.LENGTH_SHORT).show();
    }
}
