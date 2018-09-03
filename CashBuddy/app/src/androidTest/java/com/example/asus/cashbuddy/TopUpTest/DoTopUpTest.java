package com.example.asus.cashbuddy.TopUpTest;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.TextView;

import com.example.asus.cashbuddy.Activity.All.LoginActivity;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.ToastTest;
import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.NumberFormat;
import java.util.Locale;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
public class DoTopUpTest {

    @Rule
    public IntentsTestRule<LoginActivity> doTopUpIntentsTestRule = new IntentsTestRule<>(LoginActivity.class);

    private LoginActivity topUpActivity = doTopUpIntentsTestRule.getActivity();

    //Wrong security code when confirming user's top up request
    @Test
    public void testConfirmWrongSC() {
        loginAdmin();
        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(1).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.request_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.accept_request)).perform(click());
        onView(withText("Please enter your security code to proceed")).check(matches(isDisplayed()));
        onView(withId(R.id.pinEntry)).check(matches(withText("")));
        onView(withId(R.id.pinEntry)).perform(typeText("111111"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Wrong security code"))
                .inRoot(new ToastTest()).check(matches(isDisplayed()));
    }

    //Accept top up request
    @Test
    public void testAcceptRequest() {
        loginUser();
        int balance = Integer.parseInt(getText(withId(R.id.balance)).replace(".", "")
                .replace("Rp", ""));
        logoutUser();
        loginAdmin();
        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(1).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.request_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        int topupamount = Integer.parseInt(getText(withId(R.id.topup_detail_amount))
                .replace(".", "").replace("Rp", "")
                .replace("Top-up Amount : ", ""));
        onView(withId(R.id.accept_request)).perform(click());
        onView(withText("Please enter your security code to proceed")).check(matches(isDisplayed()));
        onView(withId(R.id.pinEntry)).check(matches(withText("")));
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Request accepted"))
                .inRoot(new ToastTest()).check(matches(isDisplayed()));
        Espresso.pressBack();
        logoutAdmin();
        loginUser();
        String newBalance = changeToRupiahFormat(balance + topupamount);
        onView(withId(R.id.balance)).check(matches(withText(newBalance)));
    }

    //Reject top up request
    @Test
    public void testRejectRequest() {
        loginAdmin();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(1).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.request_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.decline_request)).perform(click());
        onView(withText("Please enter your security code to proceed")).check(matches(isDisplayed()));
        onView(withId(R.id.pinEntry)).check(matches(withText("")));
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Request declined"))
                .inRoot(new ToastTest()).check(matches(isDisplayed()));
    }

    //Top up amount is less than Rp50.000
    @Test
    public void testWrongTopUpAmount() {
        loginAdmin();
        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(0).perform(click());
        onView(withId(R.id.topUpAmount)).perform(typeText("5000"));
        onView(withId(R.id.topUpAmount)).check(matches(hasErrorText("Minimal amount for top up is Rp 50.000")));
    }

    //QR code is invalid
    @Test
    public void testInvalidQRCode() {
        loginAdmin();
        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(0).perform(click());
        onView(withId(R.id.topUpAmount)).perform(typeText("50000"));
        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(1).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
        onView(withText("Invalid QR code. Please try again")).check(matches(isDisplayed()));
    }

    //Phone number is invalid
    @Test
    public void testWrongPhoneNumber() {
        loginAdmin();
        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(0).perform(click());
        onView(withId(R.id.topUpAmount)).perform(typeText("50000"));
        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(2).perform(click());
        onView(withId(R.id.phoneNum)).perform(typeText("123456789"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.phoneNum)).check(matches(hasErrorText
                ("Invalid phone number")));
    }

    //Wrong security code
    @Test
    public void testWrongSecurityCode() {
        loginAdmin();
        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(0).perform(click());
        onView(withId(R.id.topUpAmount)).perform(typeText("50000"));
        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(2).perform(click());
        onView(withId(R.id.phoneNum)).perform(typeText("0866666666"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.pinEntry)).perform(typeText("555555"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Wrong security code")).inRoot(new ToastTest())
                .check(matches(isDisplayed()));
    }

    //Top up successful with scanning qr code
    @Test
    public void testSuccessfulScanTopUp() {
        loginUser();
        int balance = Integer.parseInt(getText(withId(R.id.balance)).replace(".", "")
                .replace("Rp", ""));
        logoutUser();
        loginAdmin();
        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(0).perform(click());
        onView(withId(R.id.topUpAmount)).perform(typeText("50000"));
        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(1).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Top up successful")).inRoot(new ToastTest())
                .check(matches(isDisplayed()));
        logoutAdmin();
        loginUser();
        String newBalance = changeToRupiahFormat(balance + 50000);
        onView(withId(R.id.balance)).check(matches(withText(newBalance)));
    }

    //Top up successful with phone number
    @Test
    public void testSuccessfulPhoneNumberTopUp() {
        loginUser();
        int balance = Integer.parseInt(getText(withId(R.id.balance)).replace(".", "")
                .replace("Rp", ""));
        logoutUser();
        loginAdmin();
        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(0).perform(click());
        onView(withId(R.id.topUpAmount)).perform(typeText("50000"));
        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(2).perform(click());
        onView(withId(R.id.phoneNum)).perform(typeText("0866666666"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Top up successful")).inRoot(new ToastTest())
                .check(matches(isDisplayed()));
        logoutAdmin();
        loginUser();
        String newBalance = changeToRupiahFormat(balance + 50000);
        onView(withId(R.id.balance)).check(matches(withText(newBalance)));
    }

    @After
    public void after() {
        FirebaseAuth.getInstance().signOut();
    }

    public void loginUser(){
        FirebaseAuth.getInstance().signOut();
        onView(withId(R.id.loginUsername)).perform(typeText("0866666666"));
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
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
    }

    public void loginAdmin(){
        FirebaseAuth.getInstance().signOut();
        onView(withId(R.id.loginUsername)).perform(typeText("0811111111"));
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
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
    }

    public void logoutUser(){
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.signOutButton)).perform(click());
        onView(withText("Yes")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
    }

    public void logoutAdmin(){
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("Log Out")).perform(click());
        onView(withText("Yes")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
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