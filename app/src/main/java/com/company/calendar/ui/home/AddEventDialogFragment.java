package com.company.calendar.ui.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

public class AddEventDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("이벤트 추가")
                .setMessage("이벤트 제목을 입력하세요")
                .setPositiveButton("확인", (dialog, id) -> {
                    // 여기에 입력된 이벤트를 처리하는 코드 추가
                    // 예를 들어 Firebase Firestore에 저장
                })
                .setNegativeButton("취소", (dialog, id) -> {
                    // 취소 처리
                });
        return builder.create();
    }
}
