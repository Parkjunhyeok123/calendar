package com.company.calendar.ui.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Post implements Parcelable {
    private String postId;
    private String title;
    private String content;
    private String author;
    private long timestamp;
    private String userId;
    private int viewCount; // 조회수 필드
    private String boardType; // 게시판 종류 필드
    private String stationName; // 충전소 리뷰 관련 필드
    private String reviewId;    // 충전소 리뷰 ID 필드

    public Post() {
        // 기본 생성자 필요 (Firebase용)
    }

    public void ensureBoardType(String boardType) {
        if (this.boardType == null || this.boardType.isEmpty()) {
            this.boardType = boardType;
        }
    }

    public Post(String postId, String title, String content, String author, long timestamp, String userId, int viewCount, String boardType, String stationName, String reviewId) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.author = author;
        this.timestamp = timestamp;
        this.userId = userId;
        this.viewCount = viewCount;
        this.boardType = boardType;
        this.stationName = stationName;
        this.reviewId = reviewId;
    }

    // Parcelable 구현
    protected Post(Parcel in) {
        postId = in.readString();
        title = in.readString();
        content = in.readString();
        author = in.readString();
        timestamp = in.readLong();
        userId = in.readString();
        viewCount = in.readInt();
        boardType = in.readString();
        stationName = in.readString();
        reviewId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(postId);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(author);
        dest.writeLong(timestamp);
        dest.writeString(userId);
        dest.writeInt(viewCount);
        dest.writeString(boardType);
        dest.writeString(stationName);
        dest.writeString(reviewId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    // Getters and setters
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public String getBoardType() {
        return boardType;
    }

    public void setBoardType(String boardType) {
        this.boardType = boardType;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }
}
