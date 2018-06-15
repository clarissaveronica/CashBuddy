package com.example.asus.cashbuddy.Activity.All;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.cashbuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    // Views
    private ViewGroup loadingPanel;
    private EditText loginPhoneNumEditText;
    private TextView signUp;
    private Button signIn;
    private FirebaseUser user;

    DatabaseReference mUser, mMerchant;

    // Firebase Authentication
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.splashScreenTheme);
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        // Initialize views
        loginPhoneNumEditText = findViewById(R.id.loginUsername);
        loadingPanel = findViewById(R.id.loadingPanel);

        //Sign up
        signUp = findViewById(R.id.signUpButton);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterMainActivity.class);
                startActivity(intent);
            }
        });

        //Sign in
        signIn = findViewById(R.id.signInButton);
        signIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(!validateLoginInfo()) {
                    loadingPanel.setVisibility(View.INVISIBLE);
                    return;
                }

                //Check whether user exist or not
                if(validateNum()) {
                    Toast.makeText(LoginActivity.this, "OTP has been sent", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(LoginActivity.this, PhoneNumVerificationActivity.class);
                    intent.putExtra("number", loginPhoneNumEditText.getText().toString());
                    startActivity(intent);
                    finish();
                }else{

                }
            }
        });

        mUser = FirebaseDatabase.getInstance().getReference().child("users");
        mMerchant = FirebaseDatabase.getInstance().getReference().child("merchants");
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        //Check if user signed-in
        if (user != null) {
            login();
        }
        else {
            loadingPanel.setVisibility(View.GONE);
        }
    }

    private void login() {
        Intent intent = new Intent (LoginActivity.this, PasswordVerificationActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean validateLoginInfo() {
        boolean valid = true;

        String username = loginPhoneNumEditText.getText().toString();
        if(TextUtils.isEmpty(username)) {
            loginPhoneNumEditText.setError(getString(R.string.requires_phonenum));
            valid = false;
        } else {
            loginPhoneNumEditText.setError(null);
        }

        return valid;
    }

    private boolean validateNum(){
        return true;
    }

}
