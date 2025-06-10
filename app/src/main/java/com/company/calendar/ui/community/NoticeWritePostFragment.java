package com.company.calendar.ui.community;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.company.calendar.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class NoticeWritePostFragment extends Fragment {

    private EditText editTextTitleNotice, editTextContentNotice;
    private Button buttonSubmitNotice, buttonCancelNotice;
    private static final String TAG = "NoticeWritePostFragment";

    public NoticeWritePostFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_noticewrite_post, container, false);

        editTextTitleNotice = root.findViewById(R.id.editTextTitleNotice);
        editTextContentNotice = root.findViewById(R.id.editTextContentNotice);
        buttonSubmitNotice = root.findViewById(R.id.buttonSubmitNotice);
        buttonCancelNotice = root.findViewById(R.id.buttonCancelNotice);
        Button testButton = root.findViewById(R.id.testButtonNotice); // ID 수정


        // 제목 글자 수 제한 (20자) 및 엔터키 입력 금지
        editTextTitleNotice.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(20),
                (source, start, end, dest, dstart, dend) -> source.toString().contains("\n") ? "" : source
        });

        // 내용 글자 수 제한 (100자) 및 엔터키 최대 3번 제한
        editTextContentNotice.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        editTextContentNotice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enforceEnterKeyLimit(editTextContentNotice, 3);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        testButton.setOnClickListener(v -> {
            // 제목 필드에 19자 텍스트 설정
            editTextTitleNotice.setText("테 스 트 용 제 목 입 니 다..");

            // 내용 필드에 99자 텍스트와 엔터키 3번 설정
            StringBuilder testContent = new StringBuilder();
            while (testContent.length() < 99) {
                testContent.append("테스트용입니다. ");
            }
            testContent.setLength(99);  // 정확히 99자로 맞추기
            testContent.append("\n\n\n");  // 엔터키 3번 추가

            editTextContentNotice.setText(testContent.toString());
        });




        buttonSubmitNotice.setOnClickListener(v -> {
            Log.d(TAG, "Submit button clicked");
            onSubmitButtonClick();
        });

        buttonCancelNotice.setOnClickListener(v -> returnToCommunityThirdFragment());

        return root;
    }


    private void enforceEnterKeyLimit(EditText editText, int maxEnterKeys) {
        String text = editText.getText().toString();
        int enterCount = text.length() - text.replace("\n", "").length();

        if (enterCount > maxEnterKeys) {
            Toast.makeText(getContext(), "엔터키는 최대 " + maxEnterKeys + "번까지 사용할 수 있습니다.", Toast.LENGTH_SHORT).show();
            editText.setText(text.substring(0, text.length() - 1));
            editText.setSelection(editText.getText().length());
        }
    }

    private void onSubmitButtonClick() {
        String title = editTextTitleNotice.getText().toString().trim();
        String content = editTextContentNotice.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(getContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(getContext(), "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("UserAccount")
                    .child(userId)
                    .child("name");

            userRef.get().addOnSuccessListener(dataSnapshot -> {
                String userName = dataSnapshot.getValue(String.class);
                DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference()
                        .child("board")
                        .child("general_boards")
                        .child("board_id_3")
                        .child("posts");

                String uniquePostKey = postsRef.push().getKey();
                if (uniquePostKey == null) {
                    Log.e(TAG, "Failed to generate unique post key");
                    Toast.makeText(getContext(), "게시글 작성에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> postValues = new HashMap<>();
                postValues.put("postId", uniquePostKey);
                postValues.put("author", userName);
                postValues.put("content", content);
                postValues.put("title", title);
                postValues.put("timestamp", System.currentTimeMillis());
                postValues.put("userId", userId);

                postsRef.child(uniquePostKey).setValue(postValues)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "게시글이 성공적으로 추가되었습니다.", Toast.LENGTH_SHORT).show();
                            returnToCommunityThirdFragment();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to add post", e);
                            Toast.makeText(getContext(), "게시글 작성에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error getting user name", e);
                Toast.makeText(getContext(), "사용자 이름을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void returnToCommunityThirdFragment() {
        CommunityThirdFragment communityThirdFragment = new CommunityThirdFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_notice, communityThirdFragment, "CommunityThirdFragment");
        transaction.commitAllowingStateLoss();
        getParentFragmentManager().popBackStackImmediate();
    }
}
