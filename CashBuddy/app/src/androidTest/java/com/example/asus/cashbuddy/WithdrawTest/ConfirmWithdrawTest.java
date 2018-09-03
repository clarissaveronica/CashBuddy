package com.example.asus.cashbuddy.WithdrawTest;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.asus.cashbuddy.Activity.Admin.AdminMainActivity;
import com.example.asus.cashbuddy.Activity.All.LoginActivity;
import com.example.asus.cashbuddy.Activity.User.UserScanActivity;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.ToastTest;
import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matcher;
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
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;

@RunWith(AndroidJUnit4.class)
public class ConfirmWithdrawTest {

    @Rule
    public IntentsTestRule<AdminMainActivity> confirmWithdrawActivityIntentsTestRule = new IntentsTestRule<>(AdminMainActivity.class);

    private AdminMainActivity confirmWithdrawActivity = confirmWithdrawActivityIntentsTestRule.getActivity();

    //Incorrect security code
    @Test
    public void testWrongSecurityCode() {
        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(2).perform(click());
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

    //Decline request
    @Test
    public void testDeclineRequest() {
        loginMerchant();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
        int balance = Integer.parseInt(getText(withId(R.id.balance)).replace(".", "")
                .replace("Rp", ""));

        loginAdmin();
        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(2).perform(click());
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
        int withdrawAmount = Integer.parseInt(getText(withId(R.id.withdraw_detail_amount))
                .replace(".", "").replace("Rp", ""));
        onView(withId(R.id.decline_request)).perform(click());
        onView(withText("Please enter your security code to proceed"))
                .check(matches(isDisplayed()));
        onView(withId(R.id.pinEntry)).check(matches(withText("")));
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Request declined"))
                .inRoot(new ToastTest()).check(matches(isDisplayed()));
        Espresso.pressBack();
        loginMerchant();
        String newBalance = changeToRupiahFormat(balance + withdrawAmount);
        onView(withId(R.id.balance)).check(matches(withText(newBalance)));
    }

    //Accept request
    @Test
    public void testAcceptRequest() {
        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(2).perform(click());
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
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Request accepted"))
                .inRoot(new ToastTest()).check(matches(isDisplayed()));
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

    public void loginMerchant(){
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("Log Out")).perform(click());
        onView(withText("Yes")).inRoot(isDialog()).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.loginUsername)).perform(typeText("0833333333"));
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
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
    }

    public void logoutMerchant(){
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

    public String changeToRupiahFormat(int money){
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        String temp = formatRupiah.format((double)money);

        return temp;
    }
}