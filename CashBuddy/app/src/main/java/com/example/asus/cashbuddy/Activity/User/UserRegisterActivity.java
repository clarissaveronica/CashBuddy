package com.example.asus.cashbuddy.Activity.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.asus.cashbuddy.Activity.All.PhoneNumVerificationActivity;
import com.example.asus.cashbuddy.Activity.All.RegisterVerificationActivity;
import com.example.asus.cashbuddy.Fragment.All.RegistrationCancellationDialogFragment;
import com.example.asus.cashbuddy.R;

public class UserRegisterActivity extends AppCompatActivity implements RegistrationCancellationDialogFragment.CancellationHandler{

    // Views
    private ViewGroup progressBarLayout;

    // Personal information views
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText nameEditText;
    private TextInputEditText phoneNumberEditText;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText("User Registration");

        //Initialize views
        progressBarLayout = findViewById(R.id.progress_bar_layout);
        emailEditText = findViewById(R.id.registration_email_edit_text);
        passwordEditText = findViewById(R.id.registration_password_edit_text);
        nameEditText = findViewById(R.id.user_registration_detail_name);
        phoneNumberEditText = findViewById(R.id.user_registration_detail_phone_number);
        submitButton = findViewById(R.id.registration_sign_up_button);

        progressBarLayout.setVisibility(View.GONE);

        submitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(validateForm()) {
                    String email = emailEditText.getText().toString();
                    String password = passwordEditText.getText().toString();
                    String name = nameEditText.getText().toString();
                    String number = phoneNumberEditText.getText().toString();

                    Intent intent = new Intent(UserRegisterActivity.this, RegisterVerificationActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    intent.putExtra("name", name);
                    intent.putExtra("number", number);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public boolean validateForm() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String name = nameEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();

        boolean valid = true;

        if(TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            valid = false;
        }

        if(TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            valid = false;
        }else if(password.length() !=6){
            passwordEditText.setError("Your password must be 6 digits");
            valid = false;
        }

        if(TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            valid = false;
        }

        if(TextUtils.isEmpty(phoneNumber)) {
            phoneNumberEditText.setError("Phone number is required");
            valid = false;
        }else if(phoneNumber.length() < 10){
            phoneNumberEditText.setError("Invalid phone number");
            valid = false;
        }

        return valid;
    }

    private void showCancellationConfirmation() {
        DialogFragment dialogFragment = new RegistrationCancellationDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onBackPressed() {
        if(progressBarLayout.getVisibility() == View.GONE) showCancellationConfirmation();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Cancel register
                if(progressBarLayout.getVisibility() == View.GONE) showCancellationConfirmation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void cancelRegistration() {
        setResult(-1);
        finish();
    }

    @Override
    public void resumeRegistration() {}
}
