package com.example.asus.cashbuddy.Fragment.User;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.asus.cashbuddy.Model.SplitBill;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.SplitBillUtil;
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

public class UserCreateSplitBillFragment extends Fragment {

    private String uid, uid2, uid3, uid4, uid5;
    private TextInputEditText totalPrice, person2, person3, person4, person5, price1, price2, price3, price4, price5;
    private int totalBill, totalPerson, splitPrice, pricePerson1, pricePerson2, pricePerson3, pricePerson4, pricePerson5;
    private LinearLayout layout3, layoutPrice3, layout4, layoutPrice4, layout5, layoutPrice5;
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private Button splitButton;
    private CheckBox splitEvenly;
    private Spinner spinner;
    private boolean valid, valid2, valid3, valid4, valid5;

    public UserCreateSplitBillFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_create_split_bill, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Get data firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        //Initialize views
        totalPrice = view.findViewById(R.id.insertPrice);
        splitEvenly = view.findViewById(R.id.checkBox);
        splitButton = view.findViewById(R.id.splitButton);
        price1 = view.findViewById(R.id.price1);
        price2 = view.findViewById(R.id.price2);
        price3 = view.findViewById(R.id.price3);
        price4 = view.findViewById(R.id.price4);
        price5 = view.findViewById(R.id.price5);
        person2 = view.findViewById(R.id.person2);
        person3 = view.findViewById(R.id.person3);
        person4 = view.findViewById(R.id.person4);
        person5 = view.findViewById(R.id.person5);
        layout3 = view.findViewById(R.id.layout3);
        layoutPrice3 = view.findViewById(R.id.layoutPrice3);
        layout4 = view.findViewById(R.id.layout4);
        layoutPrice4 = view.findViewById(R.id.layoutPrice4);
        layout5 = view.findViewById(R.id.layout5);
        layoutPrice5 = view.findViewById(R.id.layoutPrice5);

        spinner = view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.splitBill, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        splitEvenly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                totalPrice.clearFocus();
                if (splitEvenly.isChecked()) {
                    splitEven();
                } else {
                    enabled();
                }
            }
        });

        price1.addTextChangedListener(generalTextWatcher);
        price2.addTextChangedListener(generalTextWatcher);
        price3.addTextChangedListener(generalTextWatcher);
        price4.addTextChangedListener(generalTextWatcher);
        price5.addTextChangedListener(generalTextWatcher);
        totalPrice.addTextChangedListener(generalTextWatcher);

        splitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(pricePerson1 + pricePerson2 + pricePerson3 + pricePerson4 + pricePerson5 == totalBill){
                    price1.setError(null);
                    price2.setError(null);
                    price3.setError(null);
                    price4.setError(null);
                    price5.setError(null);
                }
                if(validateForm()) {
                    getNum(new OnGetDataListener() {
                        @Override
                        public void onSuccess() {
                            if(totalPerson == 2){
                                split(uid2, pricePerson2);
                            }else if(totalPerson ==3){
                                split(uid2, pricePerson2);
                                split(uid3, pricePerson3);
                            }else if(totalPerson == 4){
                                split(uid2, pricePerson2);
                                split(uid3, pricePerson3);
                                split(uid4, pricePerson4);
                            }else if(totalPerson == 5){
                                split(uid2, pricePerson2);
                                split(uid3, pricePerson3);
                                split(uid4, pricePerson4);
                                split(uid5, pricePerson5);
                            }

                            Toast.makeText(getActivity(), "Split bill request sent", Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                        }

                        @Override
                        public void onStart() {}

                        @Override
                        public void onFailure() {
                            if(!valid2){
                                person2.setError("Invalid phone number");
                            }

                            if(!valid3){
                                person3.setError("Invalid phone number");
                            }

                            if(!valid4){
                                person4.setError("Invalid phone number");
                            }

                            if(!valid5){
                                person5.setError("Invalid phone number");
                            }
                        }
                    });
                }
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String choose = parent.getItemAtPosition(position).toString();

                if(choose.equals("1 person")){
                    totalPerson = 2;
                    layout3.setVisibility(View.GONE);
                    layoutPrice3.setVisibility(View.GONE);
                    layout4.setVisibility(View.GONE);
                    layoutPrice4.setVisibility(View.GONE);
                    layout5.setVisibility(View.GONE);
                    layoutPrice5.setVisibility(View.GONE);
                    price3.setText("");
                    person3.setText("");
                    price4.setText("");
                    person4.setText("");
                    price5.setText("");
                    person5.setText("");
                }else if(choose.equals("2 people")){
                    totalPerson = 3;
                    layout3.setVisibility(View.VISIBLE);
                    layoutPrice3.setVisibility(View.VISIBLE);
                    layout4.setVisibility(View.GONE);
                    layoutPrice4.setVisibility(View.GONE);
                    layout5.setVisibility(View.GONE);
                    layoutPrice5.setVisibility(View.GONE);
                    price4.setText("");
                    person4.setText("");
                    price5.setText("");
                    person5.setText("");
                }else if(choose.equals("3 people")){
                    totalPerson = 4;
                    layout3.setVisibility(View.VISIBLE);
                    layoutPrice3.setVisibility(View.VISIBLE);
                    layout4.setVisibility(View.VISIBLE);
                    layoutPrice4.setVisibility(View.VISIBLE);
                    layout5.setVisibility(View.GONE);
                    layoutPrice5.setVisibility(View.GONE);
                    price5.setText("");
                    person5.setText("");
                }else if(choose.equals("4 people")){
                    totalPerson = 5;
                    layout3.setVisibility(View.VISIBLE);
                    layoutPrice3.setVisibility(View.VISIBLE);
                    layout4.setVisibility(View.VISIBLE);
                    layoutPrice4.setVisibility(View.VISIBLE);
                    layout5.setVisibility(View.VISIBLE);
                    layoutPrice5.setVisibility(View.VISIBLE);
                }

                if(splitEvenly.isChecked()){
                    splitEven();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void enabled(){
        price1.setEnabled(true);
        price2.setEnabled(true);
        price3.setEnabled(true);
        price4.setEnabled(true);
        price5.setEnabled(true);
        price1.setFocusable(true);
        price1.setFocusableInTouchMode(true);
        price2.setFocusable(true);
        price2.setFocusableInTouchMode(true);
        price3.setFocusable(true);
        price3.setFocusableInTouchMode(true);
        price4.setFocusable(true);
        price4.setFocusableInTouchMode(true);
        price5.setFocusable(true);
        price5.setFocusableInTouchMode(true);
    }

    public void split(String receiver, int amount){
        if(splitEvenly.isChecked()) {
            SplitBill splitBill = new SplitBill(receiver, user.getUid(), splitPrice);
            SplitBillUtil.insert(splitBill);
        }else{
            SplitBill splitBill = new SplitBill(receiver, user.getUid(), amount);
            SplitBillUtil.insert(splitBill);
        }
    }

    public boolean validateForm(){
        String price1Text = price1.getText().toString();
        String price2Text = price2.getText().toString();
        String price3Text = price3.getText().toString();
        String price4Text = price4.getText().toString();
        String price5Text = price5.getText().toString();
        String totalPrices = totalPrice.getText().toString();
        String person2Text = person2.getText().toString();
        String person3Text = person3.getText().toString();
        String person4Text = person4.getText().toString();
        String person5Text = person5.getText().toString();

        valid = true;

        if(TextUtils.isEmpty(price1Text)){
            price1.setError("Price is required");
            valid = false;
        }

        if(TextUtils.isEmpty(price2Text)){
            price2.setError("Price is required");
            valid = false;
        }

        if(TextUtils.isEmpty(totalPrices)){
            totalPrice.setError("Price is required");
            valid = false;
        }else if(totalPrices.equals("Rp")){
            totalPrice.setError("Price is required");
            valid = false;
        }else if(totalBill < 10000){
            totalPrice.setError("Minimum bill to split is Rp10.000");
            valid = false;
        }

        if(!TextUtils.isEmpty(price1Text) && !TextUtils.isEmpty(price2Text)) {
            if (!splitEvenly.isChecked()) {
                pricePerson1 = Integer.parseInt(price1Text.replace(",", ""));;
                pricePerson2 = Integer.parseInt(price2Text.replace(",", ""));
                if(pricePerson1 + pricePerson2 != totalBill) {
                    price1.setError("Invalid amount");
                    price2.setError("Invalid amount");
                    valid = false;
                }
            }
        }

        if(TextUtils.isEmpty(person2Text)) {
            person2.setError("Phone number is required");
            valid = false;
        }else if(person2Text.length() < 10){
            person2.setError("Invalid phone number");
            valid = false;
        }else if(!person2Text.substring(0,1).equals("0")){
            person2.setError("Invalid phone number");
            valid = false;
        }

        if(totalPerson==3){
            if(TextUtils.isEmpty(price3Text)){
                price3.setError("Price is required");
                valid = false;
            }

            if(TextUtils.isEmpty(person3Text)) {
                person3.setError("Phone number is required");
                valid = false;
            }else if(person3Text.length() < 10){
                person3.setError("Invalid phone number");
                valid = false;
            }else if(!person3Text.substring(0,1).equals("0")){
                person3.setError("Invalid phone number");
                valid = false;
            }

            if(!TextUtils.isEmpty(price1Text) && !TextUtils.isEmpty(price2Text) && !TextUtils.isEmpty(price3Text)) {
                if (!splitEvenly.isChecked()) {
                    pricePerson1 = Integer.parseInt(price1Text.replace(",", ""));
                    pricePerson2 = Integer.parseInt(price2Text.replace(",", ""));
                    pricePerson3 = Integer.parseInt(price3Text.replace(",", ""));
                    if(pricePerson1 + pricePerson2 + pricePerson3 != totalBill) {
                        price1.setError("Invalid amount");
                        price2.setError("Invalid amount");
                        price3.setError("Invalid amount");
                        valid = false;
                    }
                }
            }

        }else if(totalPerson==4){
            if(TextUtils.isEmpty(price3Text)){
                price3.setError("Price is required");
                valid = false;
            }

            if(TextUtils.isEmpty(price4Text)){
                price4.setError("Price is required");
                valid = false;
            }

            if(TextUtils.isEmpty(person3Text)) {
                person3.setError("Phone number is required");
                valid = false;
            }else if(person3Text.length() < 10){
                person3.setError("Invalid phone number");
                valid = false;
            }else if(!person3Text.substring(0,1).equals("0")){
                person3.setError("Invalid phone number");
                valid = false;
            }

            if(TextUtils.isEmpty(person4Text)) {
                person4.setError("Phone number is required");
                valid = false;
            }else if(person4Text.length() < 10){
                person4.setError("Invalid phone number");
                valid = false;
            }else if(!person4Text.substring(0,1).equals("0")){
                person4.setError("Invalid phone number");
                valid = false;
            }

            if(!TextUtils.isEmpty(price1Text) && !TextUtils.isEmpty(price2Text) && !TextUtils.isEmpty(price3Text) && !TextUtils.isEmpty(price4Text)) {
                if (!splitEvenly.isChecked()) {
                    pricePerson1 = Integer.parseInt(price1Text.replace(",", ""));
                    pricePerson2 = Integer.parseInt(price2Text.replace(",", ""));
                    pricePerson3 = Integer.parseInt(price3Text.replace(",", ""));
                    pricePerson4 = Integer.parseInt(price4Text.replace(",", ""));
                    if(pricePerson1 + pricePerson2 + pricePerson3 + pricePerson4 != totalBill) {
                        price1.setError("Invalid amount");
                        price2.setError("Invalid amount");
                        price3.setError("Invalid amount");
                        price4.setError("Invalid amount");
                        valid = false;
                    }
                }
            }
        }else if(totalPerson==5){
            if(TextUtils.isEmpty(price3Text)){
                price3.setError("Price is required");
                valid = false;
            }

            if(TextUtils.isEmpty(price4Text)){
                price4.setError("Price is required");
                valid = false;
            }

            if(TextUtils.isEmpty(price5Text)){
                price5.setError("Price is required");
                valid = false;
            }

            if(TextUtils.isEmpty(person3Text)) {
                person3.setError("Phone number is required");
                valid = false;
            }else if(person3Text.length() < 10){
                person3.setError("Invalid phone number");
                valid = false;
            }else if(!person3Text.substring(0,1).equals("0")){
                person3.setError("Invalid phone number");
                valid = false;
            }

            if(TextUtils.isEmpty(person4Text)) {
                person4.setError("Phone number is required");
                valid = false;
            }else if(person4Text.length() < 10){
                person4.setError("Invalid phone number");
                valid = false;
            }else if(!person4Text.substring(0,1).equals("0")){
                person4.setError("Invalid phone number");
                valid = false;
            }

            if(TextUtils.isEmpty(person5Text)) {
                person5.setError("Phone number is required");
                valid = false;
            }else if(person5Text.length() < 10){
                person5.setError("Invalid phone number");
                valid = false;
            }else if(!person5Text.substring(0,1).equals("0")){
                person5.setError("Invalid phone number");
                valid = false;
            }

            if(!TextUtils.isEmpty(price1Text) && !TextUtils.isEmpty(price2Text) && !TextUtils.isEmpty(price3Text) && !TextUtils.isEmpty(price4Text) && !TextUtils.isEmpty(price5Text)) {
                if (!splitEvenly.isChecked()) {
                    pricePerson1 = Integer.parseInt(price1Text.replace(",", ""));
                    pricePerson2 = Integer.parseInt(price2Text.replace(",", ""));
                    pricePerson3 = Integer.parseInt(price3Text.replace(",", ""));
                    pricePerson4 = Integer.parseInt(price4Text.replace(",", ""));
                    pricePerson5 = Integer.parseInt(price5Text.replace(",", ""));
                    if(pricePerson1 + pricePerson2 + pricePerson3 + pricePerson4 + pricePerson5 != totalBill) {
                        price1.setError("Invalid amount");
                        price2.setError("Invalid amount");
                        price3.setError("Invalid amount");
                        price4.setError("Invalid amount");
                        price5.setError("Invalid amount");
                        valid = false;
                    }
                }
            }
        }

        return valid;
    }

    private TextWatcher generalTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            price1.removeTextChangedListener(this);
            price2.removeTextChangedListener(this);
            price3.removeTextChangedListener(this);
            price4.removeTextChangedListener(this);
            price5.removeTextChangedListener(this);
            totalPrice.removeTextChangedListener(this);

            try {
                String originalString = s.toString();

                Long longval;
                if (originalString.contains(",")) {
                    originalString = originalString.replaceAll(",", "");
                }

                if(originalString.contains("Rp")){
                    originalString = originalString.replaceAll("Rp", "");
                }
                longval = Long.parseLong(originalString);

                DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                formatter.applyPattern("#,###,###,###");
                String formattedString = formatter.format(longval);

                //setting text after format to EditText
                if(price1.hasFocus()) {
                    price1.setText(formattedString);
                    price1.setSelection(price1.getText().length());
                }else if(price2.hasFocus()){
                    price2.setText(formattedString);
                    price2.setSelection(price2.getText().length());
                }else if(price3.hasFocus()){
                    price3.setText(formattedString);
                    price3.setSelection(price3.getText().length());
                }else if(price4.hasFocus()){
                    price4.setText(formattedString);
                    price4.setSelection(price4.getText().length());
                }else if(price5.hasFocus()){
                    price5.setText(formattedString);
                    price5.setSelection(price5.getText().length());
                }else if(totalPrice.hasFocus()){
                    totalPrice.setText("Rp" + formattedString);
                    totalPrice.setSelection(totalPrice.getText().length());

                    if(!totalPrice.getText().toString().equals("")){
                        totalBill = Integer.parseInt(totalPrice.getText().toString().replace(",", "").replace("Rp", ""));
                    }

                    if(splitEvenly.isChecked()){
                        splitEven();
                    }
                }
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            price1.addTextChangedListener(this);
            price2.addTextChangedListener(this);
            price3.addTextChangedListener(this);
            price4.addTextChangedListener(this);
            price5.addTextChangedListener(this);
            totalPrice.addTextChangedListener(this);
        }

    };

    private void disabled(){
        price1.setEnabled(false);
        price1.setFocusable(false);
        price2.setEnabled(false);
        price2.setFocusable(false);
        price3.setEnabled(false);
        price3.setFocusable(false);
        price4.setEnabled(false);
        price4.setFocusable(false);
        price5.setEnabled(false);
        price5.setFocusable(false);
    }

    private void splitEven(){
        disabled();
        splitPrice = totalBill/totalPerson;

        DecimalFormat formatter = new DecimalFormat("#,###,###");
        String splitted = formatter.format(splitPrice);

        if(totalPerson ==2){
            price1.setText(splitted);
            price2.setText(splitted);
            price1.setError(null);
            price2.setError(null);
        }else if(totalPerson==3){
            price1.setText(splitted);
            price2.setText(splitted);
            price3.setText(splitted);
            price1.setError(null);
            price2.setError(null);
            price3.setError(null);
        }else if(totalPerson==4){
            price1.setText(splitted);
            price2.setText(splitted);
            price3.setText(splitted);
            price4.setText(splitted);
            price1.setError(null);
            price2.setError(null);
            price3.setError(null);
            price4.setError(null);
        }else if(totalPerson==5){
            price1.setText(splitted);
            price2.setText(splitted);
            price3.setText(splitted);
            price4.setText(splitted);
            price5.setText(splitted);
            price1.setError(null);
            price2.setError(null);
            price3.setError(null);
            price4.setError(null);
            price5.setError(null);
        }
    }

    private void getNum(final OnGetDataListener listener){
        valid2 = true;
        valid3 = true;
        valid4 = true;
        valid5 = true;

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        if(totalPerson == 2) {
            DatabaseReference userNameRef = rootRef.child("phonenumbertouid").child("+62" + person2.getText().toString().substring(1));
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if(!user.getUid().equals(dataSnapshot.getValue().toString())) {
                            uid2 = dataSnapshot.getValue().toString();
                            listener.onSuccess();
                        }else{
                            valid2 = false;
                            listener.onFailure();
                        }
                    }else{
                        valid2 = false;
                        listener.onFailure();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            userNameRef.addListenerForSingleValueEvent(eventListener);
        }else if(totalPerson == 3){
            DatabaseReference userNameRef = rootRef.child("phonenumbertouid").child("+62" + person2.getText().toString().substring(1));
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if(!user.getUid().equals(dataSnapshot.getValue().toString())) {
                            uid2 = dataSnapshot.getValue().toString();

                            DatabaseReference userNameRef = rootRef.child("phonenumbertouid").child("+62" + person3.getText().toString().substring(1));
                            ValueEventListener eventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        if(!user.getUid().equals(dataSnapshot.getValue().toString())) {
                                            uid3 = dataSnapshot.getValue().toString();
                                            listener.onSuccess();
                                        }else {
                                            valid3 = false;
                                            listener.onFailure();
                                        }
                                    }else{
                                        valid3 = false;
                                        listener.onFailure();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            };
                            userNameRef.addListenerForSingleValueEvent(eventListener);
                        }else{
                            valid2 = false;
                            listener.onFailure();
                        }
                    }else{
                        valid2 = false;
                        listener.onFailure();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            userNameRef.addListenerForSingleValueEvent(eventListener);
        }else if(totalPerson == 4){
            DatabaseReference userNameRef = rootRef.child("phonenumbertouid").child("+62" + person2.getText().toString().substring(1));
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if(!user.getUid().equals(dataSnapshot.getValue().toString())) {
                            uid2 = dataSnapshot.getValue().toString();

                            DatabaseReference userNameRef = rootRef.child("phonenumbertouid").child("+62" + person3.getText().toString().substring(1));
                            ValueEventListener eventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        if(!user.getUid().equals(dataSnapshot.getValue().toString())) {
                                            uid3 = dataSnapshot.getValue().toString();

                                            DatabaseReference userNameRef = rootRef.child("phonenumbertouid").child("+62" + person4.getText().toString().substring(1));
                                            ValueEventListener eventListener = new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        if(!user.getUid().equals(dataSnapshot.getValue().toString())) {
                                                            uid4 = dataSnapshot.getValue().toString();
                                                            listener.onSuccess();
                                                        }else {
                                                            valid4 = false;
                                                            listener.onFailure();
                                                        }
                                                    }else{
                                                        valid4 = false;
                                                        listener.onFailure();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            };
                                            userNameRef.addListenerForSingleValueEvent(eventListener);
                                        }else {
                                            valid3 = false;
                                            listener.onFailure();
                                        }
                                    }else{
                                        valid3 = false;
                                        listener.onFailure();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            };
                            userNameRef.addListenerForSingleValueEvent(eventListener);
                        }else{
                            valid2 = false;
                            listener.onFailure();
                        }
                    }else{
                        valid2 = false;
                        listener.onFailure();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            userNameRef.addListenerForSingleValueEvent(eventListener);
        }else if(totalPerson == 5){
            DatabaseReference userNameRef = rootRef.child("phonenumbertouid").child("+62" + person2.getText().toString().substring(1));
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if(!user.getUid().equals(dataSnapshot.getValue().toString())) {
                            uid2 = dataSnapshot.getValue().toString();

                            DatabaseReference userNameRef = rootRef.child("phonenumbertouid").child("+62" + person3.getText().toString().substring(1));
                            ValueEventListener eventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        if(!user.getUid().equals(dataSnapshot.getValue().toString())) {
                                            uid3 = dataSnapshot.getValue().toString();

                                            DatabaseReference userNameRef = rootRef.child("phonenumbertouid").child("+62" + person4.getText().toString().substring(1));
                                            ValueEventListener eventListener = new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        if(!user.getUid().equals(dataSnapshot.getValue().toString())) {
                                                            uid4 = dataSnapshot.getValue().toString();

                                                            DatabaseReference userNameRef = rootRef.child("phonenumbertouid").child("+62" + person5.getText().toString().substring(1));
                                                            ValueEventListener eventListener = new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.exists()) {
                                                                        if(!user.getUid().equals(dataSnapshot.getValue().toString())) {
                                                                            uid5 = dataSnapshot.getValue().toString();
                                                                            listener.onSuccess();
                                                                        }else {
                                                                            valid5 = false;
                                                                            listener.onFailure();
                                                                        }
                                                                    }else{
                                                                        valid5 = false;
                                                                        listener.onFailure();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {
                                                                }
                                                            };
                                                            userNameRef.addListenerForSingleValueEvent(eventListener);
                                                        }else {
                                                            valid4 = false;
                                                            listener.onFailure();
                                                        }
                                                    }else{
                                                        valid4 = false;
                                                        listener.onFailure();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            };
                                            userNameRef.addListenerForSingleValueEvent(eventListener);
                                        }else {
                                            valid3 = false;
                                            listener.onFailure();
                                        }
                                    }else{
                                        valid3 = false;
                                        listener.onFailure();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            };
                            userNameRef.addListenerForSingleValueEvent(eventListener);
                        }else{
                            valid2 = false;
                            listener.onFailure();
                        }
                    }else{
                        valid2 = false;
                        listener.onFailure();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            userNameRef.addListenerForSingleValueEvent(eventListener);
        }
    }

    //Change number format to IDR
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
