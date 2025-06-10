package com.company.calendar.ui.mypage;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.company.calendar.ui.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MypageFourthFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyPostsAdapter myPostsAdapter;
    private List<Object> myPostsList; // 게시판 이름 + 게시글 포함
    private TextView emptyView;
    private static final String TAG = "MypageFourthFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mypage_fourth, container, false);

        recyclerView = root.findViewById(R.id.recyclerViewMyPosts);
        emptyView = root.findViewById(R.id.emptyView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        myPostsList = new ArrayList<>();

        myPostsAdapter = new MyPostsAdapter(myPostsList, requireActivity());
        recyclerView.setAdapter(myPostsAdapter);

        loadMyPosts(); // 게시글 로드

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                int backStackCount = fragmentManager.getBackStackEntryCount();
                Log.d(TAG, "Back stack entry count on back stack change: " + backStackCount);

                if (backStackCount == 0) {
                    Log.d(TAG, "Back stack is empty. Restoring RecyclerView.");
                    recyclerView.setVisibility(View.VISIBLE);

                    View fragmentContainer = activity.findViewById(R.id.fragment_container);
                    if (fragmentContainer != null) {
                        fragmentContainer.setVisibility(View.GONE);
                    }

                    View fragmentContainerQuestion = activity.findViewById(R.id.fragmentContainerQuestion);
                    if (fragmentContainerQuestion != null) {
                        fragmentContainerQuestion.setVisibility(View.GONE);
                    }
                } else {
                    Log.d(TAG, "Still in fragment, back stack is not empty");
                }
            });
        }
    }






    private void hideFragmentContainers(FragmentActivity activity) {
        View fragmentContainerQuestion = activity.findViewById(R.id.fragmentContainerQuestion);
        if (fragmentContainerQuestion != null) {
            Log.d(TAG, "Hiding fragmentContainerQuestion, current visibility: " + fragmentContainerQuestion.getVisibility());
            fragmentContainerQuestion.setVisibility(View.GONE);
        }

        View fragmentContainerFree = activity.findViewById(R.id.fragment_container);
        if (fragmentContainerFree != null) {
            Log.d(TAG, "Hiding fragment_container, current visibility: " + fragmentContainerFree.getVisibility());
            fragmentContainerFree.setVisibility(View.GONE);
        }
    }






    @Override
    public void onPause() {
        super.onPause();

        FragmentActivity activity = getActivity();
        if (activity != null) {
            // 백 스택 변경 리스너 제거
            activity.getSupportFragmentManager().removeOnBackStackChangedListener(null);
        }
    }

    private void loadMyPosts() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "Current user ID: " + userId);

            // 게시판 순서 고정
            String[] boardIds = {"board_id_1", "board_id_2", "board_id_4"};
            String[] boardNames = {"질문 게시판", "사원 칭찬 게시판", "공지 사항"};

            myPostsList.clear();

            for (int i = 0; i < boardIds.length; i++) {
                String boardId = boardIds[i];
                String boardName = boardNames[i];

                DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference()
                        .child("board")
                        .child("general_boards")
                        .child(boardId)
                        .child("posts");

                postsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean hasPosts = false;

                        List<Post> postsForThisBoard = new ArrayList<>();
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Post post = postSnapshot.getValue(Post.class);
                            if (post != null) {
                                post.setBoardType(boardName); // boardType 수동 설정
                                postsForThisBoard.add(post);
                                hasPosts = true;
                            } else {
                                Log.e(TAG, "Failed to map post data at: " + postSnapshot.getKey());
                            }
                        }

                        if (hasPosts) {
                            myPostsList.add(boardName); // 게시판 이름 추가
                            myPostsList.addAll(postsForThisBoard); // 해당 게시판의 게시글 추가
                            Log.d(TAG, "Loaded posts for board: " + boardName);
                        }

                        myPostsAdapter.notifyDataSetChanged();
                        updateEmptyView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Failed to load posts: " + databaseError.getMessage());
                    }
                });
            }
        } else {
            Log.e(TAG, "No user is currently logged in.");
        }
    }

    private void updateEmptyView() {
        if (myPostsList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "RecyclerView updated. Post count: " + myPostsList.size());
    }
}
