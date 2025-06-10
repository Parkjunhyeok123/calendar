package com.company.calendar.ui.community;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.company.calendar.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Edit3PostFragment extends Fragment {

    private EditText editTextTitleNotice, editTextContentNotice;
    private Button buttonSaveNotice, buttonCancelNotice;
    private String postIdNotice;

    private static final String TAG = "Edit3PostFragment";
    private static final int MAX_TITLE_LENGTH = 50;
    private static final int MAX_CONTENT_LENGTH = 100;
    private static final int MAX_NEWLINE_COUNT = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit3_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextTitleNotice = view.findViewById(R.id.editTextTitle);
        editTextContentNotice = view.findViewById(R.id.editTextContent);
        buttonSaveNotice = view.findViewById(R.id.buttonSave);
        buttonCancelNotice = view.findViewById(R.id.buttonCancel);

        setUpTextRestrictions();

        Bundle bundle = getArguments();
        if (bundle != null) {
            postIdNotice = bundle.getString("postId");
            Log.d(TAG, "Received postId: " + postIdNotice);
        }

        buttonCancelNotice.setOnClickListener(v -> requireActivity().onBackPressed());
        buttonSaveNotice.setOnClickListener(v -> saveChanges());

        loadPostDetails();
    }

    private void setUpTextRestrictions() {
        editTextTitleNotice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > MAX_TITLE_LENGTH) {
                    editTextTitleNotice.setText(s.subSequence(0, MAX_TITLE_LENGTH));
                    editTextTitleNotice.setSelection(editTextTitleNotice.getText().length());
                    editTextTitleNotice.setError("제목은 최대 " + MAX_TITLE_LENGTH + "자까지 입력 가능합니다.");
                }
                if (s.toString().contains("\n")) {
                    editTextTitleNotice.setText(s.toString().replace("\n", ""));
                    editTextTitleNotice.setSelection(editTextTitleNotice.getText().length());
                    editTextTitleNotice.setError("제목에 엔터 키를 사용할 수 없습니다.");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        editTextContentNotice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > MAX_CONTENT_LENGTH) {
                    editTextContentNotice.setText(s.subSequence(0, MAX_CONTENT_LENGTH));
                    editTextContentNotice.setSelection(editTextContentNotice.getText().length());
                    editTextContentNotice.setError("내용은 최대 " + MAX_CONTENT_LENGTH + "자까지 입력 가능합니다.");
                }

                int newlineCount = countNewlines(s.toString());
                if (newlineCount > MAX_NEWLINE_COUNT) {
                    editTextContentNotice.setText(s.toString().substring(0, s.toString().lastIndexOf("\n")));
                    editTextContentNotice.setSelection(editTextContentNotice.getText().length());
                    editTextContentNotice.setError("내용에 최대 " + MAX_NEWLINE_COUNT + "번까지 엔터를 사용할 수 있습니다.");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private int countNewlines(String text) {
        int newlineCount = 0;
        for (char c : text.toCharArray()) {
            if (c == '\n') newlineCount++;
        }
        return newlineCount;
    }

    private void loadPostDetails() {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference()
                .child("board").child("general_boards").child("board_id_3").child("posts").child(postIdNotice);

        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String title = dataSnapshot.child("title").getValue(String.class);
                String content = dataSnapshot.child("content").getValue(String.class);

                editTextTitleNotice.setText(title);
                editTextContentNotice.setText(content);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load post details", databaseError.toException());
            }
        });
    }

    private void saveChanges() {
        String newTitle = editTextTitleNotice.getText().toString().trim();
        String newContent = editTextContentNotice.getText().toString().trim();

        if (TextUtils.isEmpty(newTitle) || TextUtils.isEmpty(newContent)) {
            Toast.makeText(getContext(), "제목과 내용을 모두 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference()
                .child("board").child("general_boards").child("board_id_3").child("posts").child(postIdNotice);

        postRef.child("title").setValue(newTitle);
        postRef.child("content").setValue(newContent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireActivity(), "게시글이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "게시글 수정에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to update post", e);
                });
    }
}
