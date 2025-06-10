package com.company.calendar.ui.people;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;
import com.company.calendar.databinding.FragmentOptionBinding;
import com.google.android.material.tabs.TabLayout;

public class PeopleFragment extends Fragment {

    private FragmentOptionBinding binding;
    private PeopleViewModel peopleViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOptionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ViewPager viewPager = binding.viewPager;
        TabLayout tabLayout = binding.tabLayout;

        // ViewPager 어댑터 설정
        PeoplePagerAdapter pagerAdapter = new PeoplePagerAdapter(getChildFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(pagerAdapter);

        // TabLayout과 ViewPager 연결
        tabLayout.setupWithViewPager(viewPager);

        // ViewModel 초기화
        peopleViewModel = new ViewModelProvider(this).get(PeopleViewModel.class);

        // 탭 선택 이벤트 처리
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 선택된 탭의 위치를 ViewModel에 업데이트
                peopleViewModel.setCurrentTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 전달받은 selectedTab 인자 처리
        Bundle arguments = getArguments();
        int selectedTab = 0; // 기본값은 첫 번째 탭
        if (arguments != null) {
            selectedTab = arguments.getInt("selectedTab", 0);
        }
        viewPager.setCurrentItem(selectedTab);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
