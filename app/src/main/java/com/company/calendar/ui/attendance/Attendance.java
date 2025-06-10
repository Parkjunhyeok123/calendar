package com.company.calendar.ui.attendance;

public class Attendance {
    public String day;
    public String status;
    public String time;  // 출퇴근 시간 또는 표시할 문자열

    public Attendance(String day, String status, String time) {
        this.day = day;
        this.status = status;
        this.time = time;
    }
}
