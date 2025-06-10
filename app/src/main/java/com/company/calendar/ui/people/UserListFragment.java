package com.company.calendar.ui.people;

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

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.company.calendar.ui.login.UserAccount;
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

public class UserListFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<UserAccount> userList = new ArrayList<>();
    private EditText editTeamSearch;
    private Button btnSearchTeam;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 리스너 구현
        UserAdapter.OnUserClickListener listener = new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(UserAccount user) {
                String phoneNumber = user.getNumber();

                // 전화번호가 null 또는 빈 값이 아닌지 확인
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    try {
                        // 전화번호를 URI 인코딩하여 전달
                        String encodedPhoneNumber = URLEncoder.encode(phoneNumber, "UTF-8");

                        // 카카오톡 친구 추가 인텐트
                        String url = "intent://addfriend/phone/" + encodedPhoneNumber + "#Intent;scheme=kakao;package=com.kakao.talk;end";
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);

                        // 카카오톡이 설치되어 있는지 확인
                        PackageManager packageManager = getActivity().getPackageManager();
                        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                        if (activities.size() > 0) {
                            startActivity(intent);  // 카카오톡 실행
                        } else {
                            Toast.makeText(getContext(), "카카오톡이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.kakao.talk");
                            Intent storeIntent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(storeIntent); // Play 스토어 열기
                        }
                    } catch (UnsupportedEncodingException | URISyntaxException e) {
                        // URI 인코딩 또는 파싱 오류 처리
                        e.printStackTrace();
                        Toast.makeText(getContext(), "전화번호를 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "전화번호가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("UserAccount");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    UserAccount user = userSnap.getValue(UserAccount.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                adapter.updateList(new ArrayList<>(userList));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
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
