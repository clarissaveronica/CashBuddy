package com.example.asus.cashbuddy.Fragment.Admin;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.asus.cashbuddy.Model.History;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.HistoryUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminPhoneTopUpFragment extends Fragment {

    private TextInputEditText phoneNum;
    private Button submit;
    private DatabaseReference databaseUser, walletRef, phoneRef, notification;
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private int userBalance;
    private String userName, userPhone, userID, transfer;
    private int totalTransfer;
    private PinEntryEditText securitycode;

    public AdminPhoneTopUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_phone_top_up, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        phoneNum = view.findViewById(R.id.phoneNum);
        submit = view.findViewById(R.id.submitButton);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        databaseUser = FirebaseDatabase.getInstance().getReference("users");
        phoneRef = FirebaseDatabase.getInstance().getReference("phonenumbertouid");
        notification = FirebaseDatabase.getInstance().getReference("notifications");

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
                    userPhone = changeNum(phoneNumber);
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
        phoneRef.child(userPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if(!user.getUid().equals(dataSnapshot.getValue().toString())) {
                        userID = dataSnapshot.getValue().toString();
                        listener.onSuccess();
                    }else listener.onFailure();
                }else listener.onFailure();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getInfo(final OnGetDataListener listener){
        listener.onStart();

        databaseUser.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    userName = dataSnapshot.child("name").getValue().toString();
                    userBalance = Integer.parseInt(dataSnapshot.child("balance").getValue().toString());
                    listener.onSuccess();
                }else listener.onFailure();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void makeTransfer(){
        Bundle bundle = this.getArguments();
        totalTransfer = Integer.parseInt(bundle.getString("amount"));

        transfer = changeToRupiahFormat(totalTransfer);

        checkNum(new OnGetDataListener() {
            @Override
            public void onSuccess() {
                getInfo(new OnGetDataListener() {
                    @Override
                    public void onSuccess() {
                        showInputSC();
                    }

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onFailure() {
                        phoneNum.setError("Unregistered phone number");
                    }
                });
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure() {
                phoneNum.setError("Unregistered phone number");
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
        walletRef = FirebaseDatabase.getInstance().getReference("users").child(userID).child("balance");;
        walletRef.setValue(userBalance + totalTransfer);
    }

    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess();
        void onStart();
        void onFailure();
    }

    public void showInputSC(){
        final AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setTitle("Top up " + transfer + " to " + userName)
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
                                Toast.makeText(getActivity(), "Top up successful", Toast.LENGTH_SHORT).show();
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

    public void verify(final OnGetDataListener listener){
        databaseUser.child(user.getUid()).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String password = snapshot.getValue(String.class);
                if(hash(securitycode.getText().toString()).equals(password)){
                    //Set history for user
                    History history = new History("Top Up", "CB Cash", totalTransfer);
                    HistoryUtil.insert(history, userID);

                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("amount", changeToRupiahFormat(totalTransfer));
                    notificationData.put("type", "successTopup");
                    notification.child("acceptTopUp").child(userID).push().setValue(notificationData);

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
