package com.company.calendar.ui.community;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CommunityViewModel extends ViewModel {

    private MutableLiveData<Integer> currentTab = new MutableLiveData<>(0);

    public LiveData<Integer> getCurrentTab() {
        return currentTab;
    }

    public void setCurrentTab(int tabPosition) {
        currentTab.setValue(tabPosition);
    }
}