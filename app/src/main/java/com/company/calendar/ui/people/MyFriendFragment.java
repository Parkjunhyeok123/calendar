package com.company.calendar.ui.people;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.company.calendar.ui.login.UserAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyFriendFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<UserAccount> friendList = new ArrayList<>();
    private EditText editTeamSearch;
    private Button btnSearchTeam;
    private TextView txtNoFriends;

    private Set<String> friendUIDs = new HashSet<>();
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(friendList, this::onUserClick);
        recyclerView.setAdapter(adapter);

        editTeamSearch = view.findViewById(R.id.editTeamSearch);
        btnSearchTeam = view.findViewById(R.id.btnSearchTeam);
        txtNoFriends = view.findViewById(R.id.txtNoFriends);

        editTeamSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterByKeyword(s.toString().trim().toLowerCase());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSearchTeam.setOnClickListener(v -> {
            hideKeyboard();
            String keyword = editTeamSearch.getText().toString().trim().toLowerCase();
            filterByKeyword(keyword);
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            loadFriendUIDs();
        } else {
            Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void hideKeyboard() {
        if (getActivity() != null && getView() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    private void loadFriendUIDs() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("friends").child(currentUser.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                friendUIDs.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Boolean isFriend = child.getValue(Boolean.class);
                    if (Boolean.TRUE.equals(isFriend)) {
                        friendUIDs.add(child.getKey());
                        Log.d("MyFriendFragment", "Friend UID added: " + child.getKey());
                    }
                }
                loadFriendAccounts();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "친구 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFriendAccounts() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String currentUid = currentUser.getUid();
        DatabaseReference friendRef = FirebaseDatabase.getInstance().getReference("Friends").child(currentUid);

        // 1. 친구 UID 리스트 불러오기
        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> friendUIDs = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    friendUIDs.add(snap.getKey());  // 친구 UID 저장
                }

                // 2. UserAccount에서 해당 UID의 사용자 정보 불러오기
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount");
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        friendList.clear();  // 기존 목록 초기화

                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            UserAccount user = userSnap.getValue(UserAccount.class);

                            // null 체크 + uid 기준 비교
                            if (user != null && friendUIDs.contains(user.getUid())) {
                                friendList.add(user);
                                Log.d("MyFriendFragment", "Friend user loaded: " + user.getName());
                            } else {
                                Log.d("MyFriendFragment", "Skipped user: " + (user != null ? user.getUid() : "null"));
                            }
                        }

                        // RecyclerView 갱신
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        txtNoFriends.setVisibility(friendList.isEmpty() ? View.VISIBLE : View.GONE);
                    }



                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MyFriendFragment", "UserAccount load failed: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MyFriendFragment", "Friend UID load failed: " + error.getMessage());
            }
        });
    }



    private void filterByKeyword(String keyword) {
        if (keyword.isEmpty()) {
            adapter.updateList(friendList);
            txtNoFriends.setVisibility(friendList.isEmpty() ? View.VISIBLE : View.GONE);
            return;
        }

        List<UserAccount> filtered = new ArrayList<>();
        for (UserAccount user : friendList) {
            String team = user.getTeam() != null ? user.getTeam().toLowerCase() : "";
            String name = user.getName() != null ? user.getName().toLowerCase() : "";
            String number = user.getNumber() != null ? user.getNumber().toLowerCase() : "";
            String email = user.getId() != null ? user.getId().toLowerCase() : "";
            String status = user.getStatus() != null ? user.getStatus().toLowerCase():"";

            if (team.contains(keyword) || name.contains(keyword) || number.contains(keyword) || email.contains(keyword)|| status.contains(keyword)) {
                filtered.add(user);
            }
        }

        adapter.updateList(filtered);
        txtNoFriends.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        if (filtered.isEmpty()) {
            Toast.makeText(getContext(), "일치하는 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void onUserClick(UserAccount user) {
        String phoneNumber = user.getNumber();
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            try {
                String encodedPhoneNumber = URLEncoder.encode(phoneNumber, "UTF-8");
                String url = "intent://addfriend/phone/" + encodedPhoneNumber + "#Intent;scheme=kakao;package=com.kakao.talk;end";
                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);

                PackageManager packageManager = requireActivity().getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (!activities.isEmpty()) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "카카오톡이 설치되어 있지 않습니다. 설치 페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
                    Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.kakao.talk");
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                }
            } catch (UnsupportedEncodingException | URISyntaxException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "전화번호를 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "전화번호가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
