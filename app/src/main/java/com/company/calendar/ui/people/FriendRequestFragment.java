package com.company.calendar.ui.people;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.company.calendar.ui.login.UserAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<UserAccount> userList = new ArrayList<>();
    private EditText editTeamSearch;
    private Button btnSearchTeam;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_request, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 리스너 구현
        UserAdapter.OnUserClickListener listener = new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(UserAccount user) {
                String phoneNumber = user.getNumber(); // 전화번호 가져오기

                // 전화번호가 null 또는 빈 값이 아닌지 확인
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    // 친구 추가 다이얼로그 표시
                    showFriendRequestDialog(user);
                } else {
                    Toast.makeText(getContext(), "전화번호가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            // 친구 추가 다이얼로그 표시
            private void showFriendRequestDialog(UserAccount user) {
                new AlertDialog.Builder(getContext())
                        .setTitle("친구 추가")
                        .setMessage("이 사용자에게 친구 요청을 보내시겠습니까?")
                        .setPositiveButton("확인", (dialog, which) -> {
                            // 확인 버튼 클릭 시 친구 추가 진행
                            sendFriendRequest(user);
                        })
                        .setNegativeButton("취소", (dialog, which) -> {
                            // 취소 버튼 클릭 시 아무것도 하지 않음
                            dialog.dismiss();
                        })
                        .show();
            }

            // 친구 요청을 Firebase에 저장
            private void sendFriendRequest(UserAccount user) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 현재 사용자 ID

                String targetPhoneNumber = user.getNumber();

                // 전화번호로 UID 찾기
                ref.child("UserAccount").orderByChild("number").equalTo(targetPhoneNumber)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot child : snapshot.getChildren()) {
                                        String targetUserUid = child.getKey(); // 상대방 UID

                                        // 상대방 UID를 키로 요청 저장
                                        ref.child("FriendRequests").child(targetUserUid).child(currentUserId)
                                                .setValue(true)
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getContext(), "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show();
                                                        getParentFragmentManager().beginTransaction()

                                                                .commit();
                                                    } else {
                                                        Toast.makeText(getContext(), "친구 요청 실패", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                        break;
                                    }
                                } else {
                                    Toast.makeText(getContext(), "해당 번호의 사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Toast.makeText(getContext(), "데이터베이스 오류", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

        };

        // 어댑터에 리스너 전달
        adapter = new UserAdapter(userList, listener);
        recyclerView.setAdapter(adapter);

        editTeamSearch = view.findViewById(R.id.editTeamSearch);
        btnSearchTeam = view.findViewById(R.id.btnSearchTeam);

        // 실시간 입력 시 필터링
        editTeamSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterByKeyword(s.toString().trim().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // 버튼 클릭 시 필터링
        btnSearchTeam.setOnClickListener(v -> {
            String keyword = editTeamSearch.getText().toString().trim().toLowerCase();
            filterByKeyword(keyword);
        });

        loadUsersFromFirebase();
        return view;
    }

    private void loadUsersFromFirebase() {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        // 1. 친구 UID 목록 가져오기
        ref.child("Friends").child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot friendSnapshot) {
                        List<String> friendUIDs = new ArrayList<>();

                        for (DataSnapshot snap : friendSnapshot.getChildren()) {
                            friendUIDs.add(snap.getKey());
                        }

                        // 2. 전체 사용자 목록에서 친구가 아닌 사용자만 필터링
                        ref.child("UserAccount")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                        userList.clear();

                                        for (DataSnapshot userSnap : userSnapshot.getChildren()) {
                                            UserAccount user = userSnap.getValue(UserAccount.class);
                                            if (user != null && !user.getUid().equals(currentUid) && !friendUIDs.contains(user.getUid())) {
                                                userList.add(user);
                                            }
                                        }

                                        adapter.updateList(new ArrayList<>(userList));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getContext(), "사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "친구 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void filterByKeyword(String keyword) {
        if (keyword.isEmpty()) {
            adapter.updateList(new ArrayList<>(userList));
            return;
        }

        List<UserAccount> filtered = new ArrayList<>();
        for (UserAccount user : userList) {
            String team = user.getTeam() != null ? user.getTeam().toLowerCase() : "";
            String name = user.getName() != null ? user.getName().toLowerCase() : "";

            if (team.contains(keyword) || name.contains(keyword)) {
                filtered.add(user);
            }
        }

        if (filtered.isEmpty()) {
            Toast.makeText(getContext(), "일치하는 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
        }

        adapter.updateList(filtered);
    }
}
