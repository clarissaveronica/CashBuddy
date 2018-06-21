package com.example.asus.cashbuddy.Activity.User;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.asus.cashbuddy.Model.Transaction;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.TransactionUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.text.NumberFormat;
import java.util.Locale;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class UserScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databasePrice, databaseMerchant, databaseUser, walletMRef, walletURef;
    private FirebaseUser user;
    private int transactionAmount, userBalance, merchantBalance;
    private String amount, merchantName;
    private String s;

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
        databasePrice = FirebaseDatabase.getInstance().getReference("prices");
        databaseUser = FirebaseDatabase.getInstance().getReference("users");
        databaseMerchant = FirebaseDatabase.getInstance().getReference("merchant");
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
    public void handleResult(final Result rawResult) {
        s = rawResult.getText();


        getInfo(new OnGetDataListener() {
            @Override
            public void onSuccess() {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserScanActivity.this);
                builder.setMessage("You are making a " + amount + " transaction with " + merchantName + ". Click confirm to proceed")
                        .setCancelable(false)
                        .setPositiveButton(R.string.setPrice_confirm, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (userBalance >= transactionAmount) {
                                    makeTransaction();
                                    setWallet();
                                    showSuccess();
                                } else {
                                    dialog.cancel();
                                    showError();
                                }
                            }
                        })
                        .setNegativeButton(R.string.setPrice_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                finish();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.setTitle(R.string.setPrice_title);
                alert.show();
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onFailure() {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserScanActivity.this);
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
        });
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
        AlertDialog.Builder builder = new AlertDialog.Builder(UserScanActivity.this);
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

    public void showSuccess(){
        AlertDialog.Builder builder = new AlertDialog.Builder(UserScanActivity.this);
        builder.setMessage("Your transaction is completed. Thank you for using Cash Buddy!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

        AlertDialog alert = builder.create();
        alert.setTitle("Yay!");
        alert.show();
    }

    public void getInfo(final OnGetDataListener listener){
        listener.onStart();

        databasePrice.child(s).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    transactionAmount = Integer.parseInt(dataSnapshot.getValue().toString());
                    amount = changeToRupiahFormat(transactionAmount);
                }else{
                    listener.onFailure();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseMerchant.child(s).child("merchantName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    merchantName = dataSnapshot.getValue().toString();
                    listener.onSuccess();
                }else{
                    listener.onFailure();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseUser.child(user.getUid()).child("balance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    userBalance = Integer.parseInt(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void makeTransaction(){
        Transaction transaction = new Transaction(s, user.getUid(), transactionAmount);
        TransactionUtil.insert(transaction);

        databaseMerchant.child(s).child("balance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    merchantBalance = Integer.parseInt(dataSnapshot.getValue().toString()) + transactionAmount;
                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseUser.child(user.getUid()).child("balance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    userBalance = Integer.parseInt(dataSnapshot.getValue().toString()) - transactionAmount;
                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setWallet(){
        //Set balance on user's wallet
        walletURef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("balance");;
        walletURef.setValue(userBalance - transactionAmount);

        //Set balance on merchant's wallet
        walletMRef = FirebaseDatabase.getInstance().getReference("merchant").child(s).child("balance");
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
}
