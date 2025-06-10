package com.company.calendar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.company.calendar.databinding.ActivitySubBinding;
import com.company.calendar.ui.login.UserAccount;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class subActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivitySubBinding binding;

    // 아바타 리소스 배열
    private int[] avatarIds = {
            R.drawable.avatar1,
            R.drawable.avatar2,
            R.drawable.avatar3,
            R.drawable.avatar4,
            R.drawable.avatar5
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_main, R.id.nav_mypage, R.id.nav_community, R.id.nav_option, R.id.nav_people)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View headerView = navigationView.getHeaderView(0);
        Button loginButton = headerView.findViewById(R.id.button_login);
        TextView titleTextView = headerView.findViewById(R.id.nav_header_title);
        TextView subtitleTextView = headerView.findViewById(R.id.nav_header_subtitle);
        ImageView profileImageView = headerView.findViewById(R.id.imageView);

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = fAuth.getCurrentUser();

        if (loginButton != null) {
            if (currentUser != null) {
                loginButton.setText("로그아웃");
                fetchUserData(currentUser.getUid(), titleTextView, subtitleTextView, profileImageView);
            } else {
                loginButton.setText("로그인");
                titleTextView.setText(getString(R.string.nav_header_title));
                subtitleTextView.setText(getString(R.string.nav_header_subtitle));
            }

            loginButton.setOnClickListener(v -> {
                FirebaseUser currentUser1 = fAuth.getCurrentUser();
                if (currentUser1 != null) {
                    fAuth.signOut();
                    loginButton.setText("로그인");
                    titleTextView.setText(getString(R.string.nav_header_title));
                    subtitleTextView.setText(getString(R.string.nav_header_subtitle));
                    profileImageView.setImageResource(R.drawable.user);
                } else {
                    startActivity(new Intent(subActivity.this, com.company.calendar.ui.login.AppActivity.class));
                }
            });
        }

        // 프로필 이미지 클릭 시 아바타 선택
        profileImageView.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivity(new Intent(subActivity.this, com.company.calendar.ui.login.AppActivity.class));
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(subActivity.this);
            builder.setTitle("아바타 선택");

            String[] avatarNames = {"아바타1", "아바타2", "아바타3", "아바타4", "아바타5"};
            builder.setItems(avatarNames, (dialog, which) -> {
                int selectedAvatar = avatarIds[which];
                profileImageView.setImageResource(selectedAvatar);
                updateUserAvatarInFirebase(selectedAvatar);
            });

            builder.setNegativeButton("취소", null);
            builder.show();
        });
    }

    private void updateUserAvatarInFirebase(int avatarId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(user.getUid());
            userRef.child("avatarId").setValue(avatarId);
        }
    }

    private void fetchUserData(String userId, TextView titleTextView, TextView subtitleTextView, ImageView profileImageView) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserAccount userAccount = snapshot.getValue(UserAccount.class);
                    if (userAccount != null) {
                        String displayName = userAccount.getName();
                        String email = userAccount.getId();
                        int avatarRes = userAccount.getAvatarId();

                        String welcomeText = String.format(getString(R.string.user_welcome_message), displayName);
                        runOnUiThread(() -> {
                            titleTextView.setText(welcomeText);
                            subtitleTextView.setText(email);
                            if (avatarRes != 0) {
                                profileImageView.setImageResource(avatarRes);
                            } else {
                                profileImageView.setImageResource(R.drawable.user);
                            }
                        });
                    }
                } else {
                    titleTextView.setText("게스트");
                    subtitleTextView.setText("로그인 해주세요");
                    profileImageView.setImageResource(R.drawable.user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("subActivity", "Error fetching user data: " + error.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
