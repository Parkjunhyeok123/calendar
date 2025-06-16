package com.company.calendar.ui.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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

    private Spinner spinnerSearchType;
    private EditText editTextSearch;
    private ImageButton buttonSearch;

    private LinearLayout bottomActionLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_community_first, container, false);

        buttonNewPostQuestion = root.findViewById(R.id.buttonNewPostQuestion);
        listViewPostsQuestion = root.findViewById(R.id.listViewPostsQuestion);
        fragmentContainerQuestion = root.findViewById(R.id.fragmentContainerQuestion);

        spinnerSearchType = root.findViewById(R.id.spinnerSearchType);
        editTextSearch = root.findViewById(R.id.editTextSearch);
        buttonSearch = root.findViewById(R.id.buttonSearch);

        bottomActionLayout = root.findViewById(R.id.bottom_action_layout);

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
        buttonSearch.setOnClickListener(v -> searchPosts());

        return root;


    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final FragmentManager fragmentManager = getParentFragmentManager();

        fragmentManager.addOnBackStackChangedListener(() -> {
            // 프래그먼트가 아직 attached 되었는지 체크
            if (!isAdded()) return;

            Fragment writePostFragment = fragmentManager.findFragmentByTag("QuestionWritePostFragment");
            Fragment postDetailFragment = fragmentManager.findFragmentByTag("PostDetailFragment");

            if (writePostFragment == null && postDetailFragment == null) {
                listViewPostsQuestion.setVisibility(View.VISIBLE);
                buttonNewPostQuestion.setVisibility(View.VISIBLE);
                fragmentContainerQuestion.setVisibility(View.GONE);

                bottomActionLayout.setVisibility(View.VISIBLE);
                editTextSearch.setVisibility(View.VISIBLE);
                spinnerSearchType.setVisibility(View.VISIBLE);
                buttonSearch.setVisibility(View.VISIBLE);
            }
        });
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
        bottomActionLayout.setVisibility(View.GONE);
        editTextSearch.setVisibility(View.GONE);
        spinnerSearchType.setVisibility(View.GONE);
        buttonSearch.setVisibility(View.GONE);
    }
    private void searchPosts() {
        String keyword = editTextSearch.getText().toString().trim();
        String searchType = spinnerSearchType.getSelectedItem().toString();

        if (keyword.isEmpty()) {
            Toast.makeText(getContext(), "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
            retrievePosts();  // 전체 게시글을 다시 불러오는 메서드 (기존에 정의되어 있다고 가정)
            return;
        }

        databaseReferenceQuestion.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postListQuestion.clear();
                List<Post> filteredPosts = new ArrayList<>();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        boolean matches = false;
                        switch (searchType) {
                            case "제목":
                                matches = post.getTitle() != null && post.getTitle().toLowerCase().contains(keyword.toLowerCase());
                                break;
                            case "글쓴이":
                                matches = post.getAuthor() != null && post.getAuthor().toLowerCase().contains(keyword.toLowerCase());
                                break;
                            case "내용":
                                matches = post.getContent() != null && post.getContent().toLowerCase().contains(keyword.toLowerCase());
                                break;
                            default:
                                matches = post.getTitle() != null && post.getTitle().toLowerCase().contains(keyword.toLowerCase());
                        }

                        if (matches) {
                            filteredPosts.add(post);
                        }
                    }
                }

                postListAdapterQuestion.clear();
                postListAdapterQuestion.addAll(filteredPosts);
                postListAdapterQuestion.notifyDataSetChanged();

                if (filteredPosts.isEmpty()) {
                    Toast.makeText(getContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "검색 완료", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
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

        bottomActionLayout.setVisibility(View.GONE);
        editTextSearch.setVisibility(View.GONE);
        spinnerSearchType.setVisibility(View.GONE);
        buttonSearch.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();

        listViewPostsQuestion.setVisibility(View.VISIBLE);
        buttonNewPostQuestion.setVisibility(View.VISIBLE);
        fragmentContainerQuestion.setVisibility(View.GONE);

        bottomActionLayout.setVisibility(View.VISIBLE);
        editTextSearch.setVisibility(View.VISIBLE);
        spinnerSearchType.setVisibility(View.VISIBLE);
        buttonSearch.setVisibility(View.VISIBLE);

        retrievePosts(); // 목록 새로고침
    }


    private void initializeUI() {
        postListQuestion = new ArrayList<>();
        postListAdapterQuestion = new PostListAdapter(getActivity(), R.layout.post_list_item, postListQuestion);
        listViewPostsQuestion.setAdapter(postListAdapterQuestion);

        listViewPostsQuestion.setVisibility(View.VISIBLE);
        buttonNewPostQuestion.setVisibility(View.VISIBLE);
        fragmentContainerQuestion.setVisibility(View.GONE);
        bottomActionLayout.setVisibility(View.VISIBLE);
        editTextSearch.setVisibility(View.VISIBLE);
        spinnerSearchType.setVisibility(View.VISIBLE);
        buttonSearch.setVisibility(View.VISIBLE);
    }

    private void retrievePosts() {
        if (postListQuestion == null) {
            postListQuestion = new ArrayList<>();
        }
        if (postListAdapterQuestion == null) {
            postListAdapterQuestion = new PostListAdapter(getActivity(), R.layout.post_list_item, postListQuestion);
            listViewPostsQuestion.setAdapter(postListAdapterQuestion);
        }

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

    }
}