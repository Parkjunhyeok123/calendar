package com.company.calendar.ui.home;

public class Holiday {
    private String date; // 형식: yyyyMMdd
    private String name;

    public Holiday(String date, String name) {
        this.date = date;
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }
}

