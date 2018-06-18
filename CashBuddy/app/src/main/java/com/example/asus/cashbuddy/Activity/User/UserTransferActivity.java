package com.example.asus.cashbuddy.Activity.User;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.asus.cashbuddy.Fragment.All.QRScanFragment;
import com.example.asus.cashbuddy.Fragment.User.UserPhoneTransferFragment;
import com.example.asus.cashbuddy.R;

public class UserTransferActivity extends AppCompatActivity {

    FrameLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_transfer);
        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.transferText);

        //Initialize view
        final FragmentManager manager = getSupportFragmentManager();
        content = findViewById(R.id.content);

        //Initialize spinner
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(UserTransferActivity.this, R.array.methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String choose = parent.getItemAtPosition(position).toString();

                if(choose.equals("Scan QR")){
                    content.setVisibility(View.VISIBLE);
                    manager.beginTransaction().replace(R.id.content, new QRScanFragment()).commit();
                }else if(choose.equals("Telephone Number")){
                    content.setVisibility(View.VISIBLE);
                    manager.beginTransaction().replace(R.id.content, new UserPhoneTransferFragment()).commit();
                }else if(choose.equals("Select Method")){
                    content.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
