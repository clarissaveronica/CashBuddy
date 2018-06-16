package com.example.asus.cashbuddy.Activity.All;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.asus.cashbuddy.Activity.Admin.AdminMainActivity;
import com.example.asus.cashbuddy.Activity.Merchant.MerchantMainActivity;
import com.example.asus.cashbuddy.Activity.User.MainActivity;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.AccountUtil;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

public class PhoneNumVerificationActivity extends AppCompatActivity {

    private static final String TAG = "PhoneAuth";

    //Initialize views
    TextView text;
    PinEntryEditText pinEntry;
    Button resendButton;

    private String num;
    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    DatabaseReference userDatabase, mUser, mMerchant;
    private FirebaseAuth auth;
    private String uid;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_num_verification);

        Intent intent = getIntent();

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText("Phone Number Verification");

        //Initialize views
        text = findViewById(R.id.textView);
        pinEntry = findViewById(R.id.pinEntry);
        resendButton = findViewById(R.id.resendButton);

        num = "+62" + intent.getStringExtra("number").substring(1);
        text.setText("We have sent the verification pin to : " + num);

        //Database ref
        auth = FirebaseAuth.getInstance();
        mUser = FirebaseDatabase.getInstance().getReference().child("users");
        mMerchant = FirebaseDatabase.getInstance().getReference().child("merchant");

        sendCode(num);

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
    public void onBackPressed() {;
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
                    Toast.makeText(PhoneNumVerificationActivity.this, "Invalid credential", Toast.LENGTH_LONG).show();
                }else if(e instanceof FirebaseTooManyRequestsException){
                    Toast.makeText(PhoneNumVerificationActivity.this, "SMS Quota exceeded", Toast.LENGTH_LONG).show();
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
                            login();
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

    public void login(){
        //Check user's role
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uid = user.getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference();

        userDatabase.child("role").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.getValue().toString();
                    Intent intent;

                    String currentUser = auth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    switch (role){
                        case "USER":
                            mUser.child(currentUser).child("device_token").setValue(deviceToken);
                            intent = new Intent(PhoneNumVerificationActivity.this, MainActivity.class);
                            startActivity(intent);
                            finishAffinity();
                            break;
                        case "MERCHANT":
                            mMerchant.child(currentUser).child("device_token").setValue(deviceToken);
                            intent = new Intent(PhoneNumVerificationActivity.this, MerchantMainActivity.class);
                            startActivity(intent);
                            finishAffinity();
                            break;
                        case "ADMIN":
                            intent = new Intent(PhoneNumVerificationActivity.this, AdminMainActivity.class);
                            startActivity(intent);
                            finishAffinity();
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
