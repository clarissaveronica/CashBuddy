package com.example.asus.cashbuddy.WithdrawTest;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.TextView;

import com.example.asus.cashbuddy.Activity.Merchant.MerchantMainActivity;
import com.example.asus.cashbuddy.Activity.User.UserScanActivity;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.ToastTest;
import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

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

@RunWith(AndroidJUnit4.class)
public class RequestWithdrawTest {

    @Rule
    public IntentsTestRule<MerchantMainActivity> withdrawActivityIntentsTestRule = new IntentsTestRule<>(MerchantMainActivity.class);

    private MerchantMainActivity withdrawActivity = withdrawActivityIntentsTestRule.getActivity();

    //Invalid inputs
    @Test
    public void testInvalidInputs() {
        onView(withId(R.id.withdrawButton)).perform(click());
        onView(withId(R.id.amountWithdraw)).perform(typeText("5000"));
        onView(withId(R.id.submitButton)).perform(scrollTo(), click());
        onView(withId(R.id.amountWithdraw)).check(matches(hasErrorText
                ("Minimal amount for withdraw is Rp10.000")));
        onView(withId(R.id.bankName)).check(matches(hasErrorText("Bank name is required")));
        onView(withId(R.id.userName)).check(matches(hasErrorText("Merchant name is required")));
        onView(withId(R.id.bankNumber)).check(matches(hasErrorText("Bank number is required")));
    }

    //Insufficient balance
    @Test
    public void testInsufficientBalance() {
        onView(withId(R.id.withdrawButton)).perform(click());
        onView(withId(R.id.amountWithdraw)).perform(typeText("1000000"));
        onView(withId(R.id.bankName)).perform(typeText("BCA"));
        onView(withId(R.id.userName)).perform(scrollTo(), typeText("Clarissa"));
        onView(withId(R.id.bankNumber)).perform(scrollTo(), typeText("123456"));
        onView(withId(R.id.submitButton)).perform(scrollTo(), click());
        onView(withText("Insufficient funds!")).check(matches(isDisplayed()));
    }

    //Incorrect security code
    @Test
    public void testWrongSecurityCode() {
        onView(withId(R.id.withdrawButton)).perform(click());
        onView(withId(R.id.amountWithdraw)).perform(typeText("10000"));
        onView(withId(R.id.bankName)).perform(typeText("BCA"));
        onView(withId(R.id.userName)).perform(scrollTo(), typeText("Clarissa"));
        onView(withId(R.id.bankNumber)).perform(scrollTo(), typeText("123456"));
        onView(withId(R.id.submitButton)).perform(scrollTo(), click());
        onView(withId(R.id.pinEntry)).perform(typeText("555555"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Wrong security code")).inRoot(new ToastTest())
                .check(matches(isDisplayed()));
    }

    //Withdrawal request successfully sent
    @Test
    public void testSuccessfulRequest() {
        //Initialize
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
        int balance = Integer.parseInt(getText(withId(R.id.balance)).replace(".", "")
                .replace("Rp", ""));
        onView(withId(R.id.withdrawButton)).perform(click());

        //Input
        onView(withId(R.id.amountWithdraw)).perform(typeText("10000"));
        onView(withId(R.id.bankName)).perform(typeText("BCA"));
        onView(withId(R.id.userName)).perform(scrollTo(), typeText("Clarissa"));
        onView(withId(R.id.bankNumber)).perform(scrollTo(), typeText("123456"));
        onView(withId(R.id.submitButton)).perform(scrollTo(), click());
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        String date = sdf.format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());

        //Test matches
        onView(withText("Withdraw request has been successfully sent. Your request will be processed in 1x24 hours"))
                .inRoot(new ToastTest()).check(matches(isDisplayed()));
        onView(withId(R.id.balance)).check(matches(withText(changeToRupiahFormat(balance - 10000))));
        onView(withId(R.id.navigation_history)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
        onView(withText(date)).check(matches(isDisplayed()));
        loginAdmin();
        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(2).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) { }
        onView(withText(date)).check(matches(isDisplayed()));
        loginMerchant();
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

    public void loginMerchant(){
        Espresso.pressBack();
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("Log Out")).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withText("Yes")).inRoot(isDialog()).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.loginUsername)).perform(typeText("0833333333"));
        onView(withId(R.id.loginUsername)).perform(closeSoftKeyboard());
        onView(withId(R.id.signInButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        try {
            Thread.sleep(2000);
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
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
    }
}