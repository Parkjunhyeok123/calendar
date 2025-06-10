package com.company.calendar.ui.event;

import java.util.Date;

public class WeekDay {
    public String dayLabel;  // "월", "화", ...
    public Date date;
    public boolean isSelected;

    public WeekDay(String dayLabel, Date date, boolean isSelected) {
        this.dayLabel = dayLabel;
        this.date = date;
        this.isSelected = isSelected;
    }
}
