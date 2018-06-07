package com.example.asus.cashbuddy.Activity.All;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.cashbuddy.Activity.Admin.AdminMainActivity;
import com.example.asus.cashbuddy.Activity.Merchant.MerchantMainActivity;
import com.example.asus.cashbuddy.Activity.User.MainActivity;
import com.example.asus.cashbuddy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    // Views
    private ViewGroup loadingPanel;
    private EditText loginPhoneNumEditText;
    private EditText loginPasswordEditText;
    private String uid;
    private TextView signUp;
    private Button signIn;
    private FirebaseUser user;

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
        loginPasswordEditText = findViewById(R.id.loginPass);
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
                /*loadingPanel.setVisibility(View.VISIBLE); //For loading screen purposes
                loadingPanel.setClickable(true);

                if(!validateLoginInfo()) {
                    loadingPanel.setVisibility(View.INVISIBLE);
                    return;
                }

                firebaseAuth.signInWithEmailAndPassword(loginPhoneNumEditText.getText().toString(),
                    loginPasswordEditText.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                login();
                            } else {
                                Toast.makeText(LoginActivity.this, R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                            }
                            loadingPanel.setVisibility(View.GONE);
                        }
                    });*/
                Intent intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mUser = FirebaseDatabase.getInstance().getReference().child("users");
        mMerchant = FirebaseDatabase.getInstance().getReference().child("merchants");
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

    private void login() {
        //Check if User or Merchant
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
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        case "MERCHANT":
                            mMerchant.child(currentUser).child("device_token").setValue(deviceToken);
                            intent = new Intent(LoginActivity.this, MerchantMainActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        case "UNVERIFIED_MERCHANT":
                            AlertDialog alertDialog = new AlertDialog.Builder(
                                    LoginActivity.this).create();
                            alertDialog.setTitle("Merchant not verified!");
                            alertDialog.setMessage("Please wait for our admin to verify your account");
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
                            intent = new Intent(LoginActivity.this, AdminMainActivity.class);
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
        boolean valid = true;

        String username = loginPhoneNumEditText.getText().toString();
        if(TextUtils.isEmpty(username)) {
            loginPhoneNumEditText.setError(getString(R.string.requires_phonenum));
            valid = false;
        } else {
            loginPhoneNumEditText.setError(null);
        }

        String password = loginPasswordEditText.getText().toString();
        if(TextUtils.isEmpty(password)) {
            loginPasswordEditText.setError(getString(R.string.requires_password));
            valid = false;
        } else {
            loginPasswordEditText.setError(null);
        }

        return valid;
    }
}
