package com.example.asus.cashbuddy.Activity.Merchant;

import android.content.Intent;
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

import com.example.asus.cashbuddy.Activity.All.NumberVerificationActivity;
import com.example.asus.cashbuddy.Fragment.All.RegistrationCancellationDialogFragment;
import com.example.asus.cashbuddy.R;

public class MerchantRegisterActivity extends AppCompatActivity implements RegistrationCancellationDialogFragment.CancellationHandler{

    // Views
    private ViewGroup progressBarLayout;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText nameEditText;
    private TextInputEditText phoneNumberEditText;
    private TextInputEditText locationEditText;
    private Button signUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_register);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText("Merchant Registration");

        //Initialize views
        progressBarLayout = findViewById(R.id.progress_bar_layout);
        emailEditText = findViewById(R.id.merchantEmail);
        passwordEditText = findViewById(R.id.merchantPassword);
        nameEditText = findViewById(R.id.merchantName);
        phoneNumberEditText = findViewById(R.id.merchantPhoneNum);
        locationEditText = findViewById(R.id.merchantLocation);
        signUpBtn = findViewById(R.id.signUpButton);

        progressBarLayout.setVisibility(View.GONE);

        signUpBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(validateForm()) {
                    progressBarLayout.setVisibility(View.VISIBLE);

                    String email = emailEditText.getText().toString();
                    String password = passwordEditText.getText().toString();
                    String name = nameEditText.getText().toString();
                    String phoneNumber = phoneNumberEditText.getText().toString();
                    String location = locationEditText.getText().toString();

                    Intent intent = new Intent(MerchantRegisterActivity.this, NumberVerificationActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    intent.putExtra("name", name);
                    intent.putExtra("number", phoneNumber);
                    intent.putExtra("location", location);
                    intent.putExtra("role", "merchant");
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
        String location = locationEditText.getText().toString();

        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            valid = false;
        }else if(password.length() !=6){
            passwordEditText.setError("Your password must be 6 digits");
            valid = false;
        }

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            valid = false;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumberEditText.setError("Phone number is required");
            valid = false;
        }

        if (TextUtils.isEmpty(location)) {
            locationEditText.setError("Location is required");
            valid = false;
        }

        return valid;
    }

    @Override
    public void onBackPressed() {
        if (progressBarLayout.getVisibility() == View.GONE) showCancellationConfirmation();
    }

    private void showCancellationConfirmation() {
        DialogFragment dialogFragment = new RegistrationCancellationDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), null);
    }

    private void finishRegistration() {
        progressBarLayout.setVisibility(View.GONE);
        Intent intent = new Intent(MerchantRegisterActivity.this, NumberVerificationActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Cancel register
                if (progressBarLayout.getVisibility() == View.GONE) showCancellationConfirmation();
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