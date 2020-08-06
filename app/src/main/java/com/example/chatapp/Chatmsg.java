package com.example.chatapp;

public class Chatmsg {
    String Uid,Type,Message,Time,Sender,Receiver,Name;
    public Chatmsg(String Uid,String Type,String Message,String Time,String Sender,String Receiver,String Name){
        this.Uid=Uid;
        this.Type=Type;
        this.Message=Message;
        this.Time=Time;
        this.Sender=Sender;
        this.Receiver=Receiver;
        this.Name=Name;
    }
    public Chatmsg(){

    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getSender() {
        return Sender;
    }

    public void setSender(String sender) {
        Sender = sender;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getReceiver() {
        return Receiver;
    }

    public void setReceiver(String receiver) {
        Receiver = receiver;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }
}
