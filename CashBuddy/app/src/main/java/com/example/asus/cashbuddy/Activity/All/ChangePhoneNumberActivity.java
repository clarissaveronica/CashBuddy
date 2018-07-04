package com.example.asus.cashbuddy.Activity.All;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.asus.cashbuddy.Activity.User.UserScanActivity;
import com.example.asus.cashbuddy.Model.User;
import com.example.asus.cashbuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChangePhoneNumberActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private String uid, role;
    private TextInputEditText newPhone;
    private TextView phone;
    private DatabaseReference databaseUser;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_phone_number);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText("Change Phone Number");

        phone = findViewById(R.id.phoneNumber);
        newPhone = findViewById(R.id.newPhoneNum);
        submitButton = findViewById(R.id.submitButton);
        newPhone.addTextChangedListener(generalTextWatcher);

        Intent intent = getIntent();
        role = intent.getStringExtra("role");

        //Get info from firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();

        if(role.equals("user")) {
            databaseUser = FirebaseDatabase.getInstance().getReference("users");
        }else databaseUser = FirebaseDatabase.getInstance().getReference("merchant");

        databaseUser.child(uid).child("phoneNumber").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    phone.setText(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        submitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                checkNum();
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
            String phoneNumber = newPhone.getText().toString();
            if(TextUtils.isEmpty(phoneNumber)) {
                //newPhone.setError("Phone number is required");
                submitButton.setAlpha(0.5f);
                submitButton.setEnabled(false);
            }else if(phoneNumber.length() < 10){
                //newPhone.setError("Invalid phone number");
                submitButton.setAlpha(0.5f);
                submitButton.setEnabled(false);
            }else if(!phoneNumber.substring(0,1).equals("0")){
                //newPhone.setError("Invalid phone number");
                submitButton.setAlpha(0.5f);
                submitButton.setEnabled(false);
            }else{
                String temp = "+62" + newPhone.getText().toString().substring(1);
                if(temp.equals(phone.getText().toString())){
                    submitButton.setAlpha(0.5f);
                    submitButton.setEnabled(false);
                }else {
                    submitButton.setAlpha(1);
                    submitButton.setEnabled(true);
                }
            }
        }

    };

    public void checkNum(){
        final String num = "+62" + newPhone.getText().toString().substring(1);

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userNameRef = rootRef.child("phonenumbertouid").child(num);
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    newPhone.setError("Phone number is already registered");
                }else{
                    Intent intent = new Intent(ChangePhoneNumberActivity.this, ChangePhoneNumberVerificationActivity.class);
                    intent.putExtra("role", role);
                    intent.putExtra("old", phone.getText().toString());
                    intent.putExtra("phone", num);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        userNameRef.addListenerForSingleValueEvent(eventListener);
    }

}
