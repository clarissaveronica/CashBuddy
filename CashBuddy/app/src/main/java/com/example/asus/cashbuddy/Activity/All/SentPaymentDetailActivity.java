package com.example.asus.cashbuddy.Activity.All;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.asus.cashbuddy.Model.PaymentRequest;
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
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class SentPaymentDetailActivity extends AppCompatActivity {

    private TextView amount, sentTo, type, date, status;
    private PaymentRequest paymentRequest;
    private Intent intent;
    private ArrayList<PaymentRequest> paymentRequestArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent_payment_detail);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.sentReqTitle);

        intent = getIntent();
        int pos = intent.getIntExtra("Position",0);
        paymentRequestArrayList = (ArrayList<PaymentRequest>) intent.getSerializableExtra("paymentReq");
        paymentRequest = paymentRequestArrayList.get(pos);

        //Initialize views
        amount = findViewById(R.id.amount);
        sentTo = findViewById(R.id.name);
        type = findViewById(R.id.type);
        date = findViewById(R.id.date);
        status = findViewById(R.id.status);

        //Set views
        amount.setText(changeToRupiahFormat(paymentRequest.getAmount()));
        type.setText(paymentRequest.getType());
        date.setText(paymentRequest.getRequestDateString(paymentRequest.getRequestdate()));
        if(paymentRequest.getRequeststatus() == 2){
            status.setText("Declined");
        }else if(paymentRequest.getRequeststatus() == 1){
            status.setText("Accepted");
        }else if(paymentRequest.getRequeststatus() == 0){
            long remaining = paymentRequest.getRequestdate() + (5 * 24 * 60 * 60 * 1000) - Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime() ;
            long day = (remaining / (60*60*24*1000));

            status.setText("Pending - " + day + " day(s) left");
        }else if(paymentRequest.getRequeststatus() == 3){
            status.setText("Expired");
        }

        FirebaseDatabase.getInstance().getReference("users").child(paymentRequest.getReceiverRequest()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sentTo.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
}
