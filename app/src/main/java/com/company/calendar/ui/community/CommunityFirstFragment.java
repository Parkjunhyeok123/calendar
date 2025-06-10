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

public class CommunityFirstFragment extends Fragment {

    private Button buttonNewPostQuestion;
    private ListView listViewPostsQuestion;
    private FrameLayout fragmentContainerQuestion;
    private FirebaseAuth firebaseAuthQuestion;
    private FirebaseUser currentUserQuestion;
    private DatabaseReference databaseReferenceQuestion;
    private PostListAdapter postListAdapterQuestion;
    private List<Post> postListQuestion;
    private ValueEventListener postsListenerQuestion;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_community_first, container, false);

        buttonNewPostQuestion = root.findViewById(R.id.buttonNewPostQuestion);
        listViewPostsQuestion = root.findViewById(R.id.listViewPostsQuestion);
        fragmentContainerQuestion = root.findViewById(R.id.fragmentContainerQuestion);

        firebaseAuthQuestion = FirebaseAuth.getInstance();
        currentUserQuestion = firebaseAuthQuestion.getCurrentUser();
        databaseReferenceQuestion = FirebaseDatabase.getInstance().getReference()
                .child("board")
                .child("general_boards")
                .child("board_id_1")
                .child("posts");

        buttonNewPostQuestion.setOnClickListener(v -> {
            if (currentUserQuestion != null) {
                openQuestionWritePostFragment();
            } else {
                Toast.makeText(getContext(), "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), AppActivity.class));
            }
        });

        listViewPostsQuestion.setOnItemClickListener((parent, view, position, id) -> {
            Post selectedPost = postListQuestion.get(position);
            incrementViewCountAndOpenDetail(selectedPost);
        });

        return root;
    }

    private void openQuestionWritePostFragment() {
        QuestionWritePostFragment questionWritePostFragment = new QuestionWritePostFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainerQuestion, questionWritePostFragment, "QuestionWritePostFragment");
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();

        listViewPostsQuestion.setVisibility(View.GONE);
        buttonNewPostQuestion.setVisibility(View.GONE);
        fragmentContainerQuestion.setVisibility(View.VISIBLE);
    }

    private void incrementViewCountAndOpenDetail(Post post) {
        DatabaseReference postRef = databaseReferenceQuestion.child(post.getPostId());
        postRef.child("viewCount").setValue(post.getViewCount() + 1)
                .addOnSuccessListener(aVoid -> openPostDetailFragment(post))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "조회수 증가 실패", Toast.LENGTH_SHORT).show());
    }

    private void openPostDetailFragment(Post selectedPost) {
        PostDetailFragment postDetailFragment = new PostDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("postId", selectedPost.getPostId());
        postDetailFragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainerQuestion, postDetailFragment, "PostDetailFragment");
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();

        listViewPostsQuestion.setVisibility(View.GONE);
        buttonNewPostQuestion.setVisibility(View.GONE);
        fragmentContainerQuestion.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeUI();
        retrievePosts();
    }

    private void initializeUI() {
        postListQuestion = new ArrayList<>();
        postListAdapterQuestion = new PostListAdapter(getActivity(), R.layout.post_list_item, postListQuestion);
        listViewPostsQuestion.setAdapter(postListAdapterQuestion);

        listViewPostsQuestion.setVisibility(View.VISIBLE);
        buttonNewPostQuestion.setVisibility(View.VISIBLE);
        fragmentContainerQuestion.setVisibility(View.GONE);
    }

    private void retrievePosts() {
        postListQuestion.clear();
        postListAdapterQuestion.notifyDataSetChanged();

        postsListenerQuestion = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postListQuestion.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        postListQuestion.add(post);
                    }
                }
                postListAdapterQuestion.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        };

        databaseReferenceQuestion.addValueEventListener(postsListenerQuestion);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (postsListenerQuestion != null) {
            databaseReferenceQuestion.removeEventListener(postsListenerQuestion);
        }
        // UI 초기화
        initializeUI();
    }
}