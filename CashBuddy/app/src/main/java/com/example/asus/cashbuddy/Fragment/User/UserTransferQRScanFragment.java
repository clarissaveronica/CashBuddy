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
import android.widget.Button;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.asus.cashbuddy.Model.History;
import com.example.asus.cashbuddy.Model.Transfer;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.HistoryUtil;
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
import java.util.HashMap;
import java.util.Locale;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserTransferQRScanFragment extends Fragment implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private DatabaseReference databaseUser, walletSender, walletReceiver, notification;
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private int userBalance, receiverBalance;
    private String result, receiverName, transfer, senderName;
    private int totalTransfer;
    private PinEntryEditText securitycode;

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
    public void handleResult(Result rawResult) {
        result = rawResult.getText();
        Bundle bundle = this.getArguments();
        totalTransfer = Integer.parseInt(bundle.getString("amount"));

        transfer = changeToRupiahFormat(totalTransfer);

        getInfo(new OnGetDataListener() {
            @Override
            public void onSuccess() {
                if (userBalance >= totalTransfer) {
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
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Unregistered phone number. Please try again")
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

        databaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    receiverName = dataSnapshot.child(result).child("name").getValue().toString();
                    receiverBalance = Integer.parseInt(dataSnapshot.child(result).child("balance").getValue().toString());
                    userBalance = Integer.parseInt(dataSnapshot.child(user.getUid()).child("balance").getValue().toString());
                    senderName = dataSnapshot.child(user.getUid()).child("name").getValue().toString();
                    listener.onSuccess();
                }else listener.onFailure();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setWallet(){
        //Set balance on sender's wallet
        walletSender = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("balance");;
        walletSender.setValue(userBalance - totalTransfer);

        //Set balance on receiver's wallet
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

    public void showInputSC(){
        final AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setTitle("Sending " + transfer + " to " + receiverName)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .setMessage("Please enter your security code to proceed")
                .create();

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.input_security_code, (ViewGroup) getView(), false);

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
                                Toast.makeText(getActivity(), "Transfer successful", Toast.LENGTH_LONG).show();
                                getActivity().finish();
                            }

                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(getActivity(), "Wrong security code", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });

                negative.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        mScannerView.resumeCameraPreview(UserTransferQRScanFragment.this);
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
                if(securitycode.getText().toString().equals(password)){
                    Transfer transfer = new Transfer(result, user.getUid(), totalTransfer);
                    TransferUtil.insert(transfer);

                    //Set history for sender
                    History history = new History("Send CB Cash", receiverName, totalTransfer);
                    HistoryUtil.insert(history, user.getUid());

                    //Set history for receiver
                    History history2 = new History("Receive CB Cash", senderName, totalTransfer);
                    HistoryUtil.insert(history2, result);

                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("amount", changeToRupiahFormat(totalTransfer));
                    notificationData.put("type", "transfer");
                    notification.child("transfer").child(result).push().setValue(notificationData);

                    setWallet();
                    listener.onSuccess();
                }else listener.onFailure();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}