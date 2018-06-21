package com.example.asus.cashbuddy.Activity.Merchant;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.asus.cashbuddy.Fragment.All.HistoryFragment;
import com.example.asus.cashbuddy.Fragment.Merchant.MerchantHomeFragment;
import com.example.asus.cashbuddy.Fragment.Merchant.MerchantNewTransactionFragment;
import com.example.asus.cashbuddy.Fragment.All.ProfileFragment;
import com.example.asus.cashbuddy.Model.Merchant;
import com.example.asus.cashbuddy.Others.BottomNavigationViewHelper;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.AccountUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MerchantMainActivity extends AppCompatActivity {

    private DatabaseReference merchantReference;
    private ChildEventListener merchantEventListener;
    private FirebaseUser merchant;
    private FirebaseAuth firebaseAuth;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //Bottom Navigation Options
            FragmentManager manager = getSupportFragmentManager();

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    manager.beginTransaction().replace(R.id.content, new MerchantHomeFragment()).commit();
                    return true;
                case R.id.navigation_new_transaction:
                    manager.beginTransaction().replace(R.id.content, new MerchantNewTransactionFragment()).commit();
                    return true;
                case R.id.navigation_history:
                    manager.beginTransaction().replace(R.id.content, new HistoryFragment()).commit();
                    return true;
                case R.id.navigation_profile:
                    manager.beginTransaction().replace(R.id.content, new ProfileFragment()).commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_main);

        //Initialize Bottom Navigation
        BottomNavigationView navigation = findViewById(R.id.navigation_merchant);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.removeShiftMode(navigation);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.app_name);

        //Default Main Menu
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new MerchantHomeFragment()).commit();

        //Get User
        firebaseAuth = FirebaseAuth.getInstance();
        merchant = firebaseAuth.getCurrentUser();

        merchantReference = FirebaseDatabase.getInstance().getReference("merchant");
    }

    @Override
    protected void onStart() {
        super.onStart();
        merchantEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().equals(merchant.getUid())) {
                    Merchant currentMerchant = dataSnapshot.getValue(Merchant.class);
                    AccountUtil.setCurrentAccount(currentMerchant);
                    //populateUserInfo();
                }
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
        if(merchantReference != null) merchantReference.addChildEventListener(merchantEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(merchantReference != null) merchantReference.removeEventListener(merchantEventListener);
    }

}
