package com.company.calendar.ui.community;

public class Comment {
    private String userId;
    private String content;
    private String commentId;
    private int likeCount;

    // 파라미터가 없는 기본 생성자
    public Comment() {
    }

    public Comment(String userId, String content) {
        this.userId = userId;
        this.content = content;
        this.likeCount = 0;  // 기본 좋아요 개수 설정
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
