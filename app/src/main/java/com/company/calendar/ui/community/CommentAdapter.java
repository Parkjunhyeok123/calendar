package com.company.calendar.ui.community;

import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.company.calendar.ui.community.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentsList;
    private DatabaseReference commentsRef;
    private DatabaseReference likesRef;
    private String reviewId;

    public CommentAdapter(List<Comment> commentsList, String reviewId) {
        this.commentsList = commentsList != null ? commentsList : new ArrayList<>();
        this.reviewId = reviewId;
        this.commentsRef = FirebaseDatabase.getInstance().getReference().child("comments").child(reviewId);
        this.likesRef = FirebaseDatabase.getInstance().getReference().child("likes").child(reviewId);
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentsList.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return commentsList != null ? commentsList.size() : 0;
    }

    public void setComments(List<Comment> comments) {
        this.commentsList = comments;
        notifyDataSetChanged();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {

        private TextView commentTextView;
        private ImageButton likeCommentButton, deleteCommentButton;
        private TextView likeCountTextView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentTextView = itemView.findViewById(R.id.comment_text_view);
            likeCommentButton = itemView.findViewById(R.id.like_comment_button);
            deleteCommentButton = itemView.findViewById(R.id.delete_comment_button);
            likeCountTextView = itemView.findViewById(R.id.like_count);

            likeCommentButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Comment comment = commentsList.get(position);
                    toggleLike(comment.getCommentId());
                }
            });
        }

        public void bind(Comment comment) {
            commentTextView.setText(comment.getContent());
            updateLikeButton(comment.getCommentId());

            // 현재 로그인한 사용자와 댓글 작성자가 동일할 때만 삭제 버튼을 표시
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && comment.getUserId().equals(currentUser.getUid())) {
                deleteCommentButton.setVisibility(View.VISIBLE); // 삭제 버튼 보이기
                deleteCommentButton.setOnClickListener(v -> {
                    confirmDeleteComment(comment.getCommentId()); // 삭제 확인 다이얼로그 표시
                });
            } else {
                deleteCommentButton.setVisibility(View.GONE); // 삭제 버튼 숨기기
            }
        }

        private void confirmDeleteComment(String commentId) {
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("댓글 삭제")
                    .setMessage("정말 이 댓글을 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> deleteComment(commentId))
                    .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                    .show();
        }

        private void deleteComment(String commentId) {
            commentsRef.child(commentId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(itemView.getContext(), "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(itemView.getContext(), "댓글 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    });
        }

        private void toggleLike(String commentId) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String currentUserId = currentUser.getUid();
                DatabaseReference userLikeRef = likesRef.child(commentId).child(currentUserId);

                userLikeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            userLikeRef.removeValue();
                            decrementLikeCount(commentId);
                        } else {
                            userLikeRef.setValue(true);
                            incrementLikeCount(commentId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("CommentViewHolder", "Failed to toggle like", databaseError.toException());
                    }
                });
            } else {
                Toast.makeText(itemView.getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }

        private void incrementLikeCount(String commentId) {
            DatabaseReference commentLikeCountRef = commentsRef.child(commentId).child("likeCount");

            commentLikeCountRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    Integer currentCount = mutableData.getValue(Integer.class);
                    if (currentCount == null) {
                        mutableData.setValue(1);
                    } else {
                        mutableData.setValue(currentCount + 1);
                    }
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    if (error != null) {
                        Log.e("CommentViewHolder", "Failed to increment like count", error.toException());
                    } else if (committed && currentData != null) {
                        Integer updatedCount = currentData.getValue(Integer.class);
                        likeCountTextView.setText(String.valueOf(updatedCount));
                        likeCommentButton.setImageResource(R.drawable.ic_heart_filled);
                    }
                }
            });
        }

        private void decrementLikeCount(String commentId) {
            DatabaseReference commentLikeCountRef = commentsRef.child(commentId).child("likeCount");

            commentLikeCountRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    Integer currentCount = mutableData.getValue(Integer.class);
                    if (currentCount != null && currentCount > 0) {
                        mutableData.setValue(currentCount - 1);
                    }
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    if (error != null) {
                        Log.e("CommentViewHolder", "Failed to decrement like count", error.toException());
                    } else if (committed && currentData != null) {
                        Integer updatedCount = currentData.getValue(Integer.class);
                        likeCountTextView.setText(String.valueOf(updatedCount));
                        likeCommentButton.setImageResource(R.drawable.ic_heart_outline);
                    }
                }
            });
        }

        private void updateLikeButton(String commentId) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                likeCommentButton.setImageResource(R.drawable.ic_heart_outline);
                return;
            }

            DatabaseReference userLikeRef = likesRef.child(commentId).child(currentUser.getUid());
            userLikeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        likeCommentButton.setImageResource(R.drawable.ic_heart_filled);
                    } else {
                        likeCommentButton.setImageResource(R.drawable.ic_heart_outline);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("CommentViewHolder", "Failed to read like status", databaseError.toException());
                }
            });

            commentsRef.child(commentId).child("likeCount").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Integer likeCount = dataSnapshot.getValue(Integer.class);
                    if (likeCount != null) {
                        likeCountTextView.setText(String.valueOf(likeCount));
                    } else {
                        likeCountTextView.setText("0");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("CommentViewHolder", "Failed to read like count", databaseError.toException());
                }
            });
        }
    }
}
