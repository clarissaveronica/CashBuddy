package com.example.asus.cashbuddy;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.TextView;

import com.example.asus.cashbuddy.Activity.User.MainActivity;
import com.example.asus.cashbuddy.Activity.User.UserScanActivity;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class PaymentRequestTest {

    @Rule
    public IntentsTestRule<MainActivity> paymentRequestIntentsTestRule = new IntentsTestRule<>(MainActivity.class);

    private MainActivity paymentRequestActivity = paymentRequestIntentsTestRule.getActivity();

    //Invalid inputs + Wrong security code when sending payment request
    @Test
    public void testFailedSendPaymentReq() {
        onView(withId(R.id.requestPaymentButton)).perform(click());

        //Test wrong inputs
        onView(withId(R.id.amountTransfer)).perform(typeText("5000"));
        onView(withId(R.id.phoneNum)).perform(scrollTo(), typeText("123456"));
        onView(withId(R.id.submitButton)).perform(scrollTo(), click());
        onView(withId(R.id.amountTransfer)).check(matches(hasErrorText("Minimal amount requested is Rp 10.000")));
        onView(withId(R.id.phoneNum)).check(matches(hasErrorText("Invalid phone number")));
        onView(withId(R.id.type)).check(matches(hasErrorText("Type of payment is required")));

        //Test wrong security code
        onView(withId(R.id.amountTransfer)).perform(scrollTo(), replaceText("10000"));
        onView(withId(R.id.phoneNum)).perform(scrollTo(), replaceText("0888888888"));
        onView(withId(R.id.type)).perform(scrollTo(), typeText("Pizza"));
        onView(withId(R.id.submitButton)).perform(scrollTo(), click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.pinEntry)).perform(typeText("555555"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Wrong security code")).inRoot(new ToastTest()).check(matches(isDisplayed()));
    }

    //Payment request is successfully sent
    @Test
    public void testSuccessfulSendPaymentReq() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        onView(withId(R.id.requestPaymentButton)).perform(click());

        //Input the necessary data
        onView(withId(R.id.amountTransfer)).perform(typeText("10000"));
        onView(withId(R.id.phoneNum)).perform(scrollTo(), typeText("0899999999"));
        onView(withId(R.id.type)).perform(scrollTo(), typeText("Pizza"));
        onView(withId(R.id.submitButton)).perform(scrollTo(), click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        String date = sdf.format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());

        //Check if request is sent or not
        onView(withId(R.id.requestPaymentButton)).perform(click());
        onView(withText("Sent")).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) { }
        onView(withText(date)).check(matches(isDisplayed()));
        Espresso.pressBack();
        loginUser("0899999999");
        onView(withId(R.id.requestPaymentButton)).perform(click());
        onView(withText("Received")).perform(click());
        onView(withText(date)).check(matches(isDisplayed()));
        Espresso.pressBack();
        loginUser("0866666666");
    }

    //Not enough balance
    @Test
    public void testInsufficientFunds() {
        loginUser("0899999999");
        onView(withId(R.id.requestPaymentButton)).perform(click());
        onView(withText("Received")).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.received_request_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.accept_request)).perform(click());
        onView(withText("Insufficient funds. Please top up to proceed.")).check(matches(isDisplayed()));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        Espresso.pressBack();
        Espresso.pressBack();
        loginUser("0866666666");
    }

    //Wrong security code when confirming payment request
    @Test
    public void testWrongSecurityCode() {
        loginUser("0888888888");
        onView(withId(R.id.requestPaymentButton)).perform(click());
        onView(withText("Received")).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.received_request_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.accept_request)).perform(scrollTo(), click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.pinEntry)).perform(typeText("555555"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Wrong security code")).inRoot(new ToastTest()).check(matches(isDisplayed()));
    }

    //Reject payment request
    @Test
    public void testRejectPaymentRequest() {
        loginUser("0888888888");
        onView(withId(R.id.requestPaymentButton)).perform(click());
        onView(withText("Received")).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.received_request_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        String date = getText(withId(R.id.date));
        onView(withId(R.id.decline_request)).perform(scrollTo(), click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Request declined")).inRoot(new ToastTest()).check(matches(isDisplayed()));
        Espresso.pressBack();
        loginUser("0866666666");
        onView(withId(R.id.requestPaymentButton)).perform(click());
        onView(withText("Sent")).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withText(date)).perform(click());
        onView(withText("Declined")).check(matches(isDisplayed()));
    }

    //Accept payment request
    @Test
    public void testAcceptPaymentRequest() {
        //Check balance on both users
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) { }
        int senderBalance = Integer.parseInt(getText(withId(R.id.balance)).replace(".", "").replace("Rp", ""));
        loginUser("0888888888");
        int receiverBalance = Integer.parseInt(getText(withId(R.id.balance)).replace(".", "").replace("Rp", ""));

        //Accept request
        onView(withId(R.id.requestPaymentButton)).perform(click());
        onView(withText("Received")).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.received_request_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        String date = getText(withId(R.id.date));
        onView(withId(R.id.accept_request)).perform(scrollTo(), click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Request accepted")).inRoot(new ToastTest()).check(matches(isDisplayed()));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) { }

        //Check new balance and request status
        Espresso.pressBack();
        String newReceiverBalance = changeToRupiahFormat(receiverBalance - 10000);
        onView(withId(R.id.balance)).check(matches(withText(newReceiverBalance)));
        loginUser("0866666666");
        String newSenderBalance = changeToRupiahFormat(senderBalance + 10000);
        onView(withId(R.id.balance)).check(matches(withText(newSenderBalance)));
        onView(withId(R.id.requestPaymentButton)).perform(click());
        onView(withText("Sent")).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withText(date)).perform(click());
        onView(withText("Accepted")).check(matches(isDisplayed()));
    }

    public void loginUser(String phoneNum){
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.signOutButton)).perform(click());
        onView(withText("Yes")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.loginUsername)).perform(typeText(phoneNum));
        onView(withId(R.id.loginUsername)).perform(closeSoftKeyboard());
        onView(withId(R.id.signInButton)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
    }

    String getText(final Matcher<View> matcher) {
        final String[] stringHolder = { null };
        onView(matcher).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TextView.class);
            }

            @Override
            public String getDescription() {
                return "getting text from a TextView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TextView tv = (TextView)view; //Save, because of check in getConstraints()
                stringHolder[0] = tv.getText().toString();
            }
        });
        return stringHolder[0];
    }

    public String changeToRupiahFormat(int money){
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        String temp = formatRupiah.format((double)money);

        return temp;
    }
}