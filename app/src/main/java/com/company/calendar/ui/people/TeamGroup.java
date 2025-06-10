package com.company.calendar.ui.people;

import com.company.calendar.ui.login.UserAccount;

import java.util.List;

public class TeamGroup {
    private String teamName;
    private List<UserAccount> members;

    public TeamGroup(String teamName, List<UserAccount> members) {
        this.teamName = teamName;

        this.members = members;
    }

    public String getTeamName() {
        return teamName;
    }

    public List<UserAccount> getMembers() {
        return members;
    }
}

