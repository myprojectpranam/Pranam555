package notinuse;

import com.example.pranam555.ui.NewGroupParticipantsAddActivity;

import java.util.List;

public class Contactsusers {

    //Spelling must be same as your database
    public String name,status,image,uid,phoneNumber;


    public Contactsusers(){

    }

    public Contactsusers(String name, String status, String image, String uid,String phoneNumber) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.uid = uid;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
