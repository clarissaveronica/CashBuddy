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
import com.example.asus.cashbuddy.Fragment.All.NotificationFragment;
import com.example.asus.cashbuddy.Fragment.User.UserProfileFragment;
import com.example.asus.cashbuddy.Others.BottomNavigationViewHelper;
import com.example.asus.cashbuddy.R;

public class MerchantMainActivity extends AppCompatActivity {

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
                case R.id.navigation_notifications:
                    manager.beginTransaction().replace(R.id.content, new NotificationFragment()).commit();
                    return true;
                case R.id.navigation_profile:
                    manager.beginTransaction().replace(R.id.content, new UserProfileFragment()).commit();
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
    }

}
