package com.example.asus.cashbuddy.Activity.User;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.asus.cashbuddy.Model.History;
import com.example.asus.cashbuddy.Model.PaymentRequest;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.HistoryUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class UserReceivedPaymentDetailActivity extends AppCompatActivity {

    private TextView amount, sentBy, type, date;
    private Button accept, decline;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference walletSender, walletReceiver, notification, reference;
    private PaymentRequest paymentRequest;
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private int userBalance, senderBalance;
    private Intent intent;
    private String userName;
    private boolean isAccepted;
    private ArrayList<PaymentRequest> paymentRequestArrayList;
    private PinEntryEditText securitycode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_received_payment_detail);
        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.receivedReqTitle);

        intent = getIntent();
        int pos = intent.getIntExtra("Position",0);
        paymentRequestArrayList = (ArrayList<PaymentRequest>) intent.getSerializableExtra("paymentReq");
        paymentRequest = paymentRequestArrayList.get(pos);

        //Get data firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        notification = FirebaseDatabase.getInstance().getReference("notifications");
        reference = FirebaseDatabase.getInstance().getReference("paymentrequest");

        //Initialize views
        amount = findViewById(R.id.amount);
        sentBy = findViewById(R.id.name);
        type = findViewById(R.id.type);
        date = findViewById(R.id.date);
        accept = findViewById(R.id.accept_request);
        decline = findViewById(R.id.decline_request);

        //Set views
        amount.setText(changeToRupiahFormat(paymentRequest.getAmount()));
        type.setText(paymentRequest.getType());
        date.setText(paymentRequest.getRequestDateString(paymentRequest.getRequestdate()));

        FirebaseDatabase.getInstance().getReference(paymentRequest.getFrom()).child(paymentRequest.getSenderRequest()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(paymentRequest.getFrom().equals("merchant")) {
                    sentBy.setText(dataSnapshot.child("merchantName").getValue().toString());
                }else sentBy.setText(dataSnapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        accept.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                getInfo(new OnGetDataListener() {
                    @Override
                    public void onSuccess() {
                        if (userBalance >= paymentRequest.getAmount()) {
                            isAccepted = true;
                            showInputSC();
                        }else {
                            showError();
                        }
                    }

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onFailure() {
                    }
                });

            }
        });

        decline.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                isAccepted = false;
                showInputSC();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    //Change number format to IDR
    public String changeToRupiahFormat(int money){
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        String temp = formatRupiah.format((double)money);

        return temp;
    }

    public void showError(){
        AlertDialog.Builder builder = new AlertDialog.Builder(UserReceivedPaymentDetailActivity.this);
        builder.setMessage("Insufficient funds. Please top up to proceed.")
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

    private void setWallet(){
        //Set balance on user's wallet
        walletReceiver = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("balance");;
        walletReceiver.setValue(userBalance - paymentRequest.getAmount());

        //Set balance on sender's wallet
        walletSender = FirebaseDatabase.getInstance().getReference(paymentRequest.getFrom()).child(paymentRequest.getSenderRequest()).child("balance");
        walletSender.setValue(senderBalance + paymentRequest.getAmount());
    }

    public void getInfo(final OnGetDataListener listener){
        listener.onStart();

        firebaseDatabase.getReference().child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    userBalance = Integer.parseInt(dataSnapshot.child("balance").getValue().toString());
                    userName = dataSnapshot.child("name").getValue().toString();

                    firebaseDatabase.getReference().child(paymentRequest.getFrom()).child(paymentRequest.getSenderRequest()).child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                senderBalance = Integer.parseInt(dataSnapshot.getValue().toString());
                                listener.onSuccess();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showInputSC(){
        final AlertDialog builder = new AlertDialog.Builder(UserReceivedPaymentDetailActivity.this)
                .setTitle("Confirmation")
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
                                if(isAccepted) {
                                    Toast.makeText(getApplicationContext(), "Request accepted", Toast.LENGTH_SHORT).show();
                                }else Toast.makeText(getApplicationContext(), "Request declined", Toast.LENGTH_SHORT).show();
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
        firebaseDatabase.getReference().child("users").child(user.getUid()).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String password = snapshot.getValue(String.class);
                if(hash(securitycode.getText().toString()).equals(password)){
                    if(isAccepted) {
                        //Set history for user
                        History history = new History("Payment Request", sentBy.getText().toString(), paymentRequest.getAmount());
                        HistoryUtil.insert(history, user.getUid());

                        //Set history for request's sender
                        History history2 = new History("Payment Request Accepted", userName, paymentRequest.getAmount());
                        HistoryUtil.insert(history2, paymentRequest.getSenderRequest());

                        HashMap<String, String> notificationData = new HashMap<>();
                        notificationData.put("amount", amount.getText().toString());
                        notificationData.put("type", "paymentReq");
                        notification.child("requestPayment").child("accepted").child(paymentRequest.getFrom()).child(paymentRequest.getSenderRequest()).push().setValue(notificationData);

                        updateReq();
                        setWallet();
                        listener.onSuccess();
                    }else{
                        HashMap<String, String> notificationData = new HashMap<>();
                        notificationData.put("amount", amount.getText().toString());
                        notificationData.put("type", "paymentReq");
                        notification.child("requestPayment").child("rejected").child(paymentRequest.getFrom()).child(paymentRequest.getSenderRequest()).push().setValue(notificationData);
                        updateReq();
                        listener.onSuccess();
                    }
                }else listener.onFailure();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void updateReq(){
        final DatabaseReference paymentReqDatabase= FirebaseDatabase.getInstance().getReference();

        paymentReqDatabase.child("paymentrequest").orderByChild("senderRequest").equalTo(paymentRequest.getSenderRequest()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot productSnapshot: dataSnapshot.getChildren()) {
                    if (productSnapshot.child("requestdate").getValue().equals(paymentRequest.getRequestdate())) {
                        HashMap<String, Object> result = new HashMap<>();
                        if(isAccepted) {
                            result.put("requeststatus", 1);
                            reference.child(productSnapshot.getKey()).updateChildren(result);
                        }else{
                            result.put("requeststatus", 2);
                            reference.child(productSnapshot.getKey()).updateChildren(result);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
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

    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess();
        void onStart();
        void onFailure();
    }
}
