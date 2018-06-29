package com.example.asus.cashbuddy.Fragment.User;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.asus.cashbuddy.Activity.User.UserScanActivity;
import com.example.asus.cashbuddy.Activity.User.UserTopUpActivity;
import com.example.asus.cashbuddy.Activity.User.UserTransferActivity;
import com.example.asus.cashbuddy.Model.User;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.AccountUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.NumberFormat;
import java.util.Locale;

public class UserHomeFragment extends Fragment {

    //Initialize
    private ImageButton scan, transfer, topup;
    private ImageView profile;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseRef;
    private FirebaseUser user;
    private TextView balance;
    private User currentUser;

    public UserHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize Views
        scan = view.findViewById(R.id.scanButton);
        transfer = view.findViewById(R.id.transferButton);
        topup = view.findViewById(R.id.topupButton);
        profile = view.findViewById(R.id.profile);
        balance = view.findViewById(R.id.balance);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        //Start qr code scan
        scan.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                Intent intent = new Intent(getActivity(), UserScanActivity.class);
                startActivity(intent);
            }
        });

        //Transfer money to another account
        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UserTransferActivity.class);
                startActivity(intent);
            }
        });

        //Do top up
        topup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UserTopUpActivity.class);
                startActivity(intent);
            }
        });

        //Show user's QR when profile button is clicked
        profile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                showQR();
            }
        });

        //Initialize balance on view
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        getUser(new OnGetDataListener() {
            @Override
            public void onSuccess() {
                User user = AccountUtil.getCurrentUser();
                String money = changeToRupiahFormat(user.getBalance());
                balance.setText(money);
                //Set image
                if(getActivity() != null) {
                    if (user.getProfilePictureUrl() != null) {
                        Glide.with(getActivity().getApplicationContext())
                                .load(user.getProfilePictureUrl())
                                .into(profile);
                    }
                }
            }

            @Override
            public void onStart() {}

            @Override
            public void onFailure() { }
        });

    }

    //Show user's QR
    public void showQR(){
        Bitmap bitmap;
        AlertDialog.Builder alertadd = new AlertDialog.Builder(getActivity());
        alertadd.setTitle("QR Code");

        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View view = factory.inflate(R.layout.qr_layout, null);

        String text2Qr = firebaseAuth.getCurrentUser().getUid(); //will be changed later
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(text2Qr, BarcodeFormat.QR_CODE,300,300);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
            ImageView image= view.findViewById(R.id.imageView);
            image.setImageBitmap(bitmap);
        }  catch (WriterException e) {
            e.printStackTrace();
        }

        alertadd.setView(view);
        alertadd.setNeutralButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {

            }
        });

        alertadd.show();
    }

    public String changeToRupiahFormat(int money){
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        String temp = formatRupiah.format((double)money);

        return temp;
    }

    public void getUser(final OnGetDataListener listener){
        listener.onStart();

        databaseRef.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    currentUser = dataSnapshot.getValue(User.class);
                    AccountUtil.setCurrentAccount(currentUser);
                    listener.onSuccess();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess();
        void onStart();
        void onFailure();
    }
}
