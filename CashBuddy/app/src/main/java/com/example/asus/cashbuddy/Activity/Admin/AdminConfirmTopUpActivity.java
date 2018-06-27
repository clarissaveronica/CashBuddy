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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminConfirmTopUpActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<TopUp> topups;
    private AdminConfirmTopUpAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_confirm_top_up);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.confirmTopUpTitle);

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
        adapter.notifyDataSetChanged();
        attachDatabaseReadListener();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void attachDatabaseReadListener() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("topuprequest");

        Query query = ref.orderByChild("requeststatus").equalTo(0);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                adapter.removeTopUp();
                for (DataSnapshot child : snapshot.getChildren()) {
                    TopUp topUp = child.getValue(TopUp.class);
                    if(topUp.getRequeststatus()==0) {
                        adapter.addTopUp(topUp);
                    }
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
