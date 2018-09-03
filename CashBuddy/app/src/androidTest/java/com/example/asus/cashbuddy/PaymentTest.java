package com.example.asus.cashbuddy;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.TextView;

import com.example.asus.cashbuddy.Activity.User.MainActivity;

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
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class PaymentTest {

    @Rule
    public IntentsTestRule<MainActivity> userScanActivityIntentsTestRule = new IntentsTestRule<>(MainActivity.class);

    private MainActivity scanActivity = userScanActivityIntentsTestRule.getActivity();

    //Invalid qr code
    @Test
    public void testInvalidQR() {
        onView(withId(R.id.scanButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withText("Invalid QR code. Please try again")).check(matches(isDisplayed()));
    }

    //Insufficient funds
    @Test
    public void testInsufficientFunds() {
        onView(withId(R.id.scanButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withText("Insufficient funds. Please top up to proceed with the transaction"))
                .check(matches(isDisplayed()));
    }

    //Incorrect security code
    @Test
    public void testWrongSecurityCode() {
        onView(withId(R.id.scanButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withText("Please enter your security code to proceed")).check(matches(isDisplayed()));
        onView(withId(R.id.pinEntry)).check(matches(withText("")));
        onView(withId(R.id.pinEntry)).perform(typeText("555555"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withText("Wrong security code")).inRoot(new ToastTest())
                .check(matches(isDisplayed()));
    }

    //Successful payment
    @Test
    public void testSuccessfulPayment() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
        int balance = Integer.parseInt(getText(withId(R.id.balance)).replace(".", "")
                .replace("Rp", ""));
        onView(withId(R.id.scanButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }

        onView(withText("Please enter your security code to proceed")).check(matches(isDisplayed()));
        onView(withId(R.id.pinEntry)).check(matches(withText("")));
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        String date = sdf.format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
        String newBalance = changeToRupiahFormat(balance - 10000);
        onView(withId(R.id.balance)).check(matches(withText(newBalance)));
        onView(withText("Yay! Your transaction is completed!")).inRoot(new ToastTest())
                .check(matches(isDisplayed()));
        onView(withId(R.id.navigation_history)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withText(date)).check(matches(isDisplayed()));
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