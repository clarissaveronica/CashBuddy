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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeSecurityCodeActivity extends AppCompatActivity {

    PinEntryEditText pinEntry;

    // Firebase Authentication
    DatabaseReference userDatabase;
    private FirebaseUser user;
    private String uid;
    private FirebaseAuth firebaseAuth;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_security_code);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.changeSecurityCodeTitle);

        //Check user's role
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        uid = user.getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference();

        pinEntry = findViewById(R.id.pinEntry);

        Intent intent = getIntent();
        role = intent.getStringExtra("role");

        if (pinEntry != null) {
            pinEntry.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
                @Override
                public void onPinEntered(CharSequence str) {
                    if (str.toString().length()==6) {

                        if(role.equals("user")){
                            FirebaseDatabase.getInstance().getReference("users").child(uid).child("password").setValue(str.toString());
                        }else if(role.equals("merchant")){
                            FirebaseDatabase.getInstance().getReference("merchant").child(uid).child("password").setValue(str.toString());
                        }
                        Toast.makeText(ChangeSecurityCodeActivity.this, "Security code has successfully been changed", Toast.LENGTH_SHORT).show();

                        finish();
                    }
                }
            });
        }
    }

}
