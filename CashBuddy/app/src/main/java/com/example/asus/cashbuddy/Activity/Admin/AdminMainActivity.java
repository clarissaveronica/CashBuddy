package com.example.asus.cashbuddy.Activity.Admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.asus.cashbuddy.Activity.All.LoginActivity;
import com.example.asus.cashbuddy.Adapter.AdminMenuAdapter;
import com.example.asus.cashbuddy.R;
import com.google.firebase.auth.FirebaseAuth;

public class AdminMainActivity extends AppCompatActivity {

    private static final int MENU_LOGOUT = Menu.FIRST;

    String[] name = {"User Top Up","Confirm Top Up", "Merchant Withdrawal","Merchant Verification"};

    String[] description = {"Add user's balance","Confirm user's top up request",
            "Confirm merchant's request for balance withdrawal", "Verify unverified merchant"};

    String[] TAG = {"TOP_UP","C_TOP_UP","WITHDRAW", "MERCHANT_VERIFICATION"};

    ListView lView;
    ListAdapter lAdapter;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.app_name);
        textViewTitle.setGravity(1);

        lView = findViewById(R.id.list);

        lAdapter = new AdminMenuAdapter(AdminMainActivity.this, name, description, TAG);

        lView.setAdapter(lAdapter);

        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent;
                switch (TAG[i]){
                    case "TOP_UP":
                        intent = new Intent(AdminMainActivity.this, AdminTopUpActivity.class);
                        startActivity(intent);
                        break;
                    case "WITHDRAW":
                        intent = new Intent(AdminMainActivity. this, AdminMerchantWithdrawActivity.class);
                        startActivity(intent);
                        break;
                    case "C_TOP_UP":
                        intent = new Intent(AdminMainActivity. this, AdminConfirmTopUpActivity.class);
                        startActivity(intent);
                        break;
                    case "MERCHANT_VERIFICATION":
                        intent = new Intent(AdminMainActivity. this, AdminMerchantVerificationActivity.class);
                        startActivity(intent);
                        break;

                    default: break;
                }

            }
        });
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
                                firebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(AdminMainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finishAffinity();
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
