package com.company.calendar.ui.mypage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.company.calendar.databinding.FragmentMypageFirstBinding;
import com.company.calendar.ui.login.UserAccount;
import com.company.calendar.ui.mypage.MypageViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MypageFirstFragment extends Fragment {

    private FragmentMypageFirstBinding binding;
    private MypageViewModel myPageViewModel;

    private EditText editTextName, editTextNumber, editTextId, editTextTeam;
    private Spinner spinnerStatus;
    private Button buttonUpdate;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMypageFirstBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        myPageViewModel = new ViewModelProvider(requireActivity()).get(MypageViewModel.class);

        myPageViewModel.getCurrentTab().observe(getViewLifecycleOwner(), currentTab -> {
            if (currentTab == 0) {
                setupUserInfoUpdate();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupUserInfoUpdate() {
        editTextName = binding.editTextName;
        editTextNumber = binding.editTextNumber;
        editTextId = binding.editTextId;
        editTextTeam = binding.editTextTeam;
        spinnerStatus = binding.spinnerStatus;
        buttonUpdate = binding.buttonUpdate;

        // 상태 옵션 배열
        String[] statusOptions = {"회의중", "근무중", "휴무중", "출장중"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserAccount userAccount = snapshot.getValue(UserAccount.class);
                        if (userAccount != null) {
                            String name = userAccount.getName();
                            String number = userAccount.getNumber();
                            String id = userAccount.getId();
                            String team = userAccount.getTeam();
                            String status = userAccount.getStatus();

                            editTextName.setText(name);
                            editTextNumber.setText(number);
                            editTextId.setText(id);
                            editTextTeam.setText(team);

                            if (status != null) {
                                int spinnerPosition = adapter.getPosition(status);
                                if (spinnerPosition >= 0) {
                                    spinnerStatus.setSelection(spinnerPosition);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(requireContext(), "사용자 정보 로딩 실패", Toast.LENGTH_SHORT).show();
                }
            });
        }

        buttonUpdate.setOnClickListener(v -> updateUserInfo());
    }

    private void updateUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            String name = editTextName.getText().toString().trim();
            String number = editTextNumber.getText().toString().trim();
            String id = editTextId.getText().toString().trim();
            String team = editTextTeam.getText().toString().trim();
            String status = spinnerStatus.getSelectedItem().toString();

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(userId);
            userRef.child("name").setValue(name);
            userRef.child("number").setValue(number);
            userRef.child("id").setValue(id);
            userRef.child("team").setValue(team);
            userRef.child("status").setValue(status);

            Toast.makeText(requireContext(), "정보가 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
