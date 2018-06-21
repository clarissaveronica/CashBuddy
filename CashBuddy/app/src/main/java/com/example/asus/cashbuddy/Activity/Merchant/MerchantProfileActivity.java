package com.example.asus.cashbuddy.Activity.Merchant;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.cashbuddy.Activity.All.ChangePhoneNumberActivity;
import com.example.asus.cashbuddy.Model.Merchant;
import com.example.asus.cashbuddy.Others.EditCancellationDialogFragment;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.AccountUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MerchantProfileActivity extends AppCompatActivity implements EditCancellationDialogFragment.CancellationHandler{

    private TextInputEditText name, email, location;
    private TextView phone;
    private Merchant merchant;
    private Button editButton, submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_profile);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.editProfileTitle);

        //Initialize views
        name = findViewById(R.id.merchantName);
        email = findViewById(R.id.merchantEmail);
        phone = findViewById(R.id.phoneNumber);
        location = findViewById(R.id.merchantLocation);
        editButton = findViewById(R.id.editButton);
        submitButton = findViewById(R.id.submitButton);

        merchant = AccountUtil.getCurrentMerchant();

        name.setText(merchant.getMerchantName());
        email.setText(merchant.getEmail());
        phone.setText(merchant.getPhoneNumber());
        location.setText(merchant.getLocation());

        submitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                saveChanges();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(MerchantProfileActivity.this, ChangePhoneNumberActivity.class);
                intent.putExtra("role", "merchant");
                startActivity(intent);
            }
        });

        name.addTextChangedListener(generalTextWatcher);
        email.addTextChangedListener(generalTextWatcher);
        location.addTextChangedListener(generalTextWatcher);
    }

    private void saveChanges() {
        // Update information //
        String nameMerchant = name.getText().toString();
        String emailMerchant = email.getText().toString();
        String locationMerchant = location.getText().toString();

        merchant.setMerchantName(nameMerchant);
        merchant.setEmail(emailMerchant);
        merchant.setLocation(locationMerchant);

        Task<Void> updateTask;
        updateTask = AccountUtil.updateMerchantOtherInformation(merchant);

        updateTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Check status
                if(task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Profile saved", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Failed to save", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private TextWatcher generalTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (email != null && name != null) {
                if (!merchant.getEmail().equals(email.getText().toString()) || !merchant.getMerchantName().equals(name.getText().toString())
                        || !merchant.getLocation().equals(location.getText().toString())) {
                    if (isEmailValid(email.getText().toString())) {
                        submitButton.setAlpha(1);
                        submitButton.setEnabled(true);
                    } else {
                        email.setError("Invalid email");
                        submitButton.setAlpha(0.5f);
                        submitButton.setEnabled(false);
                    }
                } else {
                    submitButton.setAlpha(0.5f);
                    submitButton.setEnabled(false);
                }
            }else{
                if(email == null) email.setError("Email is required");
                if(name == null) name.setError("Name is required");
                if(location == null) location.setError("Location is required");
            }
        }

    };

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    public void resumeRegistration() {}

    @Override
    public void cancelRegistration() {
        finish();
    }

    @Override
    public void onBackPressed() {
        showCancellationConfirmation();
    }

    private void showCancellationConfirmation() {
        if(!isEdited()) {
            finish();
            return;
        }
        DialogFragment dialogFragment = new EditCancellationDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), null);
    }

    private boolean isEdited() {
        String userName = name.getText().toString();
        String userEmail = email.getText().toString();
        String userLoc = location.getText().toString();
        return !(userName.equals(merchant.getMerchantName()) && userEmail.equals(merchant.getEmail()) && userLoc.equals(merchant.getLocation()));
    }
}
