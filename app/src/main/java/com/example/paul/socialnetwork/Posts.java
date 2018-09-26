package com.example.paul.socialnetwork;

public class Posts {
    public String uid , profileimage ,postimage , fullname , time ,date , description;

    public Posts(){

    }


    public Posts(String uid, String profileimage, String postimage, String fullname, String time, String date, String description) {
        this.uid = uid;
        this.profileimage = profileimage;
        this.postimage = postimage;
        this.fullname = fullname;
        this.time = time;
        this.date = date;
        this.description = description;



    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
