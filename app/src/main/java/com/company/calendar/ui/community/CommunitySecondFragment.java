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

    private Button buttonNewPostFree;
    private ListView listViewPostsFree;
    private FrameLayout fragmentContainerFree;

    private FirebaseAuth firebaseAuthFree;
    private FirebaseUser currentUserFree;
    private DatabaseReference databaseReferenceFree;

    private PostListAdapter postListAdapterFree;
    private List<Post> postListFree;
    private ValueEventListener postsListenerFree;

    private LinearLayout bottomActionLayout;

    private Spinner spinnerSearchType;
    private EditText editTextSearch;
    private ImageButton buttonSearch;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_community_second, container, false);

        buttonNewPostFree = root.findViewById(R.id.buttonNewPost);
        listViewPostsFree = root.findViewById(R.id.listViewPosts);
        fragmentContainerFree = root.findViewById(R.id.fragment_container);

        bottomActionLayout = root.findViewById(R.id.bottom_action_layout);

        spinnerSearchType = root.findViewById(R.id.spinnerSearchType);
        editTextSearch = root.findViewById(R.id.editTextSearch);
        buttonSearch = root.findViewById(R.id.buttonSearch);



        firebaseAuthFree = FirebaseAuth.getInstance();
        currentUserFree = firebaseAuthFree.getCurrentUser();
        databaseReferenceFree = FirebaseDatabase.getInstance().getReference()
                .child("board")
                .child("general_boards")
                .child("board_id_2") // 자유게시판
                .child("posts");

        buttonNewPostFree.setOnClickListener(v -> {
            if (currentUserFree != null) {
                openFreeWritePostFragment();
            } else {
                Toast.makeText(getContext(), "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), AppActivity.class));
            }
        });

        listViewPostsFree.setOnItemClickListener((parent, view, position, id) -> {
            Post selectedPost = postListFree.get(position);
            incrementViewCountAndOpenDetail(selectedPost);
        });
        buttonSearch.setOnClickListener(v -> performSearch());
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getParentFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment writePostFragment = getParentFragmentManager().findFragmentByTag("FreeWritePostFragment");
            Fragment postDetailFragment = getParentFragmentManager().findFragmentByTag("PostDetail2Fragment");

            if (writePostFragment == null && postDetailFragment == null) {
                listViewPostsFree.setVisibility(View.VISIBLE);
                buttonNewPostFree.setVisibility(View.VISIBLE);
                fragmentContainerFree.setVisibility(View.GONE);

                bottomActionLayout.setVisibility(View.VISIBLE);
                editTextSearch.setVisibility(View.VISIBLE);
                spinnerSearchType.setVisibility(View.VISIBLE);
                buttonSearch.setVisibility(View.VISIBLE);
            }
        });
    }

    private void performSearch() {
        String searchType = spinnerSearchType.getSelectedItem().toString();
        String keyword = editTextSearch.getText().toString().trim();

        if (keyword.isEmpty()) {
            Toast.makeText(getContext(), "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Post> filteredList = new ArrayList<>();
        for (Post post : postListFree) {
            if (post == null) continue;
            switch (searchType) {
                case "제목":
                    if (post.getTitle() != null && post.getTitle().contains(keyword)) {
                        filteredList.add(post);
                    }
                    break;
                case "내용":
                    if (post.getContent() != null && post.getContent().contains(keyword)) {
                        filteredList.add(post);
                    }
                    break;
                case "작성자":
                    if (post.getAuthor() != null && post.getAuthor().contains(keyword)) {
                        filteredList.add(post);
                    }
                    break;
            }
        }

        postListAdapterFree = new PostListAdapter(getActivity(), R.layout.post_list_item, filteredList);
        listViewPostsFree.setAdapter(postListAdapterFree);
    }



    private void openFreeWritePostFragment() {
        FreeWritePostFragment fragment = new FreeWritePostFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, "FreeWritePostFragment");
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();

        listViewPostsFree.setVisibility(View.GONE);
        buttonNewPostFree.setVisibility(View.GONE);
        fragmentContainerFree.setVisibility(View.VISIBLE);
        bottomActionLayout.setVisibility(View.GONE);
        editTextSearch.setVisibility(View.GONE);
        spinnerSearchType.setVisibility(View.GONE);
        buttonSearch.setVisibility(View.GONE);
    }

    private void incrementViewCountAndOpenDetail(Post post) {
        DatabaseReference postRef = databaseReferenceFree.child(post.getPostId());
        postRef.child("viewCount").setValue(post.getViewCount() + 1)
                .addOnSuccessListener(aVoid -> openPostDetail2Fragment(post))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "조회수 증가 실패", Toast.LENGTH_SHORT).show());
    }

    private void openPostDetail2Fragment(Post post) {
        PostDetail2Fragment fragment = new PostDetail2Fragment();
        Bundle bundle = new Bundle();
        bundle.putString("postId", post.getPostId());
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, "PostDetail2Fragment");
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();

        listViewPostsFree.setVisibility(View.GONE);
        buttonNewPostFree.setVisibility(View.GONE);
        fragmentContainerFree.setVisibility(View.VISIBLE);

        bottomActionLayout.setVisibility(View.GONE);
        editTextSearch.setVisibility(View.GONE);
        spinnerSearchType.setVisibility(View.GONE);
        buttonSearch.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        listViewPostsFree.setVisibility(View.VISIBLE);
        buttonNewPostFree.setVisibility(View.VISIBLE);
        fragmentContainerFree.setVisibility(View.GONE);

        bottomActionLayout.setVisibility(View.VISIBLE);
        editTextSearch.setVisibility(View.VISIBLE);
        spinnerSearchType.setVisibility(View.VISIBLE);
        buttonSearch.setVisibility(View.VISIBLE);
        retrievePosts();
    }

    private void initializeUI() {
        postListFree = new ArrayList<>();
        postListAdapterFree = new PostListAdapter(getActivity(), R.layout.post_list_item, postListFree);
        listViewPostsFree.setAdapter(postListAdapterFree);

        listViewPostsFree.setVisibility(View.VISIBLE);
        buttonNewPostFree.setVisibility(View.VISIBLE);
        fragmentContainerFree.setVisibility(View.GONE);
        bottomActionLayout.setVisibility(View.VISIBLE);
        editTextSearch.setVisibility(View.VISIBLE);
        spinnerSearchType.setVisibility(View.VISIBLE);
        buttonSearch.setVisibility(View.VISIBLE);
    }

    private void retrievePosts() {
        if (postListFree == null) {
            postListFree = new ArrayList<>();
        }
        if (postListAdapterFree == null) {
            postListAdapterFree = new PostListAdapter(getActivity(), R.layout.post_list_item, postListFree);
            listViewPostsFree.setAdapter(postListAdapterFree);
        }

        postListFree.clear();
        postListAdapterFree.notifyDataSetChanged();

        postsListenerFree = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postListFree.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        postListFree.add(post);
                    }
                }
                postListAdapterFree.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        };

        databaseReferenceFree.addValueEventListener(postsListenerFree);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (postsListenerFree != null) {
            databaseReferenceFree.removeEventListener(postsListenerFree);
        }
    }
}
