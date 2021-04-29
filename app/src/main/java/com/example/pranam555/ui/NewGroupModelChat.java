package com.example.pranam555.ui;

public class NewGroupModelChat {

    String message,sender,timeStamp,type,to;
    String isseen;

    public NewGroupModelChat(){


    }

    public NewGroupModelChat(String message, String sender, String timeStamp, String type,String to,String isseen) {
        this.message = message;
        this.sender = sender;
        this.timeStamp = timeStamp;
        this.type = type;
        this.isseen = isseen;
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String isIsseen() {
        return isseen;
    }

    public void setIsseen(String isseen) {
        this.isseen = isseen;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
