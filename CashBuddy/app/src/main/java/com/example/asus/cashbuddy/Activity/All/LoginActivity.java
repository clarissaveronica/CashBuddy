package com.example.asus.cashbuddy.Activity.All;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.cashbuddy.Activity.User.UserRegisterActivity;
import com.example.asus.cashbuddy.Model.User;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.AccountUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
    private String tempUid;
    private boolean valid;
    private boolean check;

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
                }else {
                    newLoginSession();
                }

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

    private void newLoginSession(){
        //Check user's role
        userDatabase = FirebaseDatabase.getInstance().getReference();

        userDatabase.child("role").child(tempUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.getValue().toString();
                    Intent intent;

                    switch (role){
                        case "USER":
                            Toast.makeText(LoginActivity.this, "OTP has been sent", Toast.LENGTH_LONG).show();
                            intent = new Intent(LoginActivity.this, LoginVerificationActivity.class);
                            intent.putExtra("number", loginPhoneNumEditText.getText().toString());
                            startActivity(intent);
                            break;
                        case "MERCHANT":
                            Toast.makeText(LoginActivity.this, "OTP has been sent", Toast.LENGTH_LONG).show();
                            intent = new Intent(LoginActivity.this, LoginVerificationActivity.class);
                            intent.putExtra("number", loginPhoneNumEditText.getText().toString());
                            startActivity(intent);
                            break;
                        case "UNVERIFIED_MERCHANT":
                            AlertDialog alertDialog = new AlertDialog.Builder(
                                    LoginActivity.this).create();
                            alertDialog.setTitle("Merchant not verified!");
                            alertDialog.setMessage("Please wait for our admin to verify your store.");
                            alertDialog.setIcon(R.drawable.logo);
                            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseAuth.getInstance().signOut();
                                }
                            });
                            alertDialog.show();
                            loadingPanel.setVisibility(View.INVISIBLE);
                            break;
                        case "ADMIN":
                            Toast.makeText(LoginActivity.this, "OTP has been sent", Toast.LENGTH_LONG).show();
                            intent = new Intent(LoginActivity.this, LoginVerificationActivity.class);
                            intent.putExtra("number", loginPhoneNumEditText.getText().toString());
                            startActivity(intent);
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
                            intent = new Intent(LoginActivity.this, SecurityCodeVerificationActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        case "MERCHANT":
                            mMerchant.child(currentUser).child("device_token").setValue(deviceToken);
                            intent = new Intent(LoginActivity.this, SecurityCodeVerificationActivity.class);
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
                            intent = new Intent(LoginActivity.this, SecurityCodeVerificationActivity.class);
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
        String username = loginPhoneNumEditText.getText().toString();
        if(TextUtils.isEmpty(username)) {
            loginPhoneNumEditText.setError(getString(R.string.requires_phonenum));
            valid = false;
        }

        if(!TextUtils.isEmpty(username)) {
            validateNum(new OnGetDataListener() {
                @Override
                public void onSuccess(boolean checked) {
                    loginPhoneNumEditText.setError("Phone number is not registered");
                    valid = false;
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFailure() {
                    newLoginSession();
                    valid = true;
                }
            });
        }

        return valid;
    }

    private void validateNum(final OnGetDataListener listener){
        listener.onStart();

        String num = "+62" + loginPhoneNumEditText.getText().toString().substring(1);

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userNameRef = rootRef.child("phonenumbertouid").child(num);
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    check = false;
                    listener.onSuccess(check);
                }else{
                    tempUid = dataSnapshot.getValue().toString();
                    check = true;
                    listener.onFailure();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {listener.onFailure();}
        };
        userNameRef.addListenerForSingleValueEvent(eventListener);
    }

    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess(boolean checked);
        void onStart();
        void onFailure();
    }

}
