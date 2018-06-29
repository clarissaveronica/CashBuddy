package com.example.asus.cashbuddy.Activity.All;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.asus.cashbuddy.Activity.Merchant.MerchantMainActivity;
import com.example.asus.cashbuddy.Activity.User.MainActivity;
import com.example.asus.cashbuddy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class ChangePhoneNumberVerificationActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private String uid, phone, phoneVerificationId, role, old;
    private DatabaseReference databaseUser;
    private TextView text;
    private Button resendButton;
    private PinEntryEditText pinEntry;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private CountDownTimer cTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_phone_number_verification);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText("New Phone Number Verification");

        Intent intent = getIntent();
        old = intent.getStringExtra("old");
        phone = intent.getStringExtra("phone");
        role = intent.getStringExtra("role");

        //Get info from firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();

        if(role.equals("user")) {
            databaseUser = FirebaseDatabase.getInstance().getReference("users");
        }else databaseUser = FirebaseDatabase.getInstance().getReference("merchant");

        //Initialize views
        text = findViewById(R.id.textView);
        pinEntry = findViewById(R.id.pinEntry);
        resendButton = findViewById(R.id.resendButton);

        text.setText("We have sent the verification pin to : " + phone);

        if (pinEntry != null) {
            pinEntry.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
                @Override
                public void onPinEntered(CharSequence str) {
                    if (str.toString().length()==6) {
                        final String code = str.toString();
                        verifyCode(code);
                    }
                }
            });
        }

        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer();
                resendCode(phone);
            }
        });

        sendCode(phone);
        startTimer();
    }

    @Override
    public void onBackPressed() {
        cancelTimer();
        finish();
    }

    public void sendCode(String num){
        setUpVerificationCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(num, 60, TimeUnit.SECONDS, this, verificationCallbacks);
    }

    private void setUpVerificationCallbacks(){
        verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if(e instanceof FirebaseAuthInvalidCredentialsException){
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChangePhoneNumberVerificationActivity.this);
                    builder.setMessage("Invalid Credential. Please enter a correct phone number")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.setTitle("Oops!");
                    alert.show();
                }else if(e instanceof FirebaseTooManyRequestsException){
                    Toast.makeText(getApplicationContext(), "SMS Quota exceeded", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token){
                phoneVerificationId = verificationId;
                resendToken = token;
            }
        };
    }

    public void verifyCode(String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        firebaseAuth.getCurrentUser().updatePhoneNumber(credential);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            successChange();
                            finishAffinity();
                            if(role.equals("user")) {
                                Intent intent = new Intent(ChangePhoneNumberVerificationActivity.this, MainActivity.class);
                                startActivity(intent);
                            }else{
                                Intent intent = new Intent(ChangePhoneNumberVerificationActivity.this, MerchantMainActivity.class);
                                startActivity(intent);
                            }
                        } else {
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(), "Verification pin is wrong", Toast.LENGTH_SHORT).show();
                                pinEntry.setText("");
                            }
                        }
                    }
                });
    }

    public void successChange(){
        databaseUser.child(uid).child("phoneNumber").setValue(phone);
        FirebaseDatabase.getInstance().getReference().child("phonenumbertouid").child(old).removeValue();
        FirebaseDatabase.getInstance().getReference().child("phonenumbertouid").child(phone).setValue(uid);
    }


    public void resendCode(String num) {
        Toast.makeText(getApplicationContext(), "New verification pin has been sent", Toast.LENGTH_SHORT).show();

        setUpVerificationCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                num,
                60,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks,
                resendToken);
    }

    //start timer function
    void startTimer() {
        cTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                resendButton.setText("RESEND CODE (" + millisUntilFinished/1000 +")");
                resendButton.setAlpha(.5f);
            }
            public void onFinish() {
                resendButton.setText("RESEND CODE");
                resendButton.setAlpha(1);
                resendButton.setEnabled(true);
            }
        };
        cTimer.start();
    }

    //cancel timer
    void cancelTimer() {
        if(cTimer!=null)
            cTimer.cancel();
    }
}
