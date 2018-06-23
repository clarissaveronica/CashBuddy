package com.example.asus.cashbuddy.Model;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Transfer implements Serializable {

    private String name;
    // Store Identity Variable
    private String receiver;
    // User Identity Variable
    private String sender;
    // Total price
    private int totalTransfer;
    // Purchase Date
    private long transferDate;

    // Empty Constructor
    public Transfer() {

    }

    public Transfer(@NonNull String receiver, @NonNull String sender, @NonNull int totalTransfer) {
        this.receiver = receiver;
        this.sender = sender;
        this.totalTransfer = totalTransfer;
        this.transferDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setTransferDate(long transferDate) {
        this.transferDate = transferDate;
    }

    public void setTotalTransfer(int totalTransfer) {
        this.totalTransfer = totalTransfer;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getSender() {
        return sender;
    }

    public long getTransferDate() {
        return transferDate;
    }

    public int getTotalTransfer() {
        return totalTransfer;
    }

    public String getName(){
        return this.name;
    }

    public static String getPurchasedDateString(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        return sdf.format(time);
    }
}