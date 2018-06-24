package com.example.asus.cashbuddy.Activity.Admin;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.example.asus.cashbuddy.Adapter.AdminConfirmTopUpAdapter;
import com.example.asus.cashbuddy.Model.TopUp;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.TopUpUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class AdminConfirmTopUpActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference emailDatabase;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<TopUp> topups;
    private AdminConfirmTopUpAdapter adapter;
    private ChildEventListener EventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_confirm_top_up);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.confirmTopUpTitle);

        getSupportActionBar().setTitle("Confirm Top Up");

        topups = new ArrayList<TopUp>();
        adapter = new AdminConfirmTopUpAdapter (topups);
        recyclerView = findViewById(R.id.request_recycler_view);
        layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onResume() {
        super.onResume();
        attachDatabaseReadListener();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        detachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (EventListener == null) {
            EventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    TopUp topUp = dataSnapshot.getValue(TopUp.class);
                    adapter.addTopUp(topUp);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {}

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };

            TopUpUtil.query().addChildEventListener(EventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if(topups != null) {
            topups = null;
        }
    }
    @Override
    public void onBackPressed() {
        finish();
    }
}
