package com.example.asus.cashbuddy.Activity.All;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.asus.cashbuddy.Activity.User.MainActivity;
import com.example.asus.cashbuddy.Activity.User.UserRegisterActivity;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.AccountUtil;
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

import java.util.concurrent.TimeUnit;

public class RegisterVerificationActivity extends AppCompatActivity {

    private static final String TAG = "PhoneAuth";

    //Initialize views
    TextView text;
    PinEntryEditText pinEntry;
    Button resendButton;

    private String phoneVerificationId, num, name, password, email, location, role;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_verification);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText("Phone Number Verification");

        //Get data
        Intent intent = getIntent();
        num = "+62" + intent.getStringExtra("number").substring(1);
        name = intent.getStringExtra("name");
        role = intent.getStringExtra("role");
        location = intent.getStringExtra("location");
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");

        sendCode(num);

        //Initialize views
        text = findViewById(R.id.textView);
        pinEntry = findViewById(R.id.pinEntry);
        resendButton = findViewById(R.id.resendButton);

        text.setText("We have sent the verification pin to : " + num);

        auth = FirebaseAuth.getInstance();

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
                resendCode(num);
            }
        });
    }

    @Override
    public void onBackPressed() {
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
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if(e instanceof FirebaseAuthInvalidCredentialsException){
                    Toast.makeText(RegisterVerificationActivity.this, "Invalid credential", Toast.LENGTH_LONG).show();
                }else if(e instanceof FirebaseTooManyRequestsException){
                    Toast.makeText(RegisterVerificationActivity.this, "SMS Quota exceeded", Toast.LENGTH_LONG).show();
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
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if(role.equals("newUser")) {
                                AccountUtil.createUserOtherInformation(name, num, email, password, null, 0);
                                Intent intent = new Intent(RegisterVerificationActivity.this, MainActivity.class);
                                startActivity(intent);
                                finishAffinity();
                            }else{
                                AccountUtil.createMerchantOtherInformation(name, num, password, email, location, 0);
                                Intent intent = new Intent(RegisterVerificationActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finishAffinity();
                            }
                        } else {
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    public void resendCode(String num) {
        setUpVerificationCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                num,
                60,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks,
                resendToken);
    }
}
