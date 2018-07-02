package com.example.asus.cashbuddy.Activity.User;

import android.app.Activity;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.asus.cashbuddy.Fragment.User.UserBankTransferFragment;
import com.example.asus.cashbuddy.Fragment.User.UserPhoneTransferFragment;
import com.example.asus.cashbuddy.Fragment.User.UserTransferQRScanFragment;
import com.example.asus.cashbuddy.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class UserTransferActivity extends AppCompatActivity {

    private FrameLayout content;
    private TextInputEditText amountTransfer;
    private String choose;
    private String newPrice = "0";
    private Bundle bundle;
    private Spinner spinner;

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
        amountTransfer = findViewById(R.id.amountTransfer);

        //Initialize spinner
        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(UserTransferActivity.this, R.array.transfer_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        bundle = new Bundle();

        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(amountTransfer.hasFocus()){
                    amountTransfer.clearFocus();
                }
                InputMethodManager inputMethodManager =(InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                return false;
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                choose = parent.getItemAtPosition(position).toString();

                if(choose.equals("Scan QR")){
                    if(Integer.parseInt(newPrice) < 10000){
                        amountTransfer.setError("Minimal amount for transfer is Rp 10.000");
                        spinner.setSelection(0);
                    }else {
                        UserTransferQRScanFragment myObj = new UserTransferQRScanFragment();
                        bundle.putString("amount", newPrice);
                        myObj.setArguments(bundle);

                        content.setVisibility(View.VISIBLE);
                        manager.beginTransaction().replace(R.id.content, myObj).commit();
                    }
                }else if(choose.equals("Telephone Number")){
                    if(Integer.parseInt(newPrice) < 10000){
                        amountTransfer.setError("Minimal amount for transfer is Rp 10.000");
                        spinner.setSelection(0);
                    }else {
                        UserPhoneTransferFragment myObj = new UserPhoneTransferFragment();
                        bundle.putString("amount", newPrice);
                        myObj.setArguments(bundle);

                        content.setVisibility(View.VISIBLE);
                        manager.beginTransaction().replace(R.id.content, myObj).commit();
                    }
                }else if(choose.equals("Bank Account")){
                    if(Integer.parseInt(newPrice) < 10000){
                        amountTransfer.setError("Minimal amount for transfer is Rp 10.000");
                        spinner.setSelection(0);
                    }else {
                        UserBankTransferFragment myObj = new UserBankTransferFragment();
                        bundle.putString("amount", newPrice);
                        myObj.setArguments(bundle);

                        content.setVisibility(View.VISIBLE);
                        manager.beginTransaction().replace(R.id.content, myObj).commit();
                    }
                }
                else if(choose.equals("Select Method")){
                    content.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        amountTransfer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                amountTransfer.removeTextChangedListener(this);

                try {
                    String originalString = editable.toString();

                    Long longval;
                    if (originalString.contains(",")) {
                        originalString = originalString.replaceAll(",", "");
                    }
                    longval = Long.parseLong(originalString);

                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    formatter.applyPattern("#,###,###,###");
                    String formattedString = formatter.format(longval);

                    //setting text after format to EditText
                    amountTransfer.setText(formattedString);
                    amountTransfer.setSelection(amountTransfer.getText().length());
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
                amountTransfer.addTextChangedListener(this);

                newPrice = amountTransfer.getText().toString().replace(",", "");

                bundle.putString("amount", newPrice);

                if(newPrice.isEmpty() || Integer.parseInt(newPrice) < 10000){
                    amountTransfer.setError("Minimal amount for transfer is Rp 10.000");
                    spinner.setSelection(0);
                }else {
                    if (choose.equals("Scan QR")) {
                        UserTransferQRScanFragment myObj = new UserTransferQRScanFragment();
                        myObj.setArguments(bundle);
                    } else if (choose.equals("Telephone Number")) {
                        UserPhoneTransferFragment myObj = new UserPhoneTransferFragment();
                        myObj.setArguments(bundle);
                    }else if(choose.equals("Bank Account")){
                        UserBankTransferFragment myObj = new UserBankTransferFragment();
                        myObj.setArguments(bundle);
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
