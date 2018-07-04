package com.example.asus.cashbuddy.Fragment.Merchant;


import android.content.DialogInterface;
import android.graphics.Color;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.asus.cashbuddy.Model.PaymentRequest;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.PaymentRequestUtil;
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
import java.util.HashMap;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class MerchantRequestPaymentFragment extends Fragment {

    private TextInputEditText amountRequested, phoneNum;
    private Button submit;
    private Spinner spinner;
    private String choose;
    private int newPrice;
    private String receiver, receiverName, receiverPhone;
    private boolean valid;
    private PinEntryEditText securitycode;
    private DatabaseReference databaseUser, phoneRef, notification, databaseMerchant;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    public MerchantRequestPaymentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_merchant_request_payment, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize view
        amountRequested = view.findViewById(R.id.amountTransfer);
        phoneNum = view.findViewById(R.id.phoneNum);
        submit = view.findViewById(R.id.submitButton);
        spinner = view.findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.typePaymentRequest, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        databaseUser = FirebaseDatabase.getInstance().getReference("users");
        databaseMerchant = FirebaseDatabase.getInstance().getReference("merchant");
        phoneRef = FirebaseDatabase.getInstance().getReference("phonenumbertouid");
        notification = FirebaseDatabase.getInstance().getReference("notifications");

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choose = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(validateInfo()){
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
            }
        });

        amountRequested.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                amountRequested.removeTextChangedListener(this);

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
                    amountRequested.setText(formattedString);
                    amountRequested.setSelection(amountRequested.getText().length());
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
                amountRequested.addTextChangedListener(this);

                if(!amountRequested.getText().toString().equals("")){
                    newPrice = Integer.parseInt(amountRequested.getText().toString().replace(",", ""));
                }
            }
        });
    }

    public boolean validateInfo(){
        String phone = phoneNum.getText().toString();
        valid = true;

        if(TextUtils.isEmpty(phone)) {
            phoneNum.setError("Phone number is required");
            valid = false;
        }else if(phone.length() < 10){
            phoneNum.setError("Invalid phone number");
            valid = false;
        }else if(!phone.substring(0,1).equals("0")){
            phoneNum.setError("Invalid phone number");
            valid = false;
        }

        if(!TextUtils.isEmpty(phone)){
            receiverPhone = changeNum(phoneNum.getText().toString());
        }

        if(choose.equals("Select Type")){
            TextView errorText = (TextView)spinner.getSelectedView();
            errorText.setError("Type of payment is required");
            errorText.setTextColor(Color.RED);
            valid = false;
        }

        if(newPrice < 10000){
            amountRequested.setError("Minimal amount for transfer is Rp 10.000");
            valid = false;
        }

        return valid;
    }

    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess();
        void onStart();
        void onFailure();
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

        databaseUser.child(receiver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    receiverName = dataSnapshot.child("name").getValue().toString();
                    listener.onSuccess();
                }else listener.onFailure();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showInputSC(){
        final AlertDialog builder = new AlertDialog.Builder(getContext())
                .setTitle("Requesting " + changeToRupiahFormat(newPrice) + " from " + receiverName)
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
                                Toast.makeText(getActivity(), "Payment request delivered!", Toast.LENGTH_SHORT).show();
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
        databaseMerchant.child(user.getUid()).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String password = snapshot.getValue(String.class);
                if(hash(securitycode.getText().toString()).equals(password)){
                    PaymentRequest paymentRequest = new PaymentRequest(receiver, user.getUid(), newPrice, "merchant", choose);
                    PaymentRequestUtil.insert(paymentRequest);

                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("amount", changeToRupiahFormat(newPrice));
                    notificationData.put("type", "paymentReq");
                    notification.child("requestPayment").child("sent").child(receiver).push().setValue(notificationData);

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
