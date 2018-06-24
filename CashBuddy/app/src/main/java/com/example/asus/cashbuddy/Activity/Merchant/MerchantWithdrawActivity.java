package com.example.asus.cashbuddy.Activity.Merchant;

import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.asus.cashbuddy.Model.Transfer;
import com.example.asus.cashbuddy.Model.Withdraw;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.TransferUtil;
import com.example.asus.cashbuddy.Utils.WithdrawUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class MerchantWithdrawActivity extends AppCompatActivity {

    private TextInputEditText withdrawAmount, merchantName, bankName, bankNum;
    private Button withdraw;
    private DatabaseReference databaseMerchant, walletRef;
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private int merchantBalance, withdrawBalance;
    private PinEntryEditText securitycode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_withdraw);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText("Withdraw Money");

        //Get data from firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        databaseMerchant = FirebaseDatabase.getInstance().getReference("merchant");

        //Initialize views
        withdrawAmount = findViewById(R.id.amountWithdraw);
        bankName = findViewById(R.id.bankName);
        merchantName = findViewById(R.id.userName);
        bankNum = findViewById(R.id.bankNumber);
        withdraw = findViewById(R.id.submitButton);

        withdrawAmount.addTextChangedListener(generalTextWatcher);

        withdraw.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(validateForm()){
                    getInfo(new OnGetDataListener() {
                        @Override
                        public void onSuccess() {
                            if (merchantBalance >= withdrawBalance) {
                                showInputSC();
                            }else {
                                showError();
                            }
                        }

                        @Override
                        public void onStart() {
                        }

                        @Override
                        public void onFailure() {
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public boolean validateForm(){
        boolean valid = true;

        String money = withdrawAmount.getText().toString();
        String bankname = bankName.getText().toString();
        String banknumber = bankNum.getText().toString();
        String merchantname = merchantName.getText().toString();

        if(TextUtils.isEmpty(money)) {
            withdrawAmount.setError("Minimal amount for withdraw is Rp10.000");
            valid = false;
        }else if(withdrawBalance < 10000){
            withdrawAmount.setError("Minimal amount for withdraw is Rp10.000");
            valid = false;
        }

        if(TextUtils.isEmpty(bankname)) {
            bankName.setError("Bank name is required");
            valid = false;
        }

        if(TextUtils.isEmpty(banknumber)) {
            bankNum.setError("Bank number is required");
            valid = false;
        }

        if(TextUtils.isEmpty(merchantname)) {
            merchantName.setError("Merchant name is required");
            valid = false;
        }

        return valid;
    }

    public void showInputSC(){
        final AlertDialog builder = new AlertDialog.Builder(MerchantWithdrawActivity.this)
            .setTitle("Withdrawing " + changeToRupiahFormat(withdrawBalance))
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .setMessage("Please enter your security code to proceed")
            .create();

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.input_security_code, null, false);

        securitycode = viewInflated.findViewById(R.id.pinEntry);

        builder.setView(viewInflated);

        builder.setOnShowListener(new DialogInterface.OnShowListener() {

          @Override
          public void onShow(DialogInterface dialogInterface) {
                  Button button = builder.getButton(AlertDialog.BUTTON_POSITIVE);
                  Button negative = builder.getButton(AlertDialog.BUTTON_NEGATIVE);

                  button.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View view) {
                          verify(new OnGetDataListener() {
                              @Override
                              public void onSuccess() {
                                  Toast.makeText(MerchantWithdrawActivity.this, "Withdraw request has been successfully sent. Your request will be processed in 1x24 hours", Toast.LENGTH_LONG).show();
                                  finish();
                              }

                              @Override
                              public void onStart() {
                              }

                              @Override
                              public void onFailure() {
                                  Toast.makeText(MerchantWithdrawActivity.this, "Wrong security code", Toast.LENGTH_LONG).show();
                              }
                          });
                      }
                  });

                  negative.setOnClickListener(new View.OnClickListener(){
                      @Override
                      public void onClick(View view){
                          builder.dismiss();
                      }
                  });
              }
        });
        builder.show();
    }

    public void getInfo(final OnGetDataListener listener){
        listener.onStart();

        databaseMerchant.child(user.getUid()).child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    merchantBalance = Integer.parseInt(dataSnapshot.getValue().toString());
                    listener.onSuccess();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setWallet(){
        //Set balance on merchant's wallet
        walletRef = FirebaseDatabase.getInstance().getReference("merchant").child(user.getUid()).child("balance");
        walletRef.setValue(merchantBalance - withdrawBalance);
    }

    public void verify(final OnGetDataListener listener){
        databaseMerchant.child(user.getUid()).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String password = snapshot.getValue(String.class);
                if(securitycode.getText().toString().equals(password)){
                    Withdraw withdraw= new Withdraw(merchantName.getText().toString(), user.getUid(), bankName.getText().toString(), withdrawBalance, bankNum.getText().toString());
                    WithdrawUtil.insert(withdraw);

                    setWallet();

                    listener.onSuccess();
                }else listener.onFailure();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //Change number format to IDR
    public String changeToRupiahFormat(int money){
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        String temp = formatRupiah.format((double)money);

        return temp;
    }

    //Change number InputEditText real time
    private TextWatcher generalTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            withdrawAmount.removeTextChangedListener(this);

            try {
                String originalString = s.toString();

                Long longval;
                if (originalString.contains(",")) {
                    originalString = originalString.replaceAll(",", "");
                }
                longval = Long.parseLong(originalString);

                DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                formatter.applyPattern("#,###,###,###");
                String formattedString = formatter.format(longval);

                //setting text after format to EditText
                withdrawAmount.setText(formattedString);
                withdrawAmount.setSelection(withdrawAmount.getText().length());
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            withdrawAmount.addTextChangedListener(this);

            if(!withdrawAmount.getText().toString().equals("")) {
                withdrawBalance = Integer.parseInt(withdrawAmount.getText().toString().replace(",", ""));
            }
        }
    };

    public void showError(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MerchantWithdrawActivity.this);
        builder.setMessage("Insufficient funds. Please top up to proceed with the transaction")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.setTitle("Oops!");
        alert.show();
    }

    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess();
        void onStart();
        void onFailure();
    }
}
