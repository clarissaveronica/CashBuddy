package com.example.asus.cashbuddy.Activity.All;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.asus.cashbuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChangeSecurityCodeVerificationActivity extends AppCompatActivity {

    PinEntryEditText pinEntry;

    // Firebase Authentication
    DatabaseReference userDatabase;
    private FirebaseUser user;
    private String uid;
    private String pin;
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
        userDatabase.child("role").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.getValue().toString();

                    switch (role){
                        case "USER":
                            checkUser();
                            break;
                        case "MERCHANT":
                            checkMerchant();
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

    //Check user's role
    private void checkUser(){
        userDatabase.child("users").child(uid).child("password").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String password = snapshot.getValue(String.class);
                if(pin.equals(password)){
                    Intent intent = new Intent(ChangeSecurityCodeVerificationActivity.this, ChangeSecurityCodeActivity.class);
                    intent.putExtra("role", "user");
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(ChangeSecurityCodeVerificationActivity.this, "Wrong security code", Toast.LENGTH_LONG).show();
                    pinEntry.setText("");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //Check if user is a merchant
    private void checkMerchant(){
        userDatabase.child("merchant").child(uid).child("password").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String password = snapshot.getValue(String.class);
                if(pin.equals(password)){
                    Intent intent = new Intent(ChangeSecurityCodeVerificationActivity.this, ChangeSecurityCodeActivity.class);
                    intent.putExtra("role", "merchant");
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(ChangeSecurityCodeVerificationActivity.this, "Wrong security code", Toast.LENGTH_LONG).show();
                    pinEntry.setText("");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

}
