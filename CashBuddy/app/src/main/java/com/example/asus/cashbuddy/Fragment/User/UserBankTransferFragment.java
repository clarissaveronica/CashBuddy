package com.example.asus.cashbuddy.Fragment.User;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.asus.cashbuddy.Model.History;
import com.example.asus.cashbuddy.Model.Withdraw;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.HistoryUtil;
import com.example.asus.cashbuddy.Utils.WithdrawUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserBankTransferFragment extends Fragment {

    private TextInputEditText userName, bankName, bankNum;
    private Button withdraw;
    private DatabaseReference databaseUser, walletRef;
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private int userBalance, withdrawBalance;
    private PinEntryEditText securitycode;


    public UserBankTransferFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_bank_transfer, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Get data from firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        databaseUser = FirebaseDatabase.getInstance().getReference("users");

        //Initialize views
        bankName = view.findViewById(R.id.bankName);
        userName = view.findViewById(R.id.userName);
        bankNum = view.findViewById(R.id.bankNumber);
        withdraw = view.findViewById(R.id.submitButton);

        Bundle bundle = this.getArguments();
        withdrawBalance = Integer.parseInt(bundle.getString("amount"));

        withdraw.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(validateForm()){
                    getInfo(new OnGetDataListener() {
                        @Override
                        public void onSuccess() {
                            if (userBalance >= withdrawBalance) {
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

    public boolean validateForm(){
        boolean valid = true;

        String bankname = bankName.getText().toString();
        String banknumber = bankNum.getText().toString();
        String merchantname = userName.getText().toString();

        if(TextUtils.isEmpty(bankname)) {
            bankName.setError("Bank name is required");
            valid = false;
        }

        if(TextUtils.isEmpty(banknumber)) {
            bankNum.setError("Bank number is required");
            valid = false;
        }

        if(TextUtils.isEmpty(merchantname)) {
            userName.setError("Merchant name is required");
            valid = false;
        }

        return valid;
    }

    public void showInputSC(){
        final AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setTitle("Sending " + changeToRupiahFormat(withdrawBalance) + " to " + bankName.getText().toString().toUpperCase() + " " + bankNum.getText().toString())
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
                                Toast.makeText(getActivity(), "Request has been successfully sent. Your request will be processed in 1x24 hours", Toast.LENGTH_SHORT).show();
                                getActivity().finish();
                            }

                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(getActivity(), "Wrong security code", Toast.LENGTH_SHORT).show();
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

        databaseUser.child(user.getUid()).child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    userBalance = Integer.parseInt(dataSnapshot.getValue().toString());
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
        walletRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("balance");
        walletRef.setValue(userBalance - withdrawBalance);
    }

    public void verify(final OnGetDataListener listener){
        databaseUser.child(user.getUid()).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String password = snapshot.getValue(String.class);
                if(hash(securitycode.getText().toString()).equals(password)){
                    Withdraw withdraw= new Withdraw(userName.getText().toString(), user.getUid(), bankName.getText().toString().toUpperCase(), withdrawBalance, bankNum.getText().toString(), "users");
                    WithdrawUtil.insert(withdraw);

                    History history = new History("Send CB Cash", bankName.getText().toString() + " " + bankNum.getText().toString(), withdrawBalance);
                    HistoryUtil.insert(history, user.getUid());

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

    public void showError(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

