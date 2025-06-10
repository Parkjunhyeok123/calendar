package com.company.calendar.ui.community;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.company.calendar.ui.community.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import utils.TimeUtils;

public class    PostDetailFragment extends Fragment {

    private TextView textViewTitleQuestion, textViewContentQuestion, textViewAuthorQuestion, textViewTimeQuestion, textViewViewCountQuestion;
    private EditText editTextCommentQuestion;
    private Button buttonSubmitCommentQuestion, buttonEditPostQuestion, buttonDeletePostQuestion;
    private RecyclerView recyclerViewCommentsQuestion;
    private CommentAdapter commentAdapterQuestion;
    private List<Comment> commentListQuestion = new ArrayList<>();
    private String postIdQuestion;
    private DatabaseReference postsRefQuestion, commentsRefQuestion;
    private FirebaseUser currentUserQuestion;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post_detail_question, container, false);

        initializeViews(rootView);
        currentUserQuestion = FirebaseAuth.getInstance().getCurrentUser();

        postIdQuestion = getArguments() != null ? getArguments().getString("postId") : null;

        if (postIdQuestion == null) {
            showErrorAndNavigateBack("게시물 ID를 찾을 수 없습니다.");
            return rootView;
        }

        initializeFirebaseReferences();
        setupRecyclerView();
        setButtonListeners();
        loadPostDetails();
        loadComments();

        setCommentInputRestrictions();

        return rootView;
    }

    private void setCommentInputRestrictions() {
        editTextCommentQuestion.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(50), // 최대 50자 제한
                (source, start, end, dest, dstart, dend) -> source.toString().contains("\n") ? "" : source
        });
    }

    private void loadComments() {
        commentsRefQuestion.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentListQuestion.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    if (comment != null) {
                        commentListQuestion.add(comment);
                    }
                }
                commentAdapterQuestion.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showError("댓글을 불러오는 데 실패했습니다.");
            }
        });
    }

    private void initializeViews(View rootView) {
        textViewTitleQuestion = rootView.findViewById(R.id.textViewTitleQuestion);
        textViewContentQuestion = rootView.findViewById(R.id.textViewContentQuestion);
        textViewAuthorQuestion = rootView.findViewById(R.id.textViewAuthorQuestion);
        textViewTimeQuestion = rootView.findViewById(R.id.textViewTimeQuestion);
        textViewViewCountQuestion = rootView.findViewById(R.id.textViewViewCountQuestion);
        editTextCommentQuestion = rootView.findViewById(R.id.editTextCommentQuestion);
        buttonSubmitCommentQuestion = rootView.findViewById(R.id.buttonSubmitCommentQuestion);
        buttonEditPostQuestion = rootView.findViewById(R.id.buttonEditPostQuestion);
        buttonDeletePostQuestion = rootView.findViewById(R.id.buttonDeletePostQuestion);
        recyclerViewCommentsQuestion = rootView.findViewById(R.id.recyclerViewCommentsQuestion);
    }

    private void initializeFirebaseReferences() {
        commentsRefQuestion = FirebaseDatabase.getInstance().getReference().child("comments").child(postIdQuestion);
        postsRefQuestion = FirebaseDatabase.getInstance().getReference()
                .child("board").child("general_boards").child("board_id_1").child("posts").child(postIdQuestion);
    }

    private void setupRecyclerView() {
        recyclerViewCommentsQuestion.setLayoutManager(new LinearLayoutManager(getContext()));
        commentAdapterQuestion = new CommentAdapter(commentListQuestion, postIdQuestion);
        recyclerViewCommentsQuestion.setAdapter(commentAdapterQuestion);
    }

    private void setButtonListeners() {
        buttonSubmitCommentQuestion.setOnClickListener(v -> addComment());
        buttonEditPostQuestion.setOnClickListener(v -> editPost());
        buttonDeletePostQuestion.setOnClickListener(v -> deletePost());
    }

    private void loadPostDetails() {
        postsRefQuestion.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String title = dataSnapshot.child("title").getValue(String.class);
                    String content = dataSnapshot.child("content").getValue(String.class);
                    String authorName = dataSnapshot.child("author").getValue(String.class);
                    String authorId = dataSnapshot.child("userId").getValue(String.class);
                    long timestamp = dataSnapshot.child("timestamp").getValue(Long.class);
                    Integer viewCount = dataSnapshot.child("viewCount").getValue(Integer.class);

                    updatePostDetails(title, content, authorName, timestamp, viewCount);
                    manageEditAndDeleteButtonsVisibility(authorId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showError("게시글을 불러오는 데 실패했습니다.");
            }
        });
    }

    private void manageEditAndDeleteButtonsVisibility(String authorId) {
        if (currentUserQuestion != null) {
            String currentUid = currentUserQuestion.getUid();

            DatabaseReference roleRef = FirebaseDatabase.getInstance()
                    .getReference("UserAccount").child(currentUid).child("role");

            roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String role = snapshot.getValue(String.class);
                    boolean isAdmin = "admin".equals(role);
                    boolean isAuthor = currentUid.equals(authorId);

                    // 작성자면 수정 가능
                    buttonEditPostQuestion.setVisibility(isAuthor ? View.VISIBLE : View.GONE);

                    // 관리자 또는 작성자는 삭제 가능
                    if (isAdmin || isAuthor) {
                        buttonDeletePostQuestion.setVisibility(View.VISIBLE);
                    } else {
                        buttonDeletePostQuestion.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showError("권한 확인 중 오류가 발생했습니다.");
                    buttonEditPostQuestion.setVisibility(View.GONE);
                    buttonDeletePostQuestion.setVisibility(View.GONE);
                }
            });
        }
    }


    private void updatePostDetails(String title, String content, String author, long timestamp, Integer viewCount) {
        textViewTitleQuestion.setText(title);
        textViewContentQuestion.setText(content);
        textViewAuthorQuestion.setText(author);
        textViewViewCountQuestion.setText("조회수: " + (viewCount != null ? viewCount : 0));
        textViewTimeQuestion.setText(TimeUtils.getTimeDifference(timestamp));
    }

    private void addComment() {
        String commentText = editTextCommentQuestion.getText().toString().trim();
        if (commentText.isEmpty()) {
            showError("댓글을 입력해 주세요.");
            return;
        }

        String userId = currentUserQuestion.getUid();
        DatabaseReference newCommentRef = commentsRefQuestion.push();
        Comment newComment = new Comment(userId, commentText);
        newComment.setCommentId(newCommentRef.getKey());
        newCommentRef.setValue(newComment)
                .addOnSuccessListener(aVoid -> editTextCommentQuestion.setText(""))
                .addOnFailureListener(e -> showError("댓글 추가에 실패했습니다."));
    }

    private void editPost() {
        Fragment editPostFragment = new EditPostFragment();
        Bundle bundle = new Bundle();
        bundle.putString("postId", postIdQuestion);
        editPostFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerQuestion, editPostFragment)
                .addToBackStack(null)
                .commit();
    }

    private void deletePost() {
        new AlertDialog.Builder(getContext())
                .setTitle("게시글 삭제")
                .setMessage("정말 이 게시글을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> postsRefQuestion.removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        })
                        .addOnFailureListener(e -> showError("게시글 삭제에 실패했습니다.")))
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showErrorAndNavigateBack(String message) {
        showError(message);
        getActivity().onBackPressed();
    }
}
