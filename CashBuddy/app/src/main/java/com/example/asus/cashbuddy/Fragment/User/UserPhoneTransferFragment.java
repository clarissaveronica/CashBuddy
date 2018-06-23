package com.example.asus.cashbuddy.Fragment.User;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserPhoneTransferFragment extends Fragment {

    private TextInputEditText phoneNum;
    private Button submit;
    private DatabaseReference databaseUser, walletSender, walletReceiver, phoneRef;
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private int userBalance, receiverBalance;
    private String receiverName, receiverPhone, receiver;
    private int totalTransfer;

    public UserPhoneTransferFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_phone_transfer, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        phoneNum = view.findViewById(R.id.phoneNum);
        submit = view.findViewById(R.id.submitButton);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        databaseUser = FirebaseDatabase.getInstance().getReference("users");
        phoneRef = FirebaseDatabase.getInstance().getReference("phonenumbertouid");

        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String phoneNumber = phoneNum.getText().toString();

                if(TextUtils.isEmpty(phoneNumber)) {
                    phoneNum.setError("Phone number is required");
                }else if(phoneNumber.length() < 10){
                    phoneNum.setError("Invalid phone number");
                }else if(!phoneNumber.substring(0,1).equals("0")){
                    phoneNum.setError("Invalid phone number");
                }else{
                    receiverPhone = changeNum(phoneNumber);
                    makeTransfer();
                }
            }
        });
    }

    //Change phone number format
    public String changeNum(String phone){
        String num = "+62" + phone.substring(1);
        return num;
    }

    public void checkNum(final OnGetDataListener listener){
        phoneRef.child(receiverPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    receiver = dataSnapshot.getValue().toString();
                    listener.onSuccess();
                }else listener.onFailure();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getInfo(final OnGetDataListener listener){
        listener.onStart();

        databaseUser.child(receiver).child("name").addValueEventListener(new ValueEventListener() {
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

        databaseUser.child(receiver).child("balance").addValueEventListener(new ValueEventListener() {
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

    public void makeTransfer(){
        Bundle bundle = this.getArguments();
        totalTransfer = Integer.parseInt(bundle.getString("amount"));

        final String transfer = changeToRupiahFormat(totalTransfer);

        checkNum(new OnGetDataListener() {
            @Override
            public void onSuccess() {
                getInfo(new OnGetDataListener() {
                    @Override
                    public void onSuccess() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Sending " + transfer + " to " + receiverName + ". Click confirm to proceed")
                                .setCancelable(false)
                                .setPositiveButton(R.string.setPrice_confirm, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (userBalance >= totalTransfer) {
                                            Transfer transfer = new Transfer(receiver, user.getUid(), totalTransfer);
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
                        builder.setMessage("Wrong phone number. Please try again")
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
                });
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Wrong phone number. Please try again")
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
        });
    }

    //Change number format to IDR
    public String changeToRupiahFormat(int money){
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        String temp = formatRupiah.format((double)money);

        return temp;
    }

    private void setWallet(){
        //Set balance on user's wallet
        walletSender = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("balance");;
        walletSender.setValue(userBalance - totalTransfer);

        //Set balance on merchant's wallet
        walletReceiver = FirebaseDatabase.getInstance().getReference("users").child(receiver).child("balance");
        walletReceiver.setValue(receiverBalance + totalTransfer);
    }

    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess();
        void onStart();
        void onFailure();
    }
}
