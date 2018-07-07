package com.example.asus.cashbuddy.Model;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Transaction implements Serializable {

    private String sid;
    private String uid;
    private int totalPrice;
    private long purchaseDate;
    private int split;

    // Empty Constructor
    public Transaction() {

    }

    public Transaction(@NonNull String store_id, @NonNull String user_id, @NonNull int totalPrice) {
        this.sid = store_id;
        this.uid = user_id;
        this.totalPrice = totalPrice;
        this.purchaseDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();
        split = 0;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setPurchaseDate(long purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setSplit(int split){this.split = split;}

    public int getSplit(){return split;}

    public String getSid() {
        return sid;
    }

    public String getUid() {
        return uid;
    }

    public long getPurchaseDate() {
        return purchaseDate;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public static String getPurchasedDateString(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        return sdf.format(time);
    }
}