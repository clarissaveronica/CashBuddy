package com.example.asus.cashbuddy.Model;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class History {

    private String type, desc;
    private int amount;
    private long date;

    public History() {}
    public History(@NonNull String type, String desc, int amount) {
        this.desc = desc;
        this.type = type;
        this.amount = amount;
        this.date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getDateString(long x) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        return sdf.format(x);
    }
}
