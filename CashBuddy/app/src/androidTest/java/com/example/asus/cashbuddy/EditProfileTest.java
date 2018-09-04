package com.example.asus.cashbuddy;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.asus.cashbuddy.Activity.User.MainActivity;
import com.example.asus.cashbuddy.Activity.User.UserScanActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
public class EditProfileTest {

    @Before
    public void before(){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
    }

    @Rule
    public IntentsTestRule<MainActivity> editProfileIntentsTestRule = new IntentsTestRule<>(MainActivity.class);

    private MainActivity editProfileActivity = editProfileIntentsTestRule.getActivity();

    //Invalid inputs (incorrect email, empty field)
    @Test
    public void testFailedEditProfile() {
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.editProfileButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.userName)).perform(replaceText(""));
        onView(withId(R.id.userEmail)).perform(scrollTo(), replaceText(""));
        onView(withId(R.id.submitButton)).check(matches(not(isEnabled())));
        onView(withId(R.id.userName)).check(matches(hasErrorText("Name is required")));
        onView(withId(R.id.userEmail)).check(matches(hasErrorText("Email is required")));
    }

    //Successful edit profile
    @Test
    public void testSuccessfulEditProfile() {
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.editProfileButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.userName)).perform(replaceText("Clarissa V"));
        onView(withId(R.id.userEmail)).perform(scrollTo(), replaceText("clarissav@gmail.com"));
        onView(withId(R.id.submitButton)).check(matches(isEnabled()));
        onView(withId(R.id.submitButton)).perform(scrollTo(), click());
        onView(withText("Profile saved")).inRoot(new ToastTest()).check(matches(isDisplayed()));
    }

    //Incorrect security code when changing security code
    @Test
    public void testFailedChangeSecurityCode() {
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.changePassButton)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.pinEntry)).perform(typeText("555555"));
        onView(withText("Wrong security code")).inRoot(new ToastTest()).check(matches(isDisplayed()));
    }

    //Successful security code change
    @Test
    public void testSuccessfulChangeSC() {
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.changePassButton)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) { }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("Security code has successfully been changed")).inRoot(new ToastTest()).check(matches(isDisplayed()));
    }

    //Incorrect security code when changing security code (registered phone num, incorrect otp)
    @Test
    public void testFailedChangePhoneNum() {
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.editProfileButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.editButton)).perform(click());

        //Test phone number is already registered
        onView(withId(R.id.newPhoneNum)).perform(typeText("0811111111"));
        onView(withId(R.id.submitButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.newPhoneNum)).check(matches(hasErrorText("Phone number is already registered")));

        //Test incorrect OTP
        onView(withId(R.id.newPhoneNum)).perform(replaceText("0866666666"));
        onView(withId(R.id.submitButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.pinEntry)).perform(typeText("555555"));
        onView(withText("Verification pin is wrong")).inRoot(new ToastTest()).check(matches(isDisplayed()));
    }

    //Successful security code change
    @Test
    public void testSuccessfulChangePhoneNum() {
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.editProfileButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.editButton)).perform(click());
        onView(withId(R.id.newPhoneNum)).perform(typeText("0822222222"));
        onView(withId(R.id.submitButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        onView(withText("Phone number has successfully been changed")).inRoot(new ToastTest()).check(matches(isDisplayed()));
    }
}