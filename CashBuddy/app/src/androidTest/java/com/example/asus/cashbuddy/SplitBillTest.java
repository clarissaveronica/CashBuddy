package com.example.asus.cashbuddy;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.asus.cashbuddy.Activity.User.UserScanActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SplitBillTest {

    @Rule
    public IntentsTestRule<UserScanActivity> userScanActivityIntentsTestRule = new IntentsTestRule<>(UserScanActivity.class);

    private UserScanActivity scanActivity = userScanActivityIntentsTestRule.getActivity();

    //Invalid qr code
    @Test
    public void testInvalidQR() {

    }

    //Insufficient funds
    @Test
    public void testInsufficientFunds() {

    }

    //Incorrect security code
    @Test
    public void testWrongSecurityCode() {

    }

    //Successful payment
    @Test
    public void testSuccessfulPayment() {

    }
}