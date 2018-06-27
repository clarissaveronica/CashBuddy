package com.example.asus.cashbuddy.Activity.Admin;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.example.asus.cashbuddy.Adapter.AdminMerchantVerificationAdapter;
import com.example.asus.cashbuddy.Model.Merchant;
import com.example.asus.cashbuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminMerchantVerificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Merchant> merchants;
    private AdminMerchantVerificationAdapter adapter;
    private ChildEventListener EventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_merchant_verification);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.verifyMerchantTitle);

        merchants = new ArrayList<Merchant>();
        adapter = new AdminMerchantVerificationAdapter(merchants);
        recyclerView = findViewById(R.id.verifyMerchant_recycler);
        layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        attachDatabaseReadListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        //detachDatabaseReadListener();
    }
    private void attachDatabaseReadListener() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("role");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                adapter.removeMerchants();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String merchantString = child.getValue().toString();
                    if(merchantString.equals("UNVERIFIED_MERCHANT")) adapter.addMerchant(child.getKey());
                }
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
}
