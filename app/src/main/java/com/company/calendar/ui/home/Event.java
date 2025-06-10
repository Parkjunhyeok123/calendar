    package com.company.calendar.ui.home;

    import com.google.firebase.Timestamp;

    import java.util.Date;

    public class Event {
        private String userId;
        private boolean shared;
        private String title;
        private String date;

        private String time;
        private Timestamp timestamp;  // Timestamp로 변경

        public Event() {
        }

        // Constructor, Getter, Setter 추가
        public Event(String title, Timestamp timestamp, boolean shared, String userId,String time) {
            this.title = title;
            this.timestamp = timestamp;
            this.date = date;
            this.shared = shared;
            this.time = time;
            this.userId = userId;

        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public boolean isShared() {
            return shared;
        }

        public String getTitle() {
            return title;
        }
        public String getDate() {
            return date;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
        }
    }
