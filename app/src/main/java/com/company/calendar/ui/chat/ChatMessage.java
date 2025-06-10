package com.company.calendar.ui.chat;

// ChatMessage.java
public class ChatMessage {
    public String senderId;
    public String senderName; // 닉네임도 같이 표시
    public String text;
    public long timestamp;

    public ChatMessage() {}

    public ChatMessage(String senderId, String senderName, String text, long timestamp) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
    }
}
