package com.company.calendar.ui.community;

import android.app.AlertDialog;
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

public class CommunityThirdFragment extends Fragment {

    private Button buttonNewPostNotice;
    private ListView listViewPostsNotice;
    private FrameLayout fragmentContainerNotice;
    private FirebaseAuth firebaseAuthNotice;
    private FirebaseUser currentUserNotice;
    private DatabaseReference databaseReferenceNotice;
    private DatabaseReference userReference;
    private PostListAdapter postListAdapterNotice;
    private List<Post> postListNotice;
    private ValueEventListener postsListenerNotice;
    private LinearLayout bottomActionLayout;
    private Spinner spinnerSearchTypeNotice;
    private EditText editTextSearchNotice;
    private ImageButton buttonSearchNotice;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_community_third, container, false);

        // UI 요소 초기화
        buttonNewPostNotice = root.findViewById(R.id.buttonNewPostNotice);
        listViewPostsNotice = root.findViewById(R.id.listViewPostsNotice);
        fragmentContainerNotice = root.findViewById(R.id.fragment_container_notice);

        bottomActionLayout = root.findViewById(R.id.bottom_action_layout);

        spinnerSearchTypeNotice = root.findViewById(R.id.spinnerSearchTypeNotice);
        editTextSearchNotice = root.findViewById(R.id.editTextSearchNotice);
        buttonSearchNotice = root.findViewById(R.id.buttonSearchNotice);

        buttonSearchNotice.setOnClickListener(v -> {
            String searchType = spinnerSearchTypeNotice.getSelectedItem().toString();
            String keyword = editTextSearchNotice.getText().toString().trim();
            performSearch(searchType, keyword);
        });

        firebaseAuthNotice = FirebaseAuth.getInstance();
        currentUserNotice = firebaseAuthNotice.getCurrentUser();
        databaseReferenceNotice = FirebaseDatabase.getInstance().getReference()
                .child("board")
                .child("general_boards")
                .child("board_id_3")
                .child("posts");

        // 관리자 여부 확인 후 버튼 설정
        initializeUI();

        checkIfAdmin();

        // 게시글 클릭 이벤트 설정
        listViewPostsNotice.setOnItemClickListener((parent, view, position, id) -> {
            Post selectedPost = postListNotice.get(position);
            if (selectedPost != null) {
                incrementViewCountAndOpenDetail(selectedPost);
            }
        });

        // Firebase에서 공지사항 실시간 감지
        listenForNewPosts();

        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getParentFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment writePostFragment = getParentFragmentManager().findFragmentByTag("FreeWritePostFragment");
            Fragment postDetailFragment = getParentFragmentManager().findFragmentByTag("PostDetail3Fragment");

            if (writePostFragment == null && postDetailFragment == null) {
                listViewPostsNotice.setVisibility(View.VISIBLE);
                buttonNewPostNotice.setVisibility(View.VISIBLE);
                fragmentContainerNotice.setVisibility(View.GONE);

                bottomActionLayout.setVisibility(View.VISIBLE);
                editTextSearchNotice.setVisibility(View.VISIBLE);
                spinnerSearchTypeNotice.setVisibility(View.VISIBLE);
                buttonSearchNotice.setVisibility(View.VISIBLE);

                checkIfAdmin();
            }
        });
    }

    // Firebase에서 새 게시글 실시간으로 감지
    private void listenForNewPosts() {
        postsListenerNotice = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postListNotice.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        postListNotice.add(post);
                    }
                }
                postListAdapterNotice.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        };

        databaseReferenceNotice.addValueEventListener(postsListenerNotice);
    }

    // 공지사항이 새로 올라왔을 때 알림 표시
    private void showNotification(Post post) {
        // 토스트 알림 예시
        Toast.makeText(getContext(), "새로운 공지사항: " + post.getTitle(), Toast.LENGTH_SHORT).show();

        // AlertDialog로 알림 표시
        new AlertDialog.Builder(getContext())
                .setTitle("새로운 공지사항")
                .setMessage(post.getTitle())
                .setPositiveButton("확인", null)
                .show();
    }

    // 관리자 확인 메서드
    private void checkIfAdmin() {
        if (currentUserNotice != null) {
            String uid = currentUserNotice.getUid();
            userReference = FirebaseDatabase.getInstance().getReference()
                    .child("UserAccount")
                    .child(uid);

            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String role = dataSnapshot.child("role").getValue(String.class);
                        if ("admin".equals(role)) {
                            buttonNewPostNotice.setVisibility(View.VISIBLE);
                            buttonNewPostNotice.setOnClickListener(v -> openNoticeWritePostFragment());
                        } else {
                            buttonNewPostNotice.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "권한 확인 실패", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            buttonNewPostNotice.setVisibility(View.GONE);
        }
    }

    private void performSearch(String searchType, String keyword) {
        if (keyword.isEmpty()) {
            // 검색어가 없으면 전체 게시글 불러오기
            listenForNewPosts();
            return;
        }

        // 전체 데이터를 한번 읽고, 클라이언트에서 필터링 (Firebase Realtime Database 특성상 복잡한 쿼리 제한)
        databaseReferenceNotice.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postListNotice.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        boolean matches = false;
                        switch (searchType) {
                            case "제목":
                                matches = post.getTitle() != null && post.getTitle().toLowerCase().contains(keyword.toLowerCase());
                                break;
                            case "내용":
                                matches = post.getContent() != null && post.getContent().toLowerCase().contains(keyword.toLowerCase());
                                break;
                            case "작성자":
                                matches = post.getAuthor() != null && post.getAuthor().toLowerCase().contains(keyword.toLowerCase());
                                break;
                            default:
                                // 기본은 제목 검색
                                matches = post.getTitle() != null && post.getTitle().toLowerCase().contains(keyword.toLowerCase());
                        }

                        if (matches) {
                            postListNotice.add(post);
                        }
                    }
                }

                postListAdapterNotice.notifyDataSetChanged();

                if (postListNotice.isEmpty()) {
                    Toast.makeText(getContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 게시글 작성 화면 열기 메서드
    private void openNoticeWritePostFragment() {
        NoticeWritePostFragment noticeWritePostFragment = new NoticeWritePostFragment();

        // 프래그먼트 전환 트랜잭션 시작
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_notice, noticeWritePostFragment, "NoticeWritePostFragment");
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();

        listViewPostsNotice.setVisibility(View.GONE);
        buttonNewPostNotice.setVisibility(View.GONE);
        fragmentContainerNotice.setVisibility(View.VISIBLE);
        bottomActionLayout.setVisibility(View.GONE);
        editTextSearchNotice.setVisibility(View.GONE);
        spinnerSearchTypeNotice.setVisibility(View.GONE);
        buttonSearchNotice.setVisibility(View.GONE);
    }

    // 게시글 조회수 증가 및 상세보기로 이동
    private void incrementViewCountAndOpenDetail(Post post) {
        if (post == null || post.getPostId() == null) {
            Toast.makeText(getContext(), "유효하지 않은 게시글입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference postRef = databaseReferenceNotice.child(post.getPostId());
        postRef.child("viewCount").setValue(post.getViewCount() + 1)
                .addOnSuccessListener(aVoid -> {
                    // 조회수 증가 후, 게시글 상세보기 열기
                    openPostDetail3Fragment(post);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "조회수 증가 실패", Toast.LENGTH_SHORT).show());
    }

    // 게시글 상세보기 프래그먼트 열기
    private void openPostDetail3Fragment(Post selectedPost) {
        PostDetail3Fragment postDetail3Fragment = new PostDetail3Fragment();
        Bundle bundle = new Bundle();
        bundle.putString("postId", selectedPost.getPostId());
        postDetail3Fragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_notice, postDetail3Fragment, "PostDetail3Fragment");
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();

        listViewPostsNotice.setVisibility(View.GONE);
        buttonNewPostNotice.setVisibility(View.GONE);
        fragmentContainerNotice.setVisibility(View.VISIBLE);

        bottomActionLayout.setVisibility(View.GONE);
        editTextSearchNotice.setVisibility(View.GONE);
        spinnerSearchTypeNotice.setVisibility(View.GONE);
        buttonSearchNotice.setVisibility(View.GONE);

    }

    @Override
    public void onResume() {
        super.onResume();
        initializeUI();
    }

    private void initializeUI() {
        // postListNotice와 adapter 초기화
        postListNotice = new ArrayList<>();
        postListAdapterNotice = new PostListAdapter(getActivity(), R.layout.post_list_item, postListNotice);
        listViewPostsNotice.setAdapter(postListAdapterNotice);

        listViewPostsNotice.setVisibility(View.VISIBLE);
        fragmentContainerNotice.setVisibility(View.GONE);
        bottomActionLayout.setVisibility(View.VISIBLE);
        editTextSearchNotice.setVisibility(View.VISIBLE);
        spinnerSearchTypeNotice.setVisibility(View.VISIBLE);
        buttonSearchNotice.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (postsListenerNotice != null) {
            databaseReferenceNotice.removeEventListener(postsListenerNotice);
            postsListenerNotice = null;
        }
    }
}
