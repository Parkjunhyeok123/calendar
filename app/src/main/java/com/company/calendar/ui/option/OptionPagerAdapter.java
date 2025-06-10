package com.company.calendar.ui.option;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.company.calendar.ui.home.HomeFragment;

public class OptionPagerAdapter extends FragmentStatePagerAdapter {

    public OptionPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new OptionFirstFragment();
            case 1:
                return new HomeFragment();
            //case 2:
               // return new OptionThirdFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "간단 일정 보기";
            case 1:
                return "전체 일정";
            //case 2:
               // return "공지 사랑";
            default:
                return null;
        }
    }
}

