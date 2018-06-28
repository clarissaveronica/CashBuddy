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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.bumptech.glide.Glide;
import com.example.asus.cashbuddy.Model.History;
import com.example.asus.cashbuddy.Model.TopUp;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.HistoryUtil;
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


public class AdminConfirmTopUpDetailActivity extends AppCompatActivity {

    private TextView transactiondate, username, bankname, amount, transfername;
    private ImageView transferproof;
    private Button accept, decline;
    private TopUp topUp;
    private FirebaseUser user;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference, databaseUser, walletRef, notification;
    private Intent intent;
    private int pos, userBalance;
    private PinEntryEditText securitycode;
    private ArrayList<TopUp> topupHistory;
    private boolean isAccepted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_confirm_top_up_detail);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.confirmTopUpTitle);

        //Get data firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("topuprequest");
        notification = FirebaseDatabase.getInstance().getReference("notifications");
        databaseUser = firebaseDatabase.getReference("users");
        intent = getIntent();
        pos = intent.getIntExtra("Position",0);
        topupHistory = (ArrayList<TopUp>) intent.getSerializableExtra("topup");
        user = FirebaseAuth.getInstance().getCurrentUser();

        topUp = topupHistory.get(pos);

        //Initialize views
        transactiondate = findViewById(R.id.topup_detail_topup_date);
        username = findViewById(R.id.request_name);
        bankname = findViewById(R.id.bank_name);
        transfername = findViewById(R.id.trans_name);
        amount = findViewById(R.id.topup_detail_amount);
        accept = findViewById(R.id.accept_request);
        decline = findViewById(R.id.decline_request);
        transferproof = findViewById(R.id.proofdetail);

        //Set views
        transactiondate.setText(topUp.getRequestDateString(topUp.getRequestdate()));
        transfername.setText(topUp.getTransfername());
        bankname.setText(topUp.getBank());
        amount.setText("Top-up Amount : "+ changeToRupiahFormat(topUp.getAmount()));

        if(topUp.getProofpicUrl() != null) {
            Glide.with(transferproof.getContext())
                    .load(topUp.getProofpicUrl())
                    .into(transferproof);
        }

        FirebaseDatabase.getInstance().getReference("users").child(topUp.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username.setText(dataSnapshot.child("name").getValue().toString());
                userBalance = Integer.parseInt(dataSnapshot.child("balance").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        accept.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                isAccepted = true;
                showInputSC();
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

    public void updateReq(){
        DatabaseReference topUpDatabase= FirebaseDatabase.getInstance().getReference();

        topUpDatabase.child("topuprequest").orderByChild("uid").equalTo(topUp.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot productSnapshot: dataSnapshot.getChildren()) {
                    if (productSnapshot.child("requestdate").getValue().equals(topUp.getRequestdate())) {
                        HashMap<String, Object> result = new HashMap<>();
                        if(isAccepted) {
                            HashMap<String,String> notificationData = new HashMap<>();
                            notificationData.put("amount", changeToRupiahFormat(topUp.getAmount()));
                            notificationData.put("type", "successTopup");
                            notification.child("acceptTopUp").child(topUp.getUid()).push().setValue(notificationData);

                            result.put("requeststatus", 1);
                            reference.child(productSnapshot.getKey()).updateChildren(result);
                            setWallet();
                        }else{
                            HashMap<String,String> notificationData = new HashMap<>();
                            notificationData.put("amount", changeToRupiahFormat(topUp.getAmount()));
                            notificationData.put("type", "declineTopup");
                            notification.child("declineTopUp").child(topUp.getUid()).push().setValue(notificationData);

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
                        if (isAccepted) {
                            verify(new OnGetDataListener() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(AdminConfirmTopUpDetailActivity.this, "Request accepted", Toast.LENGTH_LONG).show();
                                    finish();
                                }

                                @Override
                                public void onStart() {
                                }

                                @Override
                                public void onFailure() {
                                    Toast.makeText(AdminConfirmTopUpDetailActivity.this, "Wrong security code", Toast.LENGTH_LONG).show();
                                }
                            });
                        }else{
                            updateReq();
                            Toast.makeText(AdminConfirmTopUpDetailActivity.this, "Request declined", Toast.LENGTH_LONG).show();
                            finish();
                        }
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
                    History history = new History("Top Up", "CB Cash", topUp.getAmount());
                    HistoryUtil.insert(history, topUp.getUid());

                    updateReq();
                    setWallet();
                    listener.onSuccess();
                }else listener.onFailure();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setWallet(){
        //Set balance on user's wallet
        walletRef = FirebaseDatabase.getInstance().getReference("users").child(topUp.getUid()).child("balance");
        walletRef.setValue(userBalance + topUp.getAmount());
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

    @Override
    public void onBackPressed() {
        finish();
    }
}