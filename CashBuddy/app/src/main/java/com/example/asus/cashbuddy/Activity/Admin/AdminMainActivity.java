package com.example.asus.cashbuddy.Activity.Admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.asus.cashbuddy.Fragment.Admin.AdminHistoryFragment;
import com.example.asus.cashbuddy.Fragment.Admin.AdminHomeFragment;
import com.example.asus.cashbuddy.Fragment.All.NotificationFragment;
import com.example.asus.cashbuddy.Others.BottomNavigationViewHelper;
import com.example.asus.cashbuddy.R;

public class AdminMainActivity extends AppCompatActivity {

    private static final int MENU_LOGOUT = Menu.FIRST;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //Bottom Navigation Options
            FragmentManager manager = getSupportFragmentManager();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    manager.beginTransaction().replace(R.id.content, new AdminHomeFragment()).commit();
                    return true;
                case R.id.navigation_history:
                    manager.beginTransaction().replace(R.id.content, new AdminHistoryFragment()).commit();
                    return true;
                case R.id.navigation_notifications:
                    manager.beginTransaction().replace(R.id.content, new NotificationFragment()).commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        //Initialize Bottom Navigation
        BottomNavigationView navigation = findViewById(R.id.navigation_admin);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.removeShiftMode(navigation);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.app_name);
        textViewTitle.setGravity(1);

        //Default Main Menu
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new AdminHomeFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_LOGOUT, 0, "Log Out");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_LOGOUT:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.signOut_confirmation)
                        .setCancelable(false)
                        .setPositiveButton(R.string.signOut_confirm, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //firebaseAuth.getInstance().signOut();
                                //Intent intent = new Intent(MainActivityAdmin.this, LoginActivity.class);
                                //startActivity(intent);
                                //finish();
                            }
                        })
                        .setNegativeButton(R.string.signOut_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.setTitle(R.string.signOut_title);
                alert.show();
                return true;
        }
        return false;
    }
}
