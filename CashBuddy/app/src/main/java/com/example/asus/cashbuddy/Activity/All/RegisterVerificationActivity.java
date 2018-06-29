package com.example.asus.cashbuddy.Activity.All;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

public class RegisterVerificationActivity extends AppCompatActivity {

    private static final String TAG = "PhoneAuth";

    //Initialize views
    TextView text;
    PinEntryEditText pinEntry;
    Button resendButton;
    //Declare timer
    CountDownTimer cTimer = null;

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
        textViewTitle.setText(R.string.phoneNumVerificationTitle);

        //Get data
        Intent intent = getIntent();
        num = "+62" + intent.getStringExtra("number").substring(1);
        name = intent.getStringExtra("name");
        role = intent.getStringExtra("role");
        location = intent.getStringExtra("location");
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");

        startTimer();
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
                startTimer();
                resendCode(num);
            }
        });
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
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if(e instanceof FirebaseAuthInvalidCredentialsException){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterVerificationActivity.this);
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
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if(role.equals("newUser")) {
                                AccountUtil.createUserOtherInformation(name, num, email, hash(password), null, 0);
                                Intent intent = new Intent(RegisterVerificationActivity.this, MainActivity.class);
                                startActivity(intent);
                                finishAffinity();
                            }else if(role.equals("newMerchant")){
                                AccountUtil.createMerchantOtherInformation(name, num, hash(password), email, location, 0);
                                Intent intent = new Intent(RegisterVerificationActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finishAffinity();
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

}
