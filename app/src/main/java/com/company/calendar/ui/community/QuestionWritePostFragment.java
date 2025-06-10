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

public class QuestionWritePostFragment extends Fragment {

    private EditText editTextTitleQuestion;
    private EditText editTextContentQuestion;
    private static final String TAG = "QuestionWritePostFragment";

    public QuestionWritePostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        View root = inflater.inflate(R.layout.fragment_questionwrite_post, container, false);

        editTextTitleQuestion = root.findViewById(R.id.editTextTitle);
        editTextContentQuestion = root.findViewById(R.id.editTextContent);
        Button buttonSubmitQuestion = root.findViewById(R.id.buttonSubmit);
        Button buttonCancelQuestion = root.findViewById(R.id.buttonCancel);
        Button testButton = root.findViewById(R.id.testButton); // 새로 추가된 테스트용 버튼

        // 제목 글자 수 제한 및 경고 메시지
        editTextTitleQuestion.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        editTextTitleQuestion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 20) {
                    Toast.makeText(getContext(), "제목은 최대 20자까지 입력 가능합니다.", Toast.LENGTH_SHORT).show();
                    editTextTitleQuestion.setText(s.subSequence(0, 20));
                    editTextTitleQuestion.setSelection(20);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 내용 글자 수 및 Enter 키 제한, 경고 메시지
        editTextContentQuestion.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        editTextContentQuestion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 100) {
                    Toast.makeText(getContext(), "내용은 최대 100자까지 입력 가능합니다.", Toast.LENGTH_SHORT).show();
                    editTextContentQuestion.setText(s.subSequence(0, 100));
                    editTextContentQuestion.setSelection(100);
                } else if (countEnterKeys(s.toString()) > 3) {
                    Toast.makeText(getContext(), "Enter 키는 최대 3번까지 입력 가능합니다.", Toast.LENGTH_SHORT).show();
                    editTextContentQuestion.setText(removeExcessEnterKeys(s.toString(), 3));
                    editTextContentQuestion.setSelection(editTextContentQuestion.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 테스트용 버튼 클릭 시 제목과 내용에 테스트 텍스트 입력
        testButton.setOnClickListener(v -> {
            editTextTitleQuestion.setText("테 스 트 용 제 목 입 니 다..");

            StringBuilder testContent = new StringBuilder();
            while (testContent.length() < 99) {
                testContent.append("테스트용입니다. ");
            }
            testContent.setLength(99);  // 정확히 99자로 맞추기
            testContent.append("\n\n\n");  // 엔터키 3번 추가

            editTextContentQuestion.setText(testContent.toString());
        });

        buttonSubmitQuestion.setOnClickListener(v -> {
            Log.d(TAG, "Submit button clicked");
            onSubmitButtonClick();
        });

        buttonCancelQuestion.setOnClickListener(v -> {
            Log.d(TAG, "Cancel button clicked");
            onCancelButtonClick();
        });

        return root;
    }

    private int countEnterKeys(String text) {
        return text.length() - text.replace("\n", "").length();
    }

    private String removeExcessEnterKeys(String text, int maxEnterKeys) {
        StringBuilder result = new StringBuilder();
        int enterCount = 0;
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                enterCount++;
                if (enterCount > maxEnterKeys) continue;
            }
            result.append(c);
        }
        return result.toString();
    }

    private void onSubmitButtonClick() {
        Log.d(TAG, "onSubmitButtonClick called");
        String title = editTextTitleQuestion.getText().toString().trim();
        String content = editTextContentQuestion.getText().toString().trim();

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
                        .child("board_id_1")
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
                            Log.d(TAG, "Post added successfully");
                            Toast.makeText(getContext(), "게시글이 성공적으로 추가되었습니다.", Toast.LENGTH_SHORT).show();
                            returnToCommunityFirstFragment();
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

    private void onCancelButtonClick() {
        returnToCommunityFirstFragment();
    }

    private void returnToCommunityFirstFragment() {
        CommunityFirstFragment communityFirstFragment = new CommunityFirstFragment();

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainerQuestion, communityFirstFragment, "CommunityFirstFragment");
        transaction.commitAllowingStateLoss();

        getParentFragmentManager().popBackStackImmediate();
    }
}
