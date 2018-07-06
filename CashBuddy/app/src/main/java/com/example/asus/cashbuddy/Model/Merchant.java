package com.example.asus.cashbuddy.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ASUS on 2/12/2018.
 */

public class Merchant implements Parcelable {
    private String merchantName;
    private String merchantId;
    private String password;
    private String phoneNumber;
    private String location;
    private String email;
    private int balance;
    private String device_token;
    private int price;

    public Merchant() {}

    public Merchant(String merchantName, String location, String email, int balance) {
        this.email = email;
        this.merchantName = merchantName;
        this.location = location;
        this.balance = balance;
    }

    public Merchant(String merchantName, String phoneNumber, String password, String email, String location, String device_token, int balance) {
        this.merchantName = merchantName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.location = location;
        this.password = password;
        this.balance = balance;
        this.device_token = device_token;
        this.price = 0;
    }

    protected Merchant(Parcel in) {
        merchantName = in.readString();
        merchantId = in.readString();
        password=in.readString();
        phoneNumber = in.readString();
        location = in.readString();
        balance = in.readInt();
        email = in.readString();
        device_token = in.readString();
    }

    public static final Creator<Merchant> CREATOR = new Creator<Merchant>() {
        @Override
        public Merchant createFromParcel(Parcel in) {
            return new Merchant(in);
        }

        @Override
        public Merchant[] newArray(int size) {
            return new Merchant[size];
        }
    };

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {this.password = password;};

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setBalance(int balance){this.balance = balance;}

    public void setPrice(int price){this.price = price;}

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getEmail() {
        return email;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getLocation() {
        return location;
    }

    public String getPassword(){return password;}

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDevice_token() {
        return device_token;
    }

    public int getBalance(){return balance;}

    public int getPrice(){return price;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(merchantName);
        dest.writeString(merchantId);
        dest.writeString(phoneNumber);
        dest.writeString(password);
        dest.writeString(location);
        dest.writeString(email);
        dest.writeInt(balance);
        dest.writeString(device_token);
    }
}
