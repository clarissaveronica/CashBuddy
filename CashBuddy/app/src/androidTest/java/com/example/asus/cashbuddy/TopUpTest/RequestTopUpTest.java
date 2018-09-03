package com.example.asus.cashbuddy.TopUpTest;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.asus.cashbuddy.Activity.User.MainActivity;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.ToastTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
public class RequestTopUpTest {

    @Rule
    public IntentsTestRule<MainActivity> userTopUpActivityIntentsTestRule = new IntentsTestRule<>(MainActivity.class);

    private MainActivity topUpActivity = userTopUpActivityIntentsTestRule.getActivity();

    //Empty fields
    @Test
    public void testAllNull() {
        onView(withId(R.id.topupButton)).perform(click());
        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(1).perform(click());
        onView(withId(R.id.submitButton)).perform(scrollTo(), click());
        onView(withId(R.id.topUpAmount)).check(matches(hasErrorText("Minimal amount for top up is Rp10.000")));
        onView(withId(R.id.bankName)).check(matches(hasErrorText("Bank name is required")));
        onView(withId(R.id.userName)).check(matches(hasErrorText("User name is required")));
        onView(withText("Image is required")).inRoot(withDecorView(not(is(userTopUpActivityIntentsTestRule.getActivity()
                .getWindow().getDecorView())))).check(matches(isDisplayed()));
    }

    //Top up amount is less than Rp10.000
    @Test
    public void testWrongTopUpAmount() {
        onView(withId(R.id.topupButton)).perform(click());
        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(1).perform(click());
        onView(withId(R.id.topUpAmount)).perform(typeText("5000"));
        onView(withId(R.id.submitButton)).perform(scrollTo(), click());
        onView(withId(R.id.topUpAmount)).check(matches(hasErrorText
                ("Minimal amount for top up is Rp10.000")));
    }

    //Incorrect security code
    @Test
    public void testWrongSecurityCode() {
        onView(withId(R.id.topupButton)).perform(click());
        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(1).perform(click());
        onView(withId(R.id.topUpAmount)).perform(typeText("10000"));
        onView(withId(R.id.bankName)).perform(scrollTo(), typeText("BCA"));
        onView(withId(R.id.userName)).perform(scrollTo(), typeText("Clarissa"));
        onView(withId(R.id.uploadButton)).perform(scrollTo(), click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        }

        onView(withId(R.id.submitButton)).perform(scrollTo(), click());
        onView(withText("Please enter your security code to proceed")).check(matches(isDisplayed()));
        onView(withId(R.id.pinEntry)).check(matches(withText("")));
        onView(withId(R.id.pinEntry)).perform(typeText("555555"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Wrong security code")).inRoot(new ToastTest())
                .check(matches(isDisplayed()));
    }

    //Successful top up
    @Test
    public void testSuccessfulTopUp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        onView(withId(R.id.topupButton)).perform(click());
        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(1).perform(click());
        onView(withId(R.id.topUpAmount)).perform(typeText("10000"));
        onView(withId(R.id.bankName)).perform(scrollTo(), typeText("BCA"));
        onView(withId(R.id.userName)).perform(scrollTo(), typeText("Clarissa"));
        onView(withId(R.id.uploadButton)).perform(scrollTo(), click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        }

        onView(withId(R.id.submitButton)).perform(scrollTo(), click());
        onView(withText("Please enter your security code to proceed")).check(matches(isDisplayed()));
        onView(withId(R.id.pinEntry)).check(matches(withText("")));
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        String date = sdf.format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
        onView(withText("Top up request has been successfully sent. Your request will be processed in 1x24 hours"))
                .inRoot(new ToastTest()).check(matches(isDisplayed()));

        loginAdmin();
        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(1).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withText(date)).check(matches(isDisplayed()));
    }


    public void loginAdmin(){
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.signOutButton)).perform(click());
        onView(withText("Yes")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
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
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
    }
}