package com.example.asus.cashbuddy;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
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

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
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
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
public class TransferTest {

    @Rule
    public IntentsTestRule<MainActivity> transferIntentsTestRule = new IntentsTestRule<>(MainActivity.class);

    private MainActivity transferActivity = transferIntentsTestRule.getActivity();

    //Invalid inputs (Transfer amount less than Rp10.000, invalid qr, invalid phone number, empty fields)
    @Test
    public void testInvalidInputs() {
        onView(withId(R.id.transferButton)).perform(click());

        //Test transfer amount less than Rp10.000
        onView(withId(R.id.amountTransfer)).perform(typeText("5000"));
        onView(withId(R.id.amountTransfer)).check(matches(hasErrorText("Minimal amount for transfer is Rp 10.000")));

        //Test invalid QR
        onView(withId(R.id.amountTransfer)).perform(replaceText("10000"));
        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(1).perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        }
        onView(withText("Invalid QR code. Please try again")).check(matches(isDisplayed()));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

        //Test invalid phone number
        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(2).perform(click());
        onView(withId(R.id.phoneNum)).perform(typeText("123456"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitButton)).perform(click());
        onView(withId(R.id.phoneNum)).check(matches(hasErrorText("Invalid phone number")));

        //Test empty fields
        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(3).perform(click());
        onView(withId(R.id.submitButton)).perform(scrollTo(), click());
        onView(withId(R.id.bankName)).check(matches(hasErrorText("Bank name is required")));
        onView(withId(R.id.userName)).check(matches(hasErrorText("Merchant name is required")));
        onView(withId(R.id.bankNumber)).check(matches(hasErrorText("Bank number is required")));
    }

    //Insufficient funds + incorrect security code
    @Test
    public void testFailedTransfer() {
        onView(withId(R.id.transferButton)).perform(click());

        //Test insufficient funds
        onView(withId(R.id.amountTransfer)).perform(typeText("500000"));
        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(2).perform(click());
        onView(withId(R.id.phoneNum)).perform(typeText("0888888888"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) { }
        onView(withText("Insufficient funds. Please top up to proceed with the transaction")).check(matches(isDisplayed()));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

        //Test incorrect security code
        onView(withId(R.id.amountTransfer)).perform(replaceText("10000"));
        onView(withId(R.id.submitButton)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.pinEntry)).perform(typeText("555555"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Wrong security code")).inRoot(new ToastTest()).check(matches(isDisplayed()));
    }

    //Successful transfer by scanning qr code
    @Test
    public void testSuccessfulScanTransfer() {
        //Get receiver's balance and sender's balance
        loginUser("0888888888");
        int receiverBalance = Integer.parseInt(getText(withId(R.id.balance)).replace(".", "").replace("Rp", ""));
        loginUser("0866666666");
        int senderBalance = Integer.parseInt(getText(withId(R.id.balance)).replace(".", "").replace("Rp", ""));

        //Do transfer by scanning qr code
        onView(withId(R.id.transferButton)).perform(click());
        onView(withId(R.id.amountTransfer)).perform(typeText("10000"));
        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(1).perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Transfer successful")).inRoot(new ToastTest()).check(matches(isDisplayed()));

        //Get receiver's new balance and sender's new balance
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        String newSenderBalance = changeToRupiahFormat(senderBalance - 10000);
        onView(withId(R.id.balance)).check(matches(withText(newSenderBalance)));
        loginUser("0888888888");
        String newReceiverBalance = changeToRupiahFormat(receiverBalance + 10000);
        onView(withId(R.id.balance)).check(matches(withText(newReceiverBalance)));
        loginUser("0866666666");
    }

    //Successful transfer by phone number
    @Test
    public void testSuccessfulPhoneNumTransfer() {
        //Get receiver's balance and sender's balance
        loginUser("0888888888");
        int receiverBalance = Integer.parseInt(getText(withId(R.id.balance)).replace(".", "").replace("Rp", ""));
        loginUser("0866666666");
        int senderBalance = Integer.parseInt(getText(withId(R.id.balance)).replace(".", "").replace("Rp", ""));

        //Do transfer by phone number
        onView(withId(R.id.transferButton)).perform(click());
        onView(withId(R.id.amountTransfer)).perform(typeText("10000"));
        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(2).perform(click());
        onView(withId(R.id.phoneNum)).perform(typeText("0888888888"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitButton)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        onView(withText("Transfer successful")).inRoot(new ToastTest()).check(matches(isDisplayed()));

        //Get receiver's new balance and sender's new balance
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        String newSenderBalance = changeToRupiahFormat(senderBalance - 10000);
        onView(withId(R.id.balance)).check(matches(withText(newSenderBalance)));
        loginUser("0888888888");
        String newReceiverBalance = changeToRupiahFormat(receiverBalance + 10000);
        onView(withId(R.id.balance)).check(matches(withText(newReceiverBalance)));
        loginUser("0866666666");
    }

    //Successful transfer to a bank account
    @Test
    public void testSuccessfulBankTransfer() {
        //Get balance
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) { }
        int balance = Integer.parseInt(getText(withId(R.id.balance)).replace(".", "").replace("Rp", ""));

        //Do transfer by phone number
        onView(withId(R.id.transferButton)).perform(click());
        onView(withId(R.id.amountTransfer)).perform(typeText("10000"));
        onView(withId(R.id.spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(3).perform(click());
        onView(withId(R.id.bankName)).perform(typeText("BCA"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.userName)).perform(scrollTo(), typeText("Clarissa"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.bankNumber)).perform(scrollTo(), typeText("123456"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.submitButton)).perform(scrollTo(), click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
        String date = sdf.format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
        onView(withText("Request has been successfully sent. Your request will be processed in 1x24 hours")).inRoot(new ToastTest()).check(matches(isDisplayed()));

        //Get new balance
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        String newBalance = changeToRupiahFormat(balance - 10000);
        onView(withId(R.id.balance)).check(matches(withText(newBalance)));
        loginAdmin(1);
        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(2).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) { }
        onView(withText(date)).check(matches(isDisplayed()));
        loginAdmin(0);
    }

    public void loginAdmin(int status){
        if(status == 1) {
            onView(withId(R.id.navigation_profile)).perform(click());
            onView(withId(R.id.signOutButton)).perform(click());
            onView(withText("Yes")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());
            onView(withId(R.id.loginUsername)).perform(typeText("0811111111"));
            onView(withId(R.id.loginUsername)).perform(closeSoftKeyboard());
            onView(withId(R.id.signInButton)).perform(click());
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) { }
            onView(withId(R.id.pinEntry)).perform(typeText("123456"));
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) { }
            onView(withId(R.id.pinEntry)).perform(typeText("123456"));
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) { }
        }else{
            Espresso.pressBack();
            openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
            onView(withText("Log Out")).perform(click());
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) { }
            onView(withText("Yes")).inRoot(isDialog()).perform(click());
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) { }
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
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
        }
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