package com.example.asus.cashbuddy.Activity.User;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.asus.cashbuddy.Model.Deals;
import com.example.asus.cashbuddy.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class UserDealsDetailActivity extends AppCompatActivity {

    private Intent intent;
    private int pos;
    private ArrayList<Deals> dealList;
    private Deals deals;
    private ImageView pic;
    private TextView titleDeal, desc, date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_deals_detail);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.dealTitle);

        //Get data firebase
        intent = getIntent();
        pos = intent.getIntExtra("Position",0);
        dealList = (ArrayList<Deals>) intent.getSerializableExtra("deals");

        deals = dealList.get(pos);

        //Initialize views
        pic = findViewById(R.id.dealsPic);
        titleDeal = findViewById(R.id.dealTitle);
        desc = findViewById(R.id.desc);
        date = findViewById(R.id.dealEnd);

        //Set views
        titleDeal.setText(deals.getTitle());
        desc.setText(deals.getDesc());
        date.setText(deals.getDealEnd());

        if(deals.getPicUrl() != null) {
            Glide.with(this)
                    .load(deals.getPicUrl())
                    .into(pic);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
