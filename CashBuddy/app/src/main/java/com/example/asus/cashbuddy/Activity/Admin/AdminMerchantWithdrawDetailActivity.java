package com.example.asus.cashbuddy.Activity.Admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.asus.cashbuddy.Model.Withdraw;
import com.example.asus.cashbuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


public class AdminMerchantWithdrawDetailActivity extends AppCompatActivity {

    private TextView transactiondate, username, bankname, amount, transfername, accountnumber;
    private Button accept;
    private Withdraw withdraw;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference, databaseUser, notification;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Intent intent;
    private int pos;
    private PinEntryEditText securitycode;
    private ArrayList<Withdraw> withdrawHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_merchant_withdraw_detail);

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

        transactiondate = findViewById(R.id.withdrawDate);
        username = findViewById(R.id.withdrawReqName);
        bankname = findViewById(R.id.bank_name);
        transfername = findViewById(R.id.trans_name);
        amount = findViewById(R.id.withdraw_detail_amount);
        accept = findViewById(R.id.accept_request);
        accountnumber = findViewById(R.id.request_number);

        transactiondate.setText(withdraw.getRequestDateString(withdraw.getRequestdate()));
        transfername.setText(withdraw.getTransfername());
        bankname.setText(withdraw.getBank());
        accountnumber.setText(withdraw.getAccountnumber());
        amount.setText("Withdraw Amount : "+ changeToRupiahFormat(withdraw.getAmount()));

        FirebaseDatabase.getInstance().getReference("merchant").child(withdraw.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username.setText(dataSnapshot.child("merchantName").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });


        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        result.put("requeststatus", 1);
                        reference.child(productSnapshot.getKey()).updateChildren(result);
                    }


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
                                Toast.makeText(AdminMerchantWithdrawDetailActivity.this, "Request accepted", Toast.LENGTH_LONG).show();
                                finish();
                            }

                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(AdminMerchantWithdrawDetailActivity.this, "Wrong security code", Toast.LENGTH_LONG).show();
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
                if(securitycode.getText().toString().equals(password)){
                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("amount", changeToRupiahFormat(withdraw.getAmount()));
                    notificationData.put("type", "withdraw");
                    notification.child("withdraw").child(withdraw.getUid()).push().setValue(notificationData);

                    updateReq();
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

    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess();
        void onStart();
        void onFailure();
    }

}
