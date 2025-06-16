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
import android.widget.ImageButton;
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

public class PostDetail3Fragment extends Fragment {

    private TextView textViewTitleNotice, textViewContentNotice, textViewAuthor, textViewTime, textViewViewCount;
    private EditText editTextCommentNotice;
    private ImageButton buttonSubmitCommentNotice, buttonEditNotice, buttonDeleteNotice;
    private RecyclerView recyclerViewCommentsNotice;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();
    private String postId;
    private DatabaseReference postsRef, commentsRef;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post_detail3, container, false);

        initializeViews(rootView);

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
        setCommentInputRestrictions();

        return rootView;
    }

    private void setCommentInputRestrictions() {
        editTextCommentNotice.setFilters(new InputFilter[]{
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
        textViewTitleNotice = rootView.findViewById(R.id.textViewTitleNotice);
        textViewContentNotice = rootView.findViewById(R.id.textViewContentNotice);
        textViewAuthor = rootView.findViewById(R.id.text_view_author);
        textViewTime = rootView.findViewById(R.id.text_view_time);
        textViewViewCount = rootView.findViewById(R.id.text_view_view_count);
        editTextCommentNotice = rootView.findViewById(R.id.editTextCommentNotice);
        buttonSubmitCommentNotice = rootView.findViewById(R.id.buttonSubmitCommentNotice);
        buttonEditNotice = rootView.findViewById(R.id.buttonEditNotice);
        buttonDeleteNotice = rootView.findViewById(R.id.buttonDeleteNotice);
        recyclerViewCommentsNotice = rootView.findViewById(R.id.recyclerViewCommentsNotice);

        if (textViewAuthor == null) Log.e("PostDetail3Fragment", "textViewAuthor is null");
        if (textViewViewCount == null) Log.e("PostDetail3Fragment", "textViewViewCount is null");
        if (textViewTime == null) Log.e("PostDetail3Fragment", "textViewTime is null");
    }

    private void initializeFirebaseReferences() {
        commentsRef = FirebaseDatabase.getInstance().getReference().child("comments").child(postId);
        postsRef = FirebaseDatabase.getInstance().getReference()
                .child("board").child("general_boards").child("board_id_3").child("posts").child(postId);
    }

    private void setupRecyclerView() {
        recyclerViewCommentsNotice.setLayoutManager(new LinearLayoutManager(getContext()));
        commentAdapter = new CommentAdapter(commentList, postId);
        recyclerViewCommentsNotice.setAdapter(commentAdapter);
    }

    private void setButtonListeners() {
        buttonSubmitCommentNotice.setOnClickListener(v -> addComment());
        buttonEditNotice.setOnClickListener(v -> editPost());
        buttonDeleteNotice.setOnClickListener(v -> deletePost());
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
        if (currentUser != null) {
            String currentUid = currentUser.getUid();

            // Firebase에서 현재 사용자의 role을 가져옴
            DatabaseReference roleRef = FirebaseDatabase.getInstance()
                    .getReference("UserAccount").child(currentUid).child("role");

            roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String role = snapshot.getValue(String.class);
                    boolean isAdmin = "admin".equals(role);
                    boolean isAuthor = currentUid.equals(authorId);

                    // 작성자는 수정 가능
                    buttonEditNotice.setVisibility(isAuthor ? View.VISIBLE : View.GONE);

                    // 작성자 또는 관리자면 삭제 가능
                    if (isAuthor || isAdmin) {
                        buttonDeleteNotice.setVisibility(View.VISIBLE);
                    } else {
                        buttonDeleteNotice.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "권한 확인 실패", Toast.LENGTH_SHORT).show();
                    buttonEditNotice.setVisibility(View.GONE);
                    buttonDeleteNotice.setVisibility(View.GONE);
                }
            });
        }
    }


    private void updatePostDetails(String title, String content, String author, long timestamp, Integer viewCount) {
        textViewTitleNotice.setText(title != null ? title : "");
        textViewContentNotice.setText(content != null ? content : "");
        textViewAuthor.setText(author != null ? author : "알 수 없음");
        textViewViewCount.setText("조회수: " + (viewCount != null ? viewCount : 0));
        textViewTime.setText(TimeUtils.getTimeDifference(timestamp));
    }

    private void addComment() {
        String commentText = editTextCommentNotice.getText().toString().trim();
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
                    .addOnSuccessListener(aVoid -> editTextCommentNotice.setText(""))
                    .addOnFailureListener(e -> showError("댓글 추가에 실패했습니다."));
        } else {
            showError("로그인이 필요합니다.");
        }
    }

    private void editPost() {
        Fragment editPostFragment = new Edit3PostFragment();
        Bundle bundle = new Bundle();
        bundle.putString("postId", postId);
        editPostFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_notice, editPostFragment)
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
        requireActivity().onBackPressed();
    }
}
