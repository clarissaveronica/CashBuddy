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
public class SplitBillTest {

    @Rule
    public IntentsTestRule<MainActivity> splitBillIntentsTestRule = new IntentsTestRule<>(MainActivity.class);

    private MainActivity splitBillActivity = splitBillIntentsTestRule.getActivity();

    //Invalid inputs on create and split transaction (splitting is not done correctly, phone number is not registered, empty fields)
    @Test
    public void testInvalidInputs() {
        onView(withId(R.id.splitButton)).perform(click());

        //Test invalid inputs on Create Split Bill
        onView(withId(R.id.insertPrice)).perform(replaceText("Rp5,000"));
        onView(withId(R.id.person2)).perform(scrollTo(), typeText("12345"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.splitButton)).perform(scrollTo(), click());
        onView(withId(R.id.insertPrice)).check(matches(hasErrorText("Minimum bill to split is Rp10.000")));
        onView(withId(R.id.person2)).check(matches(hasErrorText("Invalid phone number")));
        onView(withId(R.id.price1)).check(matches(hasErrorText("Price is required")));
        onView(withId(R.id.price2)).check(matches(hasErrorText("Price is required")));

        //Test invalid inputs on Split Existing Bill
        onView(withText("Bills")).perform(click());
        onView(withId(R.id.split_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.price1)).perform(scrollTo(), typeText("5000"));
        onView(withId(R.id.price2)).perform(scrollTo(), typeText("2000"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.splitButton)).perform(scrollTo(), click());
        onView(withId(R.id.person2)).check(matches(hasErrorText("Phone number is required")));
        onView(withId(R.id.price1)).check(matches(hasErrorText("Invalid amount")));
        onView(withId(R.id.price2)).check(matches(hasErrorText("Invalid amount")));
    }

    //Split bill request is successfully sent (both method)
    @Test
    public void testSuccessfulSendReq() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        onView(withId(R.id.splitButton)).perform(click());

        //Test successful split bill request on Create Split Bill
        onView(withId(R.id.insertPrice)).perform(click(), typeText("10000"));
        onView(withId(R.id.price1)).perform(scrollTo(), typeText("5000"));
        onView(withId(R.id.price2)).perform(scrollTo(), typeText("5000"));
        onView(withId(R.id.person2)).perform(scrollTo(), typeText("0899999999"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.splitButton)).perform(scrollTo(), click());
        String date1 = sdf.format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
        onView(withText("Split bill request sent")).inRoot(new ToastTest()).check(matches(isDisplayed()));

        //Test successful split bill request on Split Existing Bill
        onView(withId(R.id.splitButton)).perform(click());
        onView(withText("Bills")).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.split_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.price1)).perform(scrollTo(), typeText("5000"));
        onView(withId(R.id.price2)).perform(scrollTo(), typeText("5000"));
        onView(withId(R.id.person2)).perform(scrollTo(), typeText("0888888888"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.splitButton)).perform(scrollTo(), click());
        String date2 = sdf.format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
        onView(withText("Split bill request sent")).inRoot(new ToastTest()).check(matches(isDisplayed()));
        Espresso.pressBack();

        //Check requests on receiver's account
        loginUser("0899999999");
        onView(withId(R.id.splitButton)).perform(click());
        onView(withText("Received")).perform(click());
        onView(withText(date1)).check(matches(isDisplayed()));
        Espresso.pressBack();
        loginUser("0888888888");
        onView(withId(R.id.splitButton)).perform(click());
        onView(withText("Received")).perform(click());
        onView(withText(date2)).check(matches(isDisplayed()));
        Espresso.pressBack();
        loginUser("0866666666");
    }

    //Not enough balance
    @Test
    public void testInsufficientFunds() {
        loginUser("0899999999");
        onView(withId(R.id.splitButton)).perform(click());
        onView(withText("Received")).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.received_split_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.accept_request)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withText("Insufficient funds. Please top up to proceed.")).check(matches(isDisplayed()));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        Espresso.pressBack();
        Espresso.pressBack();
        loginUser("0866666666");
    }

    //Wrong security code when confirming split bill request
    @Test
    public void testWrongSecurityCode() {
        loginUser("0888888888");
        onView(withId(R.id.splitButton)).perform(click());
        onView(withText("Received")).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.received_split_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.accept_request)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.pinEntry)).perform(typeText("555555"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Wrong security code")).inRoot(new ToastTest()).check(matches(isDisplayed()));
        onView(withText("Cancel")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        Espresso.pressBack();
        Espresso.pressBack();
        loginUser("0866666666");
    }

    //Reject split bill request
    @Test
    public void testRejectSplitBillReq() {
        loginUser("0899999999");
        onView(withId(R.id.splitButton)).perform(click());
        onView(withText("Received")).perform(click());
        onView(withId(R.id.received_split_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.decline_request)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Request declined")).inRoot(new ToastTest()).check(matches(isDisplayed()));
        Espresso.pressBack();
        loginUser("0866666666");
    }

    //Accept split bill request
    @Test
    public void testAcceptSplitBillReq() {
        //Check balance on both users
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) { }
        int senderBalance = Integer.parseInt(getText(withId(R.id.balance)).replace(".", "").replace("Rp", ""));
        loginUser("0888888888");
        int receiverBalance = Integer.parseInt(getText(withId(R.id.balance)).replace(".", "").replace("Rp", ""));

        //Accept split request
        onView(withId(R.id.splitButton)).perform(click());
        onView(withText("Received")).perform(click());
        onView(withId(R.id.received_split_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.accept_request)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Request accepted")).inRoot(new ToastTest()).check(matches(isDisplayed()));

        //Check new balance and request status
        Espresso.pressBack();
        String newReceiverBalance = changeToRupiahFormat(receiverBalance - 5000);
        onView(withId(R.id.balance)).check(matches(withText(newReceiverBalance)));
        loginUser("0866666666");
        String newSenderBalance = changeToRupiahFormat(senderBalance + 5000);
        onView(withId(R.id.balance)).check(matches(withText(newSenderBalance)));
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