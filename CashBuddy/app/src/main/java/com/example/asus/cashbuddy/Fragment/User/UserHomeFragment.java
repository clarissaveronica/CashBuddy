package com.example.asus.cashbuddy.Fragment.User;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.asus.cashbuddy.Activity.All.LoginActivity;
import com.example.asus.cashbuddy.Activity.Merchant.MerchantMainActivity;
import com.example.asus.cashbuddy.Activity.User.ScanActivity;
import com.example.asus.cashbuddy.Activity.User.TopUpActivity;
import com.example.asus.cashbuddy.Activity.User.TransferActivity;
import com.example.asus.cashbuddy.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class UserHomeFragment extends Fragment {

    //Initialize
    ImageButton scan, transfer, topup, profile;

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

        //Start qr code scan
        scan.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                Intent intent = new Intent(getActivity(), ScanActivity.class);
                startActivity(intent);
            }
        });

        //Transfer money to another account
        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TransferActivity.class);
                startActivity(intent);
            }
        });

        //Do top up
        topup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TopUpActivity.class);
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
    }

    //Show user's QR
    public void showQR(){
        Bitmap bitmap;
        AlertDialog.Builder alertadd = new AlertDialog.Builder(getActivity());
        alertadd.setTitle("QR Code");

        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View view = factory.inflate(R.layout.qr_layout, null);

        String text2Qr = "abcd"; //will be changed later
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
}
