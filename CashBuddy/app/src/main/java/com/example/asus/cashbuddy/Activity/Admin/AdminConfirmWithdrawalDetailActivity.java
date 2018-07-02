package com.example.asus.cashbuddy.Activity.Admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.asus.cashbuddy.Model.History;
import com.example.asus.cashbuddy.Model.Withdraw;
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


public class AdminConfirmWithdrawalDetailActivity extends AppCompatActivity {

    private TextView transactiondate, username, bankname, amount, transfername, accountnumber;
    private Button accept, decline;
    private Withdraw withdraw;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference, databaseUser, notification, walletRef, ref;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Intent intent;
    private int pos, balance;
    private PinEntryEditText securitycode;
    private boolean isAccepted;
    private ArrayList<Withdraw> withdrawHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_confirm_withdrawal_detail);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.merchantWithdrawalTitle);

        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("withdrawrequest");
        notification = FirebaseDatabase.getInstance().getReference("notifications");
        intent = getIntent();
        pos = intent.getIntExtra("Position",0);
        withdrawHistory = (ArrayList<Withdraw>) intent.getSerializableExtra("withdraw");

        withdraw = withdrawHistory.get(pos);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        databaseUser = firebaseDatabase.getReference("users");
        ref = firebaseDatabase.getReference(withdraw.getRole());

        transactiondate = findViewById(R.id.withdrawDate);
        username = findViewById(R.id.withdrawReqName);
        bankname = findViewById(R.id.bank_name);
        transfername = findViewById(R.id.trans_name);
        amount = findViewById(R.id.withdraw_detail_amount);
        accept = findViewById(R.id.accept_request);
        decline = findViewById(R.id.decline_request);
        accountnumber = findViewById(R.id.request_number);

        transactiondate.setText(withdraw.getRequestDateString(withdraw.getRequestdate()));
        transfername.setText(withdraw.getTransfername());
        bankname.setText(withdraw.getBank());
        accountnumber.setText(withdraw.getAccountnumber());
        amount.setText(changeToRupiahFormat(withdraw.getAmount()));

        FirebaseDatabase.getInstance().getReference(withdraw.getRole()).child(withdraw.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(withdraw.getRole().equals("merchant")) {
                    username.setText(dataSnapshot.child("merchantName").getValue().toString());
                }else username.setText(dataSnapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });


        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAccepted = true;
                showInputSC();
            }
        });

        decline.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                getInfo();
                isAccepted = false;
                showInputSC();
            }
        });
    }

    public void updateReq(){
        DatabaseReference withdrawDatabase= FirebaseDatabase.getInstance().getReference();

        withdrawDatabase.child("withdrawrequest").orderByChild("uid").equalTo(withdraw.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot productSnapshot: dataSnapshot.getChildren()) {
                    if (productSnapshot.child("requestdate").getValue().equals(withdraw.getRequestdate())) {
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
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void getInfo(){
        ref.child(withdraw.getUid()).child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    balance = Integer.parseInt(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showInputSC(){
        final AlertDialog builder = new AlertDialog.Builder(this)
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
        databaseUser.child(user.getUid()).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String password = snapshot.getValue(String.class);
                if(hash(securitycode.getText().toString()).equals(password)){
                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("amount", changeToRupiahFormat(withdraw.getAmount()));
                    if(isAccepted) {
                        notificationData.put("type", "acceptWithdraw");
                        notification.child("acceptWithdraw").child(withdraw.getRole()).child(withdraw.getUid()).push().setValue(notificationData);

                        updateReq();
                        listener.onSuccess();
                    }else{
                        notificationData.put("type", "declineWithdraw");
                        notification.child("declineWithdraw").child(withdraw.getRole()).child(withdraw.getUid()).push().setValue(notificationData);

                        History history = new History("Withdraw Request Rejected", "CB Cash", withdraw.getAmount());
                        HistoryUtil.insert(history, withdraw.getUid());

                        updateReq();
                        setWallet();
                        listener.onSuccess();
                    }
                }else listener.onFailure();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setWallet(){
        //Set balance on user's wallet
        walletRef = FirebaseDatabase.getInstance().getReference(withdraw.getRole()).child(withdraw.getUid()).child("balance");
        walletRef.setValue(balance + withdraw.getAmount());
    }

    //Change number format to IDR
    public String changeToRupiahFormat(int money){
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        String temp = formatRupiah.format((double)money);

        return temp;
    }

    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess();
        void onStart();
        void onFailure();
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
