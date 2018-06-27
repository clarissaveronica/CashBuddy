package com.example.asus.cashbuddy.Model;

import java.io.Serializable;

public class Deals implements Serializable {

    private String title;
    private String picUrl;
    private String desc;
    private String dealEnd;
    private String merchant;

    public Deals() {}

    public Deals(String title,  String picUrl, String desc, String dealEnd, String merchant) {
        this.title = title;
        this.picUrl = picUrl;
        this.desc = desc;
        this.dealEnd = dealEnd;
        this.merchant = merchant;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setDealEnd(String dealEnd){this.dealEnd = dealEnd;}

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getTitle() {
        return title;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public String getDesc(){return desc;}

    public String getMerchant(){return merchant;}

    public String getDealEnd() {
        return dealEnd;
    }
}
