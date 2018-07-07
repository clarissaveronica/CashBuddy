package com.example.asus.cashbuddy.Model;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class SplitBill implements Serializable {

    //Status : 0 (new), 1 (accepted), 2 (rejected), 3 (expired)

    private String receiver, sender;
    private int amount;
    private long requestdate;
    private int requeststatus;

    public SplitBill() {}

    public SplitBill(@NonNull String receiver, String sender, int amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.requestdate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();
        requeststatus=0;
    }

    public String getSender() {
        return this.sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public long getRequestdate() {
        return requestdate;
    }

    public void setRequestdate(long requestdate) {
        this.requestdate = requestdate;
    }

    public int getRequeststatus() {
        return requeststatus;
    }

    public void setRequeststatus(int requeststatus) {
        this.requeststatus = requeststatus;
    }

    public String getRequestDateString(long x) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        return sdf.format(x);
    }
}
