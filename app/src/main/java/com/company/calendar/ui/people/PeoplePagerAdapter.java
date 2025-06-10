package com.company.calendar.ui.people;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class PeoplePagerAdapter extends FragmentStatePagerAdapter {

    public PeoplePagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new UserListFragment();
            case 1:
             return new MyFriendFragment();
            case 2:
             return new FriendFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "사원들";
            case 1:
                return "친구들";
            case 2:
                return "친구 신청";
            default:
                return null;
        }
    }
}

