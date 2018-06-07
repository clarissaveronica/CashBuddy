package com.example.asus.cashbuddy.Activity.All;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.asus.cashbuddy.Activity.Merchant.RegisterMerchantActivity;
import com.example.asus.cashbuddy.Activity.User.RegisterUserActivity;
import com.example.asus.cashbuddy.R;

public class RegisterMainActivity extends AppCompatActivity {

    private Button merchantSignUp, userSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_main);

        getSupportActionBar().hide();

        merchantSignUp = findViewById(R.id.RegisterStoreBtn);
        userSignUp = findViewById(R.id.RegisterUserBtn);

        merchantSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterMainActivity.this, RegisterMerchantActivity.class);
                intent.putExtra("role", "merchant");
                startActivity(intent);
                finish();
            }
        });

        userSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterMainActivity.this, RegisterUserActivity.class);
                intent.putExtra("role", "user");
                startActivity(intent);
                finish();
            }
        });
    }
}
