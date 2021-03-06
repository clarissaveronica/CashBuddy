package com.example.asus.cashbuddy.Activity.All;


import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.asus.cashbuddy.Activity.Admin.AdminMainActivity;
import com.example.asus.cashbuddy.Activity.Merchant.MerchantMainActivity;
import com.example.asus.cashbuddy.Activity.User.MainActivity;
import com.example.asus.cashbuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.security.MessageDigest;

public class SecurityCodeVerificationActivity extends AppCompatActivity {

    PinEntryEditText pinEntry;

    // Firebase Authentication
    DatabaseReference userDatabase, mUser, mMerchant;
    private FirebaseUser user;
    private String uid;
    private String pin, role, currentUser, deviceToken;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_code_verification);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.securityCodeVerificationTitle);

        //Check user's role
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        uid = user.getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = firebaseAuth.getCurrentUser().getUid();
        deviceToken = FirebaseInstanceId.getInstance().getToken();

        mUser = FirebaseDatabase.getInstance().getReference().child("users");
        mMerchant = FirebaseDatabase.getInstance().getReference().child("merchant");

        pinEntry = findViewById(R.id.pinEntry);

        if (pinEntry != null) {
            pinEntry.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
                @Override
                public void onPinEntered(CharSequence str) {
                    if (str.toString().length()==6) {
                        pin = str.toString();
                        verify();
                    }
                }
            });
        }
    }

    //Verify user's password input
    private void verify(){
        userDatabase.child("role").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    role = snapshot.getValue().toString();

                    switch (role){
                        case "USER":
                            checkUser();
                            break;
                        case "MERCHANT":
                            checkMerchant();
                            break;
                        case "ADMIN":
                            checkUser();
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

    //Check user's role & login
    private void checkUser(){
        userDatabase.child("users").child(uid).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String password = snapshot.getValue(String.class);
                if(hash(pin).equals(password)){
                    if(role.equals("USER")){
                        mUser.child(currentUser).child("device_token").setValue(deviceToken);
                        Intent intent = new Intent(SecurityCodeVerificationActivity.this, MainActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }else{
                        Intent intent = new Intent(SecurityCodeVerificationActivity.this, AdminMainActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }
                }else if(!hash(pin).equals(password)){
                    Toast.makeText(getApplicationContext(), "Wrong security code", Toast.LENGTH_SHORT).show();
                    pinEntry.setText("");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //Check if user is a merchant & login
    private void checkMerchant(){
        userDatabase.child("merchant").child(uid).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String password = snapshot.getValue(String.class);
                if(hash(pin).equals(password)){
                    mMerchant.child(currentUser).child("device_token").setValue(deviceToken);
                    Intent intent = new Intent(SecurityCodeVerificationActivity.this, MerchantMainActivity.class);
                    startActivity(intent);
                    finishAffinity();
                }else if(!hash(pin).equals(password)){
                    Toast.makeText(getApplicationContext(), "Wrong security code", Toast.LENGTH_SHORT).show();
                    pinEntry.setText("");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public String hash (String pass){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pass.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onBackPressed() {
        if(getIntent().getIntExtra("newLogin", 0) == 1){
            FirebaseAuth.getInstance().signOut();
        }
        finish();
    }
}
