package com.example.asus.cashbuddy.Activity.Admin;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.asus.cashbuddy.Fragment.Admin.PhoneNumTopUpFragment;
import com.example.asus.cashbuddy.Fragment.Admin.QRTopUpFragment;
import com.example.asus.cashbuddy.R;

public class AdminTopUpActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_top_up);

        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(AdminTopUpActivity.this, R.array.top_up_method, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //Default Main Menu
        final FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new QRTopUpFragment()).commit();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String choose = parent.getItemAtPosition(position).toString();

                if(choose.equals("QR")){
                    manager.beginTransaction().replace(R.id.content, new QRTopUpFragment()).commit();
                }else if(choose.equals("Telephone Number")){
                    manager.beginTransaction().replace(R.id.content, new PhoneNumTopUpFragment()).commit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
