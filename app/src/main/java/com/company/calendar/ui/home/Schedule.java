package com.company.calendar.ui.home;

public class Schedule {
    private String title;
    private String description;
    private long startTimeMillis; // 시작 시간 (timestamp)
    private long endTimeMillis;   // 종료 시간 (timestamp)

    public Schedule() {}

    public Schedule(String title, String description, long startTimeMillis, long endTimeMillis) {
        this.title = title;
        this.description = description;
        this.startTimeMillis = startTimeMillis;
        this.endTimeMillis = endTimeMillis;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getStartTimeMillis() { return startTimeMillis; }
    public long getEndTimeMillis() { return endTimeMillis; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStartTimeMillis(long startTimeMillis) { this.startTimeMillis = startTimeMillis; }
    public void setEndTimeMillis(long endTimeMillis) { this.endTimeMillis = endTimeMillis; }
}
