package com.example.asus.cashbuddy.Model;

/**
 * Created by Jessica on 1/20/2018.
 */

public class User {

    private String name;
    private String profilePictureUrl;
    private String device_token;
    private String email;
    private int balance;
    private String phoneNumber;
    private String password;

    public User() {}

    public User(String name,  String phoneNumber, String email, String password, String profilePictureUrl, String device_token, int balance) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profilePictureUrl = profilePictureUrl;
        this.device_token = device_token;
        this.balance = balance;
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String userPhone) {
        this.email = email;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public void setBalance(int balance){this.balance = balance;}

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public void setPassword(String password){this.password = password;}

    public void setPhoneNumber(String phoneNumber){this.phoneNumber = phoneNumber;}

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber(){return phoneNumber;}

    public String getPassword(){return password;}

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public String getDevice_token() {
        return device_token;
    }

    public int getBalance(){return balance;}
}
