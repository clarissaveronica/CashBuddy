package com.example.asus.cashbuddy.Fragment.User;


import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.asus.cashbuddy.Model.Transfer;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.TransferUtil;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class UserTransferQRScanFragment extends Fragment implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private DatabaseReference databaseUser, walletSender, walletReceiver;
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private int userBalance, receiverBalance;
    private String result, receiverName;
    private int totalTransfer;

    public UserTransferQRScanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(getContext());

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 100);

        return mScannerView;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        databaseUser = FirebaseDatabase.getInstance().getReference("users");

        //Bundle bundle = this.getArguments();
        //totalTransfer = Integer.parseInt(bundle.getString("amount"));
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
    public void handleResult(Result rawResult) {
        result = rawResult.getText();
        Bundle bundle = this.getArguments();
        totalTransfer = Integer.parseInt(bundle.getString("amount"));

        final String transfer = changeToRupiahFormat(totalTransfer);

        getInfo(new OnGetDataListener() {
            @Override
            public void onSuccess() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Sending " + transfer + " to " + receiverName + ". Click confirm to proceed")
                        .setCancelable(false)
                        .setPositiveButton(R.string.setPrice_confirm, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (userBalance >= totalTransfer) {
                                    Transfer transfer = new Transfer(result, user.getUid(), totalTransfer);
                                    TransferUtil.insert(transfer);

                                    setWallet();
                                    Toast.makeText(getContext(), "Transfer successful", Toast.LENGTH_LONG).show();

                                    getActivity().finish();
                                }else {
                                    dialog.cancel();
                                    showError();
                                }
                            }
                        })
                        .setNegativeButton(R.string.setPrice_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mScannerView.resumeCameraPreview(UserTransferQRScanFragment.this);
                                dialog.cancel();
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
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Invalid QR code. Please try again")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mScannerView.resumeCameraPreview(UserTransferQRScanFragment.this);
                            }
                        });

                AlertDialog alert = builder.create();
                alert.setTitle("Oops!");
                alert.show();
            }
        });
    }

    public void showError(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Insufficient funds. Please top up to proceed with the transaction")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mScannerView.resumeCameraPreview(UserTransferQRScanFragment.this);
                    }
                });

        AlertDialog alert = builder.create();
        alert.setTitle("Oops!");
        alert.show();
    }

    public void getInfo(final OnGetDataListener listener){
        listener.onStart();

        databaseUser.child(result).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    receiverName = dataSnapshot.getValue().toString();
                    listener.onSuccess();
                }else listener.onFailure();
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

        databaseUser.child(result).child("balance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    receiverBalance = Integer.parseInt(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setWallet(){
        //Set balance on user's wallet
        walletSender = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("balance");;
        walletSender.setValue(userBalance - totalTransfer);

        //Set balance on merchant's wallet
        walletReceiver = FirebaseDatabase.getInstance().getReference("users").child(result).child("balance");
        walletReceiver.setValue(receiverBalance + totalTransfer);
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