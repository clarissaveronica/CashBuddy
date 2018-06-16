package com.example.asus.cashbuddy.Activity.All;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.cashbuddy.Activity.Admin.AdminMainActivity;
import com.example.asus.cashbuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    // Views
    private ViewGroup loadingPanel;
    private EditText loginPhoneNumEditText;
    private TextView signUp;
    private Button signIn;
    private FirebaseUser user;
    private String uid;
    private boolean exist;

    DatabaseReference userDatabase, mUser, mMerchant;

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
                //if(validateNum()) {
                    Toast.makeText(LoginActivity.this, "OTP has been sent", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(LoginActivity.this, PhoneNumVerificationActivity.class);
                    intent.putExtra("number", loginPhoneNumEditText.getText().toString());
                    startActivity(intent);
               // }else{

                //}
            }
        });

        mUser = FirebaseDatabase.getInstance().getReference().child("users");
        mMerchant = FirebaseDatabase.getInstance().getReference().child("merchant");
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
        //Check user's role
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        uid = user.getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference();

        userDatabase.child("role").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.getValue().toString();
                    Intent intent;

                    String currentUser = firebaseAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    switch (role){
                        case "USER":
                            mUser.child(currentUser).child("device_token").setValue(deviceToken);
                            intent = new Intent(LoginActivity.this, PasswordVerificationActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        case "MERCHANT":
                            mMerchant.child(currentUser).child("device_token").setValue(deviceToken);
                            intent = new Intent(LoginActivity.this, PasswordVerificationActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        case "UNVERIFIED_MERCHANT":
                            AlertDialog alertDialog = new AlertDialog.Builder(
                                    LoginActivity.this).create();
                            alertDialog.setTitle("Merchant not verified!");
                            alertDialog.setMessage("Please wait for our admin to verify your store.");
                            alertDialog.setIcon(R.drawable.logo);
                            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            alertDialog.show();
                            FirebaseAuth.getInstance().signOut();
                            loadingPanel.setVisibility(View.INVISIBLE);
                            break;
                        case "ADMIN":
                            intent = new Intent(LoginActivity.this, PasswordVerificationActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        default: break;

                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
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
        /*//Check if user exist
        exist = false;
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        uid = user.getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference();

        userDatabase.child("role").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.getValue().toString();
                    Intent intent;

                    String currentUser = firebaseAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    switch (role){
                        case "USER":
                            mUser.child(currentUser).child("device_token").setValue(deviceToken);
                            intent = new Intent(LoginActivity.this, PasswordVerificationActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        case "STORE":
                            mMerchant.child(currentUser).child("device_token").setValue(deviceToken);
                            intent = new Intent(LoginActivity.this, PasswordVerificationActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        case "UNVERIFIED_STORE":
                            AlertDialog alertDialog = new AlertDialog.Builder(
                                    LoginActivity.this).create();
                            alertDialog.setTitle("Merchant not verified!");
                            alertDialog.setMessage("Please wait for our admin to verify your store.");
                            alertDialog.setIcon(R.drawable.logo);
                            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            alertDialog.show();
                            FirebaseAuth.getInstance().signOut();
                            loadingPanel.setVisibility(View.INVISIBLE);
                            break;
                        case "ADMIN":
                            intent = new Intent(LoginActivity.this, PasswordVerificationActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        default: break;

                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });*/

        return exist;
    }

}
