package com.example.asus.cashbuddy;

import android.content.ComponentName;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.asus.cashbuddy.Activity.Admin.AdminMainActivity;
import com.example.asus.cashbuddy.Activity.All.LoginActivity;
import com.example.asus.cashbuddy.Activity.All.SecurityCodeVerificationActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
public class LoginTest {

    @Rule
    public IntentsTestRule<LoginActivity> loginActivityActivityTestRule = new IntentsTestRule<>(LoginActivity.class);

    private LoginActivity loginActivity = loginActivityActivityTestRule.getActivity();

    //Test when field are empty
    @Test
    public void testSetAllNull() {
        onView(withId(R.id.signInButton)).perform(click());
        onView(withId(R.id.loginUsername)).check(matches(hasErrorText("Phone Number is required")));
    }

    //Phone number is not registered
    @Test
    public void testSetPhoneNumberAlreadyRegistered() {
        onView(withId(R.id.loginUsername)).perform(typeText("08123456789"));
        onView(withId(R.id.loginUsername)).perform(closeSoftKeyboard());
        onView(withId(R.id.signInButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.loginUsername)).check(matches(hasErrorText("Phone number is not registered")));
    }

    @Test
    public void testWrongOTP() {
        onView(withId(R.id.loginUsername)).perform(typeText("0811111111"));
        onView(withId(R.id.loginUsername)).perform(closeSoftKeyboard());
        onView(withId(R.id.signInButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.textView)).check(matches(withText("We have sent the verification pin to : +62811111111")));
        onView(withId(R.id.pinEntry)).perform(typeText("555555"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withText("Verification pin is wrong")).inRoot(withDecorView(not(is(loginActivityActivityTestRule.getActivity()
                .getWindow().getDecorView())))).check(matches(isDisplayed()));
    }

    @Test
    public void testWrongSecurityCode() {
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
        onView(withId(R.id.pinEntry)).perform(typeText("555555"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        intended(hasComponent(new ComponentName(getTargetContext(), SecurityCodeVerificationActivity.class)));
        FirebaseAuth.getInstance().signOut();
    }

    @Test
    public void testSuccessfulLogin() {
        onView(withId(R.id.loginUsername)).perform(typeText("0811111111"));
        onView(withId(R.id.loginUsername)).perform(closeSoftKeyboard());
        onView(withId(R.id.signInButton)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.textView)).check(matches(withText("We have sent the verification pin to : +62811111111")));
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.pinEntry)).check(matches(withText("")));
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
        intended(hasComponent(new ComponentName(getTargetContext(), AdminMainActivity.class)));
        FirebaseAuth.getInstance().signOut();
    }
}