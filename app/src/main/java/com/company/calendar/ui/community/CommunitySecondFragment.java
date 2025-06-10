package com.company.calendar.ui.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.company.calendar.R;
import com.company.calendar.ui.login.AppActivity;
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

public class CommunitySecondFragment extends Fragment {

    private Button buttonNewPost;
    private ListView listViewPosts;
    private FrameLayout fragmentContainer;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private PostListAdapter postListAdapter;
    private List<Post> postList;
    private ValueEventListener postsListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_community_second, container, false);

        buttonNewPost = root.findViewById(R.id.buttonNewPost);
        listViewPosts = root.findViewById(R.id.listViewPosts);
        fragmentContainer = root.findViewById(R.id.fragment_container);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("board")
                .child("general_boards")
                .child("board_id_2")  // 자유게시판 경로
                .child("posts");

        buttonNewPost.setOnClickListener(v -> {
            if (currentUser != null) {
                openFreeWritePostFragment();
            } else {
                Toast.makeText(getContext(), "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), AppActivity.class));
            }
        });

        listViewPosts.setOnItemClickListener((parent, view, position, id) -> {
            Post selectedPost = postList.get(position);
            incrementViewCountAndOpenDetail(selectedPost);
        });

        return root;
    }

    private void openFreeWritePostFragment() {
        FreeWritePostFragment freeWritePostFragment = new FreeWritePostFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, freeWritePostFragment, "FreeWritePostFragment");
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();

        listViewPosts.setVisibility(View.GONE);
        buttonNewPost.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
    }

    private void incrementViewCountAndOpenDetail(Post post) {
        DatabaseReference postRef = databaseReference.child(post.getPostId());
        postRef.child("viewCount").setValue(post.getViewCount() + 1)
                .addOnSuccessListener(aVoid -> openPostDetail2Fragment(post))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "조회수 증가 실패", Toast.LENGTH_SHORT).show());
    }

    private void openPostDetail2Fragment(Post selectedPost) {
        PostDetail2Fragment postDetail2Fragment = new PostDetail2Fragment();
        Bundle bundle = new Bundle();
        bundle.putString("postId", selectedPost.getPostId());
        postDetail2Fragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, postDetail2Fragment, "PostDetail2Fragment");
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();

        listViewPosts.setVisibility(View.GONE);
        buttonNewPost.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeUI();
        retrievePosts();
    }

    private void initializeUI() {
        postList = new ArrayList<>();
        postListAdapter = new PostListAdapter(getActivity(), R.layout.post_list_item, postList);
        listViewPosts.setAdapter(postListAdapter);

        listViewPosts.setVisibility(View.VISIBLE);
        buttonNewPost.setVisibility(View.VISIBLE);
        fragmentContainer.setVisibility(View.GONE);
    }

    private void retrievePosts() {
        postList.clear();
        postListAdapter.notifyDataSetChanged();

        postsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        postList.add(post);
                    }
                }
                postListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        };

        databaseReference.addValueEventListener(postsListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (postsListener != null) {
            databaseReference.removeEventListener(postsListener);
        }
        // UI 초기화
        postList.clear(); // 게시글 목록을 초기화
        postListAdapter.notifyDataSetChanged(); // 목록 갱신
        listViewPosts.setVisibility(View.VISIBLE); // 목록 보이기
        buttonNewPost.setVisibility(View.VISIBLE); // 새 게시글 버튼 보이기
        fragmentContainer.setVisibility(View.GONE); // 프래그먼트 컨테이너 숨기기
    }

}