package com.company.calendar.ui.mypage;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.company.calendar.ui.mail.MailFragment;


public class MypagePagerAdapter extends FragmentStatePagerAdapter {

    public MypagePagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        // position에 따라 다른 Fragment 반환
        switch (position) {
            case 0:
                return new MypageFirstFragment();
            case 1:
                return new MypageFourthFragment();
            case 2:
                return new MailFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        // 전체 Fragment 개수 반환 (여기서는 4개)
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // TabLayout에 표시될 탭 이름 반환
        switch (position) {
            case 0:
                return "내 정보";
            case 1:
                return "내 작성글 보기";
            case 2:
                return "메일";

            default:
                return null;
        }
    }
}