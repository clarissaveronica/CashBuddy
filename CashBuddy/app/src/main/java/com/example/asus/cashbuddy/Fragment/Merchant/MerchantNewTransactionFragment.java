package com.example.asus.cashbuddy.Fragment.Merchant;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.TransactionUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class MerchantNewTransactionFragment extends Fragment {

    private TextInputEditText price;
    private Button makeTransaction;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databasePrice;
    private FirebaseUser user;
    private String currentPrice;

    public MerchantNewTransactionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_merchant_new_transaction, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize views
        price = view.findViewById(R.id.price);
        makeTransaction = view.findViewById(R.id.makeTransactionButton);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        databasePrice = FirebaseDatabase.getInstance().getReference("prices");

        databasePrice.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentPrice = dataSnapshot.getValue().toString();
                    int current = Integer.parseInt(currentPrice);
                    price.setText(changeToRupiahFormat(current));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        price.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    price.setText("");
                }
            }
        });

        makeTransaction.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Set the transaction price to Rp" + price.getText().toString() + "?")
                        .setCancelable(false)
                        .setPositiveButton(R.string.setPrice_confirm, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                int money = Integer.parseInt(price.getText().toString().replaceAll(",", ""));
                                firebaseAuth = FirebaseAuth.getInstance();
                                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                DatabaseReference reference = firebaseDatabase.getReference("prices");
                                reference.child(firebaseUser.getUid()).setValue(money);
                                price.setText("");
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
        });

        price.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                price.removeTextChangedListener(this);

                try {
                    String originalString = editable.toString();

                    Long longval;
                    if (originalString.contains(",")) {
                        originalString = originalString.replaceAll(",", "");
                    }
                    longval = Long.parseLong(originalString);

                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    formatter.applyPattern("#,###,###,###");
                    String formattedString = formatter.format(longval);

                    //setting text after format to EditText
                    price.setText(formattedString);
                    price.setSelection(price.getText().length());
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
                price.addTextChangedListener(this);

                String newPrice = price.getText().toString().replace(".", "").replace("Rp", "").replace(",", "");

                if(newPrice.equals(currentPrice)){
                    makeTransaction.setEnabled(false);
                    makeTransaction.setAlpha(0.5f);

                }else if(newPrice.isEmpty()){
                    makeTransaction.setEnabled(false);
                    makeTransaction.setAlpha(0.5f);
                }else{
                    makeTransaction.setEnabled(true);
                    makeTransaction.setAlpha(1);
                }
            }
        });
    }

    public String changeToRupiahFormat(int money){
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        String temp = formatRupiah.format((double)money);

        return temp;
    }
}
