package com.example.pranam555;

public class PhoneContactsModel {

    private String name,phoneNumber,photo,uid;

    public PhoneContactsModel(){

    }

    public PhoneContactsModel(String name, String phoneNumber, String photo,String uid) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.photo = photo;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
