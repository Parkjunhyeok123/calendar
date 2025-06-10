package com.company.calendar.ui.community;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
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

public class PostDetail2Fragment extends Fragment {

    private TextView textViewTitle, textViewContent, textViewAuthor, textViewTime, textViewViewCount;
    private EditText editTextComment;
    private Button buttonSubmitComment, buttonEditPost, buttonDeletePost;
    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();
    private String postId;
    private DatabaseReference postsRef, commentsRef;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post_detail2, container, false);

        initializeViews(rootView);

        // 현재 사용자 정보 가져오기
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        postId = getArguments() != null ? getArguments().getString("postId") : null;

        if (postId == null) {
            showErrorAndNavigateBack("게시물 ID를 찾을 수 없습니다.");
            return rootView;
        }

        initializeFirebaseReferences();
        setupRecyclerView();
        setButtonListeners();
        loadPostDetails();
        loadComments();

        // 댓글 입력 제한 설정
        setCommentInputRestrictions();

        return rootView;
    }

    private void setCommentInputRestrictions() {
        editTextComment.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(50),
                (source, start, end, dest, dstart, dend) -> source.toString().contains("\n") ? "" : source
        });
    }

    private void loadComments() {
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    if (comment != null) {
                        commentList.add(comment);
                    }
                }
                if (commentAdapter != null) {
                    commentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showError("댓글을 불러오는 데 실패했습니다.");
            }
        });
    }

    private void initializeViews(View rootView) {
        textViewTitle = rootView.findViewById(R.id.text_view_title);
        textViewContent = rootView.findViewById(R.id.text_view_content);
        textViewAuthor = rootView.findViewById(R.id.text_view_author);
        textViewTime = rootView.findViewById(R.id.text_view_time);
        textViewViewCount = rootView.findViewById(R.id.text_view_view_count);
        editTextComment = rootView.findViewById(R.id.edit_text_comment);
        buttonSubmitComment = rootView.findViewById(R.id.button_submit_comment);
        buttonEditPost = rootView.findViewById(R.id.button_edit);
        buttonDeletePost = rootView.findViewById(R.id.button_delete);
        recyclerViewComments = rootView.findViewById(R.id.recycler_view_comments);
    }

    private void initializeFirebaseReferences() {
        commentsRef = FirebaseDatabase.getInstance().getReference().child("comments").child(postId);
        postsRef = FirebaseDatabase.getInstance().getReference()
                .child("board").child("general_boards").child("board_id_2").child("posts").child(postId);
    }

    private void setupRecyclerView() {
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(getContext()));
        commentAdapter = new CommentAdapter(commentList, postId);
        recyclerViewComments.setAdapter(commentAdapter);
    }

    private void setButtonListeners() {
        buttonSubmitComment.setOnClickListener(v -> addComment());
        buttonEditPost.setOnClickListener(v -> editPost());
        buttonDeletePost.setOnClickListener(v -> deletePost());
    }

    private void loadPostDetails() {
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
        if (currentUser == null) return;

        String currentUserId = currentUser.getUid();

        // 작성자인 경우
        if (currentUserId.equals(authorId)) {
            buttonEditPost.setVisibility(View.VISIBLE);
            buttonDeletePost.setVisibility(View.VISIBLE);
        } else {
            // 관리자인지 확인
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(currentUserId).child("isAdmin");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean isAdmin = snapshot.getValue(Boolean.class);
                    if (isAdmin != null && isAdmin) {
                        buttonEditPost.setVisibility(View.VISIBLE);
                        buttonDeletePost.setVisibility(View.VISIBLE);
                    } else {
                        buttonEditPost.setVisibility(View.GONE);
                        buttonDeletePost.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("PostDetail2Fragment", "관리자 권한 확인 실패: " + error.getMessage());
                    buttonEditPost.setVisibility(View.GONE);
                    buttonDeletePost.setVisibility(View.GONE);
                }
            });
        }
    }


    private void updatePostDetails(String title, String content, String author, long timestamp, Integer viewCount) {
        textViewTitle.setText(title);
        textViewContent.setText(content);
        textViewAuthor.setText(author);
        textViewViewCount.setText("조회수: " + (viewCount != null ? viewCount : 0));
        textViewTime.setText(TimeUtils.getTimeDifference(timestamp));
    }

    private void addComment() {
        String commentText = editTextComment.getText().toString().trim();
        if (commentText.isEmpty()) {
            showError("댓글을 입력해 주세요.");
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference newCommentRef = commentsRef.push();
            Comment newComment = new Comment(userId, commentText);
            newComment.setCommentId(newCommentRef.getKey());
            newCommentRef.setValue(newComment)
                    .addOnSuccessListener(aVoid -> editTextComment.setText(""))
                    .addOnFailureListener(e -> showError("댓글 추가에 실패했습니다."));
        } else {
            showError("로그인이 필요합니다.");
        }
    }

    private void editPost() {
        Fragment editPostFragment = new Edit2PostFragment();
        Bundle bundle = new Bundle();
        bundle.putString("postId", postId);
        editPostFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editPostFragment)
                .addToBackStack(null)
                .commit();
    }

    private void deletePost() {
        new AlertDialog.Builder(getContext())
                .setTitle("게시글 삭제")
                .setMessage("정말 이 게시글을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> postsRef.removeValue()
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
