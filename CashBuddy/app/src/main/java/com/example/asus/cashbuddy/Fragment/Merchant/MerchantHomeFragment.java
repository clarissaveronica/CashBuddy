package com.example.asus.cashbuddy.Fragment.Merchant;


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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.asus.cashbuddy.Activity.Merchant.MerchantWithdrawActivity;
import com.example.asus.cashbuddy.R;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class MerchantHomeFragment extends Fragment {

    private ImageButton qrButton, withdrawButton;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseWallet;
    private FirebaseUser merchant;
    private TextView balance;

    public MerchantHomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_merchant_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize views
        withdrawButton = view.findViewById(R.id.withdrawButton);
        qrButton = view.findViewById(R.id.generateQRButton);
        balance = view.findViewById(R.id.balance);

        firebaseAuth = FirebaseAuth.getInstance();
        merchant = firebaseAuth.getCurrentUser();

        //onClick events
        qrButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                showQR();
            }
        });

        withdrawButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getActivity(), MerchantWithdrawActivity.class);
                startActivity(intent);
            }
        });

        //Initialize balance on view
        databaseWallet = FirebaseDatabase.getInstance().getReference("merchant");

        databaseWallet.child(merchant.getUid()).child("balance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String money = changeToRupiahFormat(Integer.parseInt(dataSnapshot.getValue().toString()));
                balance.setText(money);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

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
}
