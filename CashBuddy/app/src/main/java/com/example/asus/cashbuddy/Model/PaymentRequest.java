package com.example.asus.cashbuddy.Model;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class PaymentRequest implements Serializable {

    //Status : 0 (new), 1 (accepted), 2 (rejected)

    private String receiverRequest, senderRequest;
    private int amount;
    private long requestdate;
    private int requeststatus;
    private String from;
    private String type;

    public PaymentRequest() {}

    public PaymentRequest(@NonNull String receiverRequest, String senderRequest, int amount, String from, String type) {
        this.senderRequest = senderRequest;
        this.receiverRequest = receiverRequest;
        this.amount = amount;
        this.from = from;
        this.type = type;
        this.requestdate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();
        requeststatus=0;
    }


    public String getSenderRequest() {
        return this.senderRequest;
    }

    public void setSenderRequest(String senderRequest) {
        this.senderRequest = senderRequest;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getReceiverRequest() {
        return receiverRequest;
    }

    public void setReceiverRequest(String receiverRequest) {
        this.receiverRequest = receiverRequest;
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
