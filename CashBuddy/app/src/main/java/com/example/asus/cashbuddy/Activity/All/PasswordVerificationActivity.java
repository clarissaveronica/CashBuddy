package com.example.asus.cashbuddy.Activity.All;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

public class PasswordVerificationActivity extends AppCompatActivity {

    PinEntryEditText pinEntry;

    // Firebase Authentication
    DatabaseReference userDatabase, mUser, mMerchant;
    private FirebaseUser user;
    private String uid;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_verification);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText("Password Verification");

        //Check user's role
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        uid = user.getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference();

        mUser = FirebaseDatabase.getInstance().getReference().child("users");
        mMerchant = FirebaseDatabase.getInstance().getReference().child("merchants");

        pinEntry = findViewById(R.id.pinEntry);

        if (pinEntry != null) {
            pinEntry.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
                @Override
                public void onPinEntered(CharSequence str) {
                    if (str.toString().length()==6) {
                        final String pin = str.toString();
                        userDatabase.child("users").child(uid).child("password").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                String password = snapshot.getValue(String.class);
                                if(pin.equals(password)){
                                    verify();
                                }else{
                                    Toast.makeText(PasswordVerificationActivity.this, "Wrong Pin", Toast.LENGTH_LONG).show();
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                }
            });
        }
    }

    private void verify(){
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
                            intent = new Intent(PasswordVerificationActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        case "MERCHANT":
                            mMerchant.child(currentUser).child("device_token").setValue(deviceToken);
                            intent = new Intent(PasswordVerificationActivity.this, MerchantMainActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        case "UNVERIFIED_MERCHANT":
                            AlertDialog alertDialog = new AlertDialog.Builder(
                                    PasswordVerificationActivity.this).create();
                            alertDialog.setTitle("Merchant not verified!");
                            alertDialog.setMessage("Please wait for our admin to verify your account");
                            alertDialog.setIcon(R.drawable.logo);
                            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            alertDialog.show();
                            FirebaseAuth.getInstance().signOut();
                            break;
                        case "ADMIN":
                            intent = new Intent(PasswordVerificationActivity.this, AdminMainActivity.class);
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
}
