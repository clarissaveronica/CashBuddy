package com.example.asus.cashbuddy;

import android.content.ComponentName;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.asus.cashbuddy.Activity.User.MainActivity;
import com.example.asus.cashbuddy.Activity.User.UserRegisterActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
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
public class UserValidateFormRegistrationTest {

    @Rule
    public IntentsTestRule<UserRegisterActivity> registerActivityTestRule = new IntentsTestRule<>(UserRegisterActivity.class);

    private UserRegisterActivity userRegisterActivity = registerActivityTestRule.getActivity();

    //Test when all field are empty
    @Test
    public void testSetAllNull() {
        onView(withId(R.id.registration_sign_up_button)).perform(scrollTo(), click());
        onView(withId(R.id.registration_email_edit_text)).check(matches(hasErrorText("Email is required")));
        onView(withId(R.id.user_registration_detail_name)).check(matches(hasErrorText("Name is required")));
        onView(withId(R.id.registration_password_edit_text)).check(matches(hasErrorText("Security code is required")));
        onView(withId(R.id.user_registration_detail_phone_number)).check(matches(hasErrorText("Phone number is required")));
    }

    //Invalid inputs
    @Test
    public void testInvalidInputs() {
        onView(withId(R.id.user_registration_detail_phone_number)).perform(typeText("123"));
        onView(withId(R.id.registration_password_edit_text)).perform(typeText("123"));
        onView(withId(R.id.registration_email_edit_text)).perform(scrollTo(), typeText("abc"));
        onView(withId(R.id.registration_email_edit_text)).perform(closeSoftKeyboard());
        onView(withId(R.id.registration_sign_up_button)).perform(scrollTo(), click());
        onView(withId(R.id.registration_email_edit_text)).check(matches(hasErrorText("Invalid email")));
        onView(withId(R.id.registration_password_edit_text)).check(matches(hasErrorText("Your security code must be 6 digits")));
        onView(withId(R.id.user_registration_detail_phone_number)).check(matches(hasErrorText("Invalid phone number")));
    }

    //Phone number is already registered
    @Test
    public void testSetPhoneNumberAlreadyRegistered() {
        onView(withId(R.id.user_registration_detail_phone_number)).perform(typeText("0811111111"));
        onView(withId(R.id.user_registration_detail_phone_number)).perform(closeSoftKeyboard());
        onView(withId(R.id.registration_sign_up_button)).perform(scrollTo(), click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.user_registration_detail_phone_number)).check(matches(hasErrorText("Phone number is already used")));
    }

    @Test
    public void wrongOTP() {
        onView(withId(R.id.user_registration_detail_phone_number)).perform(typeText("0822222222"));
        onView(withId(R.id.registration_password_edit_text)).perform(typeText("123456"));
        onView(withId(R.id.user_registration_detail_name)).perform(scrollTo(), typeText("Clarissa"));
        onView(withId(R.id.registration_email_edit_text)).perform(scrollTo(), typeText("clarissa@gmail.com"));
        onView(withId(R.id.registration_email_edit_text)).perform(closeSoftKeyboard());
        onView(withId(R.id.registration_sign_up_button)).perform(click());
        onView(withId(R.id.registration_sign_up_button)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.registration_sign_up_button)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.textView)).check(matches(withText("We have sent the verification pin to : +62822222222")));
        onView(withId(R.id.pinEntry)).perform(typeText("555555"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withText("Verification pin is wrong")).inRoot(withDecorView(not(is(registerActivityTestRule.getActivity().getWindow()
                .getDecorView())))).check(matches(isDisplayed()));
    }

    @Test
    public void registerSuccessful() {
        onView(withId(R.id.user_registration_detail_phone_number)).perform(typeText("0822222222"));
        onView(withId(R.id.registration_password_edit_text)).perform(typeText("123456"));
        onView(withId(R.id.user_registration_detail_name)).perform(scrollTo(), typeText("Clarissa"));
        onView(withId(R.id.registration_email_edit_text)).perform(scrollTo(), typeText("clarissa@gmail.com"));
        onView(withId(R.id.registration_email_edit_text)).perform(closeSoftKeyboard());
        onView(withId(R.id.registration_sign_up_button)).perform(click());
        onView(withId(R.id.registration_sign_up_button)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        onView(withId(R.id.textView)).check(matches(withText("We have sent the verification pin to : +62822222222")));
        onView(withId(R.id.pinEntry)).perform(typeText("123456"));
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ignored) {
        }
        intended(hasComponent(new ComponentName(getTargetContext(), MainActivity.class)));
    }
}