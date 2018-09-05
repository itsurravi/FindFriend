package com.ravisharma.findfriend.Models;

public class UserInfo {

    String name;
    String phone;
    String uid;


    public UserInfo() {
    }

    public UserInfo(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public UserInfo(String name, String phone, String uid) {
        this.name = name;
        this.phone = phone;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getUid() {
        return uid;
    }
}
