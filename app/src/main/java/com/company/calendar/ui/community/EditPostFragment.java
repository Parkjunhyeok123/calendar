package com.company.calendar.ui.community;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.text.TextUtils;
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

public class EditPostFragment extends Fragment {

    private EditText editTextTitleQuestion, editTextContentQuestion;
    private Button buttonSaveQuestion, buttonCancelQuestion;
    private String postIdQuestion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_post_question, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextTitleQuestion = view.findViewById(R.id.editTextTitleQuestion);
        editTextContentQuestion = view.findViewById(R.id.editTextContentQuestion);
        buttonSaveQuestion = view.findViewById(R.id.buttonSaveQuestion);
        buttonCancelQuestion = view.findViewById(R.id.buttonCancelQuestion);

        postIdQuestion = getArguments() != null ? getArguments().getString("postId") : null;

        buttonCancelQuestion.setOnClickListener(v -> requireActivity().onBackPressed());
        buttonSaveQuestion.setOnClickListener(v -> saveChanges());

        loadPostDetails();
    }

    private void loadPostDetails() {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference()
                .child("board").child("general_boards").child("board_id_1").child("posts").child(postIdQuestion);

        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String title = dataSnapshot.child("title").getValue(String.class);
                String content = dataSnapshot.child("content").getValue(String.class);

                editTextTitleQuestion.setText(title);
                editTextContentQuestion.setText(content);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load post details", databaseError.toException());
            }
        });
    }


    private void saveChanges() {
        String newTitle = editTextTitleQuestion.getText().toString().trim();
        String newContent = editTextContentQuestion.getText().toString().trim();

        if (TextUtils.isEmpty(newTitle) || TextUtils.isEmpty(newContent)) {
            Toast.makeText(getContext(), "제목과 내용을 모두 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference()
                .child("board").child("general_boards").child("board_id_1").child("posts").child(postIdQuestion);

        postRef.child("title").setValue(newTitle);
        postRef.child("content").setValue(newContent)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        Toast.makeText(requireActivity(), "게시글이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "게시글 수정에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
