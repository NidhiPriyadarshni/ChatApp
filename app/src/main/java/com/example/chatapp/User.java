package com.example.chatapp;

public class User {
    String About,Name,Phone,Pic,Uid;
    public User(String About,String Name,String Phone,String Pic,String Uid){
        this.About=About;
        this.Name=Name;
        this.Phone=Phone;
        this.Pic=Pic;
        this.Uid=Uid;
    }
    public User(){

    }

    public String getAbout() {
        return About;
    }

    public String getName() {
        return Name;
    }

    public String getPhone() {
        return Phone;
    }

    public String getPic() {
        return Pic;
    }

    public String getUid() {
        return Uid;
    }

    public void setAbout(String about) {
        About = about;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public void setPic(String pic) {
        Pic = pic;
    }

    public void setUid(String uid) {
        Uid = uid;
    }
}
