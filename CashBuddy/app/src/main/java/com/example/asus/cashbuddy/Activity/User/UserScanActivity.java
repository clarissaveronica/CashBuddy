package com.example.asus.cashbuddy.Activity.User;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.asus.cashbuddy.Model.History;
import com.example.asus.cashbuddy.Model.Transaction;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.HistoryUtil;
import com.example.asus.cashbuddy.Utils.TransactionUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.security.MessageDigest;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class UserScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseMerchant, databaseUser, walletMRef, walletURef, notification;
    private FirebaseUser user;
    private int transactionAmount, userBalance, merchantBalance;
    private String amount, merchantName, userName;
    private String result;
    private PinEntryEditText securitycode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);

        //Custom Action Bar's Title
        getSupportActionBar().setTitle("Scan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        databaseUser = FirebaseDatabase.getInstance().getReference("users");
        databaseMerchant = FirebaseDatabase.getInstance().getReference("merchant");
        notification = FirebaseDatabase.getInstance().getReference("notifications");
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(final Result rawResult) {
        result = rawResult.getText();

        if(!Patterns.WEB_URL.matcher(result).matches()) {
            getInfo(new OnGetDataListener() {
                @Override
                public void onSuccess() {
                    if (userBalance >= transactionAmount) {
                        showInputSC();
                    } else {
                        showError();
                    }
                }

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                    showInvalidError();
                }
            });
        }else showInvalidError();
    }

    public void showInvalidError(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Invalid QR code. Please try again")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mScannerView.resumeCameraPreview(UserScanActivity.this);
                    }
                });

        AlertDialog alert = builder.create();
        alert.setTitle("Oops!");
        alert.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void showError(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Insufficient funds. Please top up to proceed with the transaction")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

        AlertDialog alert = builder.create();
        alert.setTitle("Oops!");
        alert.show();
    }

    public void getInfo(final OnGetDataListener listener){
        listener.onStart();

        databaseMerchant.child(result).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    transactionAmount = Integer.parseInt(dataSnapshot.child("price").getValue().toString());
                    amount = changeToRupiahFormat(transactionAmount);
                    merchantName = dataSnapshot.child("merchantName").getValue().toString();
                    merchantBalance = Integer.parseInt(dataSnapshot.child("balance").getValue().toString());

                    if(transactionAmount != 0) {
                        databaseUser.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    userBalance = Integer.parseInt(dataSnapshot.child("balance").getValue().toString());
                                    userName = dataSnapshot.child("name").getValue().toString();
                                    listener.onSuccess();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }else listener.onFailure();
                }else{
                    listener.onFailure();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void makeTransaction(){
        Transaction transaction = new Transaction(result, user.getUid(), transactionAmount);
        TransactionUtil.insert(transaction);
    }

    private void setWallet(){
        //Set balance on user's wallet
        walletURef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("balance");;
        walletURef.setValue(userBalance - transactionAmount);

        //Set balance on merchant's wallet
        walletMRef = FirebaseDatabase.getInstance().getReference("merchant").child(result).child("balance");
        walletMRef.setValue(merchantBalance + transactionAmount);
    }

    public String changeToRupiahFormat(int money){
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        String temp = formatRupiah.format((double)money);

        return temp;
    }

    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess();
        void onStart();
        void onFailure();
    }

    public void showInputSC(){
        final AlertDialog builder = new AlertDialog.Builder(UserScanActivity.this)
                .setTitle("You are making a " + amount +  " transaction with " + merchantName)
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
                                Toast.makeText(getApplicationContext(), "Yay! Your transaction is completed!", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(getApplicationContext(), "Wrong security code", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                negative.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        mScannerView.resumeCameraPreview(UserScanActivity.this);
                        builder.dismiss();
                    }
                });
            }
        });
        builder.show();
    }

    public void verify(final OnGetDataListener listener){
        databaseUser.child(user.getUid()).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String password = snapshot.getValue(String.class);
                if(hash(securitycode.getText().toString()).equals(password)){
                    //Set history for user
                    History history = new History("Purchase", merchantName, transactionAmount);
                    HistoryUtil.insert(history, user.getUid());

                    //Set history for merchant
                    History history2 = new History("Successful Transaction", userName, transactionAmount);
                    HistoryUtil.insert(history2, result);

                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("amount", changeToRupiahFormat(transactionAmount));
                    notificationData.put("type", "purchase");
                    notification.child("purchase").child(result).push().setValue(notificationData);

                    makeTransaction();
                    setWallet();
                    listener.onSuccess();
                }else listener.onFailure();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public String hash (String pass){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pass.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
