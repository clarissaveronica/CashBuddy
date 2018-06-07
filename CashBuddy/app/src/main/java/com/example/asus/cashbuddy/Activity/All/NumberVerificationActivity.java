package com.example.asus.cashbuddy.Activity.All;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.example.asus.cashbuddy.R;

public class NumberVerificationActivity extends AppCompatActivity {

    EditText pin;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_verification);

        Intent intent = getIntent();

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText("Phone Number Verification");

        pin = findViewById(R.id.pin);
        text = findViewById(R.id.textView);

        text.setText("We have sent the verification pin to : " + intent.getStringExtra("number"));

        pin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                EditText text = (EditText) getCurrentFocus();

                if (text.length() == 4)
                {
                   //submit application and verify
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}
