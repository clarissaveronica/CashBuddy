package com.example.asus.cashbuddy.Fragment.User;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.example.asus.cashbuddy.Model.TopUp;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.TopUpUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import static com.example.asus.cashbuddy.Utils.ExternalStoragePermissionUtil.checkReadExternalStoragePermission;
import static com.example.asus.cashbuddy.Utils.ExternalStoragePermissionUtil.requestReadExternalStoragePermission;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserTransferTopUpFragment extends Fragment {

    private TextInputEditText topUpAmount, bankName, userName;
    private ImageView proofImageView;
    private Button submit, uploadPicture;
    private DatabaseReference databaseUser;
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private PinEntryEditText securitycode;
    private int topUpBalance;
    private Uri uri;
    private boolean isProfilePictureEdited;
    private AlertDialog builder;
    private ViewGroup loading;

    // Request permission code
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 100;

    // Choose image code
    private static final int CHOOSE_PICTURE_FROM_GALLERY_CODE = 101;

    public UserTransferTopUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_transfer_top_up, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize views
        topUpAmount = view.findViewById(R.id.topUpAmount);
        bankName = view.findViewById(R.id.bankName);
        userName = view.findViewById(R.id.userName);
        proofImageView = view.findViewById(R.id.payment_proof);
        uploadPicture = view.findViewById(R.id.uploadButton);
        submit = view.findViewById(R.id.submitButton);
        loading = getActivity().findViewById(R.id.loadingPanel);
        isProfilePictureEdited = false;

        //Get data
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        databaseUser = FirebaseDatabase.getInstance().getReference("users");

        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
            if(validateForm()){
                showInputSC();
            }
            }
        });

        uploadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePictureFromGallery();
            }
        });

        topUpAmount.addTextChangedListener(generalTextWatcher);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePictureFromGallery();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == CHOOSE_PICTURE_FROM_GALLERY_CODE) {
                isProfilePictureEdited = true;
                uri = data.getData();
                changeProfilePicture();
            }
        }
    }

    private void choosePictureFromGallery(){
        if(checkReadExternalStoragePermission(getActivity())) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, CHOOSE_PICTURE_FROM_GALLERY_CODE);
        }
        else {
            requestReadExternalStoragePermission(getActivity(), REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    private void changeProfilePicture() {
        if(uri == null) {
            proofImageView.setImageResource(R.drawable.logo);
        }else {
            proofImageView.setImageURI(uri);
        }
    }


    public boolean validateForm(){
        boolean valid = true;

        String money = topUpAmount.getText().toString();
        String bankname = bankName.getText().toString();
        String username = userName.getText().toString();

        if(TextUtils.isEmpty(money)) {
            topUpAmount.setError("Minimal amount for top up is Rp10.000");
            valid = false;
        }else if(topUpBalance < 10000){
            topUpAmount.setError("Minimal amount for top up is Rp10.000");
            valid = false;
        }

        if(TextUtils.isEmpty(bankname)) {
            bankName.setError("Bank name is required");
            valid = false;
        }

        if(TextUtils.isEmpty(username)) {
            userName.setError("User name is required");
            valid = false;
        }

        if(!isProfilePictureEdited) {
            valid=false;
            Toast.makeText(getActivity(), "Image is required", Toast.LENGTH_SHORT).show();
        }

        return valid;
    }

    //Change number InputEditText real time
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
            topUpAmount.removeTextChangedListener(this);

            try {
                String originalString = s.toString();

                Long longval;
                if (originalString.contains(",")) {
                    originalString = originalString.replaceAll(",", "");
                }
                longval = Long.parseLong(originalString);

                DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                formatter.applyPattern("#,###,###,###");
                String formattedString = formatter.format(longval);

                //setting text after format to EditText
                topUpAmount.setText(formattedString);
                topUpAmount.setSelection(topUpAmount.getText().length());
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            topUpAmount.addTextChangedListener(this);

            if(!topUpAmount.getText().toString().equals("")) {
                topUpBalance = Integer.parseInt(topUpAmount.getText().toString().replace(",", ""));
            }
        }

    };

    //Change number format to IDR
    public String changeToRupiahFormat(int money){
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        String temp = formatRupiah.format((double)money);

        return temp;
    }

    public void showInputSC(){
        builder = new AlertDialog.Builder(getActivity())
                .setTitle("Requesting " + changeToRupiahFormat(topUpBalance) + " top up")
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
                        loading.setVisibility(View.VISIBLE);
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        verify(new OnGetDataListener() {
                            @Override
                            public void onSuccess() {
                                loading.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), "Top up request has been successfully sent. Your request will be processed in 1x24 hours", Toast.LENGTH_LONG).show();
                                getActivity().finish();
                            }

                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onFailure() {
                                loading.setVisibility(View.INVISIBLE);
                                Toast.makeText(getActivity(), "Wrong security code", Toast.LENGTH_LONG).show();
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
                if(securitycode.getText().toString().equals(password)){
                    builder.dismiss();
                    final StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("topuprequest/"+ user.getUid() +System.currentTimeMillis()+".jpg");
                    if (uri!=null){
                        profileImageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                @SuppressWarnings("VisibleForTests") Uri downloadUrl =taskSnapshot.getDownloadUrl();

                                String transferproofUrl = downloadUrl.toString();

                                TopUp topUp= new TopUp(userName.getText().toString(), user.getUid(), transferproofUrl, bankName.getText().toString(), topUpBalance);
                                TopUpUtil.insert(topUp);
                                listener.onSuccess();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(),"Image failed to upload",Toast.LENGTH_LONG).show();
                                listener.onSuccess();
                            }
                        });
                    }
                }else listener.onFailure();
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
