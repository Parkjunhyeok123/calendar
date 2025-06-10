package com.company.calendar.ui.login;
public class UserAccount {
    public UserAccount() {}
    private String id;
    private String name;
    private String number;
    private String team;

    private String uid;

    private int avatarId;
    private String role;
    private String status;

    public UserAccount(String id, String name, String number,int avatarId, String team, String uid, String status) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.avatarId= avatarId;
        this.team = team;
        this.uid = uid;
        this.status = status;

    }
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String getStatus(){
        return status;
    }


    public void setUid(String uid){
        this.uid = uid;
    }

    public String getUid(){
        return uid;
    }


    public void setTeam(String team){
        this.team = team;
    }
    public String getTeam(){
        return team;
    }


    public int getAvatarId() {
        return avatarId;
    }


    public void setAvatarId(int avatarId) {
        this.avatarId = avatarId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }


}