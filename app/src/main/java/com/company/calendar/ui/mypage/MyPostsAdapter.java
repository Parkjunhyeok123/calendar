package com.company.calendar.ui.mypage;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.company.calendar.ui.model.Post;
import com.company.calendar.ui.community.PostDetail2Fragment;
import com.company.calendar.ui.community.PostDetailFragment;

import java.util.List;

import utils.TimeUtils;

public class MyPostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_BOARD_HEADER = 0;
    private static final int TYPE_POST_ITEM = 1;

    private final List<Object> items;
    private final Context context;

    public MyPostsAdapter(List<Object> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_BOARD_HEADER : TYPE_POST_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_BOARD_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_header_item, parent, false);
            return new BoardHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_item, parent, false);
            return new MyPostsViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BoardHeaderViewHolder) {
            // BoardHeaderViewHolder 처리
            String boardName = (String) items.get(position);
            if (boardName != null && !boardName.isEmpty()) {
                ((BoardHeaderViewHolder) holder).bind(boardName);
                Log.d(TAG, "Board name: " + boardName);
            } else {
                Log.e(TAG, "Board name is null or empty at position: " + position);
            }
        } else if (holder instanceof MyPostsViewHolder) {
            // MyPostsViewHolder 처리
            Post post = (Post) items.get(position);
            if (post == null) {
                Log.e(TAG, "Post object is null at position: " + position);
                return;
            }

            // 디버깅 로그 추가
            Log.d(TAG, "Binding Post: " + post.toString());

            if ("충전소 리뷰".equals(post.getBoardType())) {
                Log.d(TAG, "Station Name: " + post.getStationName());
                Log.d(TAG, "Review ID: " + post.getReviewId());
            }

            ((MyPostsViewHolder) holder).bind(post);

            holder.itemView.setOnClickListener(v -> {
                switch (post.getBoardType()) {
                    case "질문 게시판":
                        navigateToFragment(new PostDetailFragment(), post.getPostId(), post.getBoardType());
                        break;
                    case "자유 게시판":
                        navigateToFragment(new PostDetail2Fragment(), post.getPostId(), post.getBoardType());
                        break;
                    
                    default:
                        Log.e(TAG, "Invalid board type: " + post.getBoardType());
                }
            });
        }
    }



    private void navigateToFragment(Fragment fragment, String postId, String boardType) {
        FragmentActivity activity = (FragmentActivity) context;
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        int containerId = boardType.equals("질문 게시판") ? R.id.fragmentContainerQuestion : R.id.fragment_container;

        RecyclerView recyclerView = activity.findViewById(R.id.recyclerViewMyPosts);
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }

        FrameLayout fragmentContainer = activity.findViewById(containerId);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
        } else {
            Log.e(TAG, "Container ID " + containerId + " not found");
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("postId", postId);
        fragment.setArguments(bundle);

        transaction.replace(containerId, fragment);
        transaction.addToBackStack("post_detail_stack");

        try {
            transaction.commit();
            Log.d(TAG, "Transaction committed successfully.");
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to commit transaction: " + e.getMessage());
        }

        int backStackCount = fragmentManager.getBackStackEntryCount();
        Log.d(TAG, "Navigated to fragment: " + fragment.getClass().getSimpleName() + " with postId: " + postId);
        Log.d(TAG, "Back stack entry count after navigation: " + backStackCount);
    }




    private void navigateToActivity(Class<?> activityClass, String reviewId) {
        if (reviewId == null || reviewId.isEmpty()) {
            Log.e(TAG, "Invalid reviewId for activity navigation: " + reviewId);
            return;
        }
        Intent intent = new Intent(context, activityClass);
        intent.putExtra("reviewId", reviewId); // reviewId 전달
        Log.d(TAG, "Navigating to activity: " + activityClass.getSimpleName() + " with reviewId: " + reviewId);
        context.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class BoardHeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView boardNameTextView;

        public BoardHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            boardNameTextView = itemView.findViewById(R.id.boardNameTextView);
        }

        public void bind(String boardName) {
            boardNameTextView.setText(boardName);
        }
    }

    public static class MyPostsViewHolder extends RecyclerView.ViewHolder {
        private final TextView postTitleTextView;
        private final TextView postTimeTextView;
        private final TextView postViewCountTextView;

        public MyPostsViewHolder(@NonNull View itemView) {
            super(itemView);

            postTitleTextView = itemView.findViewById(R.id.textViewTitle);
            postTimeTextView = itemView.findViewById(R.id.textViewTime);
            postViewCountTextView = itemView.findViewById(R.id.textViewViewCount);
        }

        public void bind(Post post) {
            postTitleTextView.setText(post.getTitle() != null ? post.getTitle() : "제목 없음");
            postTimeTextView.setText(post.getTimestamp() != 0
                    ? TimeUtils.getTimeDifference(post.getTimestamp())
                    : "작성 시간 없음");
            postViewCountTextView.setText("조회수: " + post.getViewCount());

            // 충전소 리뷰인 경우 추가 정보 표시
            if ("충전소 리뷰".equals(post.getBoardType())) {
                Log.d(TAG, "Station Name: " + post.getStationName());
                Log.d(TAG, "Review ID: " + post.getReviewId());
            }
        }
    }
}
