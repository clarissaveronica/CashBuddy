package com.example.asus.cashbuddy.Testing;

import com.example.asus.cashbuddy.Activity.User.UserScanActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.junit.Test;

import static org.junit.Assert.*;

public class UserScanActivityTest {

    UserScanActivity scanActivity = new UserScanActivity();

    @Test
    public void changeToRupiahFormat() {
        String expected = "Rp10.000,00";
        String result = scanActivity.changeToRupiahFormat(10000);
        assertEquals("Convert successful", expected, result);
    }

    @Test
    public void hash() {
        String expected = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";
        String result = scanActivity.hash("123456");
        assertEquals("Hash successful", expected, result);
    }
}