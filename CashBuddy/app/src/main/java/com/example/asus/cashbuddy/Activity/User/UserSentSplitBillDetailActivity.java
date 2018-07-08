package com.example.asus.cashbuddy.Activity.User;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.asus.cashbuddy.Model.SplitBill;
import com.example.asus.cashbuddy.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class UserSentSplitBillDetailActivity extends AppCompatActivity {

    private TextView amount, receiver, date, status;
    private SplitBill splitBill;
    private Intent intent;
    private ArrayList<SplitBill> splitBillArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sent_split_bill_detail);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.splitBillsDetailTitle);

        intent = getIntent();
        int pos = intent.getIntExtra("Position",0);
        splitBillArrayList = (ArrayList<SplitBill>) intent.getSerializableExtra("splitbills");
        splitBill = splitBillArrayList.get(pos);

        //Initialize views
        amount = findViewById(R.id.amount);
        receiver = findViewById(R.id.name);
        date = findViewById(R.id.date);
        status = findViewById(R.id.status);

        //Set views
        amount.setText(changeToRupiahFormat(splitBill.getAmount()));
        date.setText(splitBill.getRequestDateString(splitBill.getRequestdate()));
        if(splitBill.getRequeststatus() == 2){
            status.setText("Declined");
        }else if(splitBill.getRequeststatus() == 1){
            status.setText("Accepted");
        }else if(splitBill.getRequeststatus() == 0){
            long remaining = splitBill.getRequestdate() + (5 * 24 * 60 * 60 * 1000) - Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime() ;
            long day = (remaining / (60*60*24*1000));

            status.setText("Pending - " + day + " day(s) left");
        }else if(splitBill.getRequestdate() == 3){
            status.setText("Expired");
        }

        FirebaseDatabase.getInstance().getReference("users").child(splitBill.getReceiver()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                receiver.setText(dataSnapshot.getValue().toString());
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
