package com.example.asus.cashbuddy;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.asus.cashbuddy.Activity.User.MainActivity;
import com.example.asus.cashbuddy.Activity.User.UserScanActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PaymentRequestTest {

    @Rule
    public IntentsTestRule<MainActivity> paymentRequestIntentsTestRule = new IntentsTestRule<>(MainActivity.class);

    private MainActivity paymentRequestActivity = paymentRequestIntentsTestRule.getActivity();

    //Invalid inputs
    @Test
    public void testInvalidInputs() {

    }

    //Wrong security code when sending payment request
    @Test
    public void testWrongSecurityCode1() {

    }

    //Payment request is successfully sent
    @Test
    public void testSuccessfulSendPaymentReq() {

    }

    //Not enough balance
    @Test
    public void testInsufficientFunds() {

    }

    //Wrong security code when confirming payment request
    @Test
    public void testWrongSecurityCode2() {

    }

    //Reject payment request
    @Test
    public void testRejectPaymentRequest() {

    }

    //Accept payment request
    @Test
    public void testAcceptPaymentRequest() {

    }
}