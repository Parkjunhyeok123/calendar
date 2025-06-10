package com.company.calendar.ui.email;

public class EmailItem {
    public String subject;
    public String from;
    public String date;
    private String messageId;
    private String body;
    public EmailItem(String subject, String from, String date,String messageId, String body) {
        this.subject = subject;
        this.from = from;
        this.date = date;
        this.messageId = messageId;
        this.body = body;


    }
    // Getter 추가
    public String getSubject() { return subject; }
    public String getFrom() { return from; }
    public String getDate() { return date; }
    public String getMessageId() { return messageId; }
    public String getBody() { return body; }
}
