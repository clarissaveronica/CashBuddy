package com.example.asus.cashbuddy.Activity.User;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.asus.cashbuddy.Model.PaymentRequest;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.PaymentRequestUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

public class UserRequestPaymentActivity extends AppCompatActivity {

    private TextInputEditText amountRequested, phoneNum;
    private Button submit;
    private int newPrice;
    private String receiver, receiverName, receiverPhone;
    private boolean valid;
    private PinEntryEditText securitycode;
    private DatabaseReference databaseUser, phoneRef, notification;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_request_payment);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.makePaymentRequestTitle);

        //Initialize view
        amountRequested = findViewById(R.id.amountTransfer);
        phoneNum = findViewById(R.id.phoneNum);
        submit = findViewById(R.id.submitButton);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        databaseUser = FirebaseDatabase.getInstance().getReference("users");
        phoneRef = FirebaseDatabase.getInstance().getReference("phonenumbertouid");
        notification = FirebaseDatabase.getInstance().getReference("notifications");

        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(validateInfo()){
                    checkNum(new OnGetDataListener() {
                        @Override
                        public void onSuccess() {
                            getInfo(new OnGetDataListener() {
                                @Override
                                public void onSuccess() {
                                    showInputSC();
                                }

                                @Override
                                public void onStart() {
                                }

                                @Override
                                public void onFailure() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(UserRequestPaymentActivity.this);
                                    builder.setMessage("Unregistered phone number. Please try again")
                                            .setCancelable(false)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                                    AlertDialog alert = builder.create();
                                    alert.setTitle("Oops!");
                                    alert.show();
                                }
                            });
                        }

                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onFailure() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(UserRequestPaymentActivity.this);
                            builder.setMessage("Wrong phone number. Please try again")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                            AlertDialog alert = builder.create();
                            alert.setTitle("Oops!");
                            alert.show();
                        }
                    });
                }
            }
        });

        amountRequested.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                amountRequested.removeTextChangedListener(this);

                try {
                    String originalString = editable.toString();

                    Long longval;
                    if (originalString.contains(",")) {
                        originalString = originalString.replaceAll(",", "");
                    }
                    longval = Long.parseLong(originalString);

                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    formatter.applyPattern("#,###,###,###");
                    String formattedString = formatter.format(longval);

                    //setting text after format to EditText
                    amountRequested.setText(formattedString);
                    amountRequested.setSelection(amountRequested.getText().length());
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
                amountRequested.addTextChangedListener(this);
            }
        });
    }

    public boolean validateInfo(){
        String phone = phoneNum.getText().toString();
        newPrice = Integer.parseInt(amountRequested.getText().toString().replace(",", ""));
        receiverPhone = changeNum(phoneNum.getText().toString());

        if(TextUtils.isEmpty(phone)) {
            phoneNum.setError("Phone number is required");
            valid = false;
        }else if(phone.length() < 10){
            phoneNum.setError("Invalid phone number");
            valid = false;
        }else if(!phone.substring(0,1).equals("0")){
            phoneNum.setError("Invalid phone number");
            valid = false;
        }

        if(newPrice < 10000){
            amountRequested.setError("Minimal amount for transfer is Rp 10.000");
            valid = false;
        }

        valid = true;

        return valid;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess();
        void onStart();
        void onFailure();
    }

    //Change phone number format
    public String changeNum(String phone){
        String num = "+62" + phone.substring(1);
        return num;
    }

    public void checkNum(final OnGetDataListener listener){
        phoneRef.child(receiverPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    receiver = dataSnapshot.getValue().toString();
                    listener.onSuccess();
                }else listener.onFailure();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getInfo(final OnGetDataListener listener){
        listener.onStart();

        databaseUser.child(receiver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    receiverName = dataSnapshot.child("name").getValue().toString();
                    listener.onSuccess();
                }else listener.onFailure();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showInputSC(){
        final AlertDialog builder = new AlertDialog.Builder(UserRequestPaymentActivity.this)
                .setTitle("Requesting " + changeToRupiahFormat(newPrice) + " from " + receiverName)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .setMessage("Please enter your security code to proceed")
                .create();

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.input_security_code, null, false);

        securitycode = viewInflated.findViewById(R.id.pinEntry);

        builder.setView(viewInflated);

        builder.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = builder.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negative = builder.getButton(AlertDialog.BUTTON_NEGATIVE);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        verify(new OnGetDataListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getApplicationContext(), "Payment request delivered!", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(getApplicationContext(), "Wrong security code", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                negative.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        builder.dismiss();
                    }
                });
            }
        });
        builder.show();
    }

    public void verify(final OnGetDataListener listener){
        databaseUser.child(user.getUid()).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String password = snapshot.getValue(String.class);
                if(hash(securitycode.getText().toString()).equals(password)){
                    PaymentRequest paymentRequest = new PaymentRequest(receiver, user.getUid(), newPrice, "users");
                    PaymentRequestUtil.insert(paymentRequest);

                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("amount", changeToRupiahFormat(newPrice));
                    notificationData.put("type", "paymentReq");
                    notification.child("requestPayment").child(receiver).push().setValue(notificationData);

                    listener.onSuccess();
                }else listener.onFailure();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //Change number format to IDR
    public String changeToRupiahFormat(int money){
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        String temp = formatRupiah.format((double)money);

        return temp;
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

