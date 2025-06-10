package com.company.calendar.ui.approval;

public class LeaveRequest {
    private String documentId;
    private String userId;
    private String userName;
    private String startDate;
    private String endDate;
    private String leaveType;
    private String reason;
    private String status;

    public LeaveRequest() { }

    public LeaveRequest(String userId, String userName, String startDate, String endDate,
                        String leaveType, String reason, String status) {
        this.userId = userId;
        this.userName = userName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.leaveType = leaveType;
        this.reason = reason;
        this.status = status;
    }

    // Getters and setters...

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getLeaveType() { return leaveType; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public void setReason(String reason) { this.reason = reason; }
    public void setStatus(String status) { this.status = status; }
}
