package com.example.asus.cashbuddy.Fragment.All;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.asus.cashbuddy.Activity.All.AboutActivity;
import com.example.asus.cashbuddy.Activity.All.ChangeSecurityCodeVerificationActivity;
import com.example.asus.cashbuddy.Activity.All.ContactUsActivity;
import com.example.asus.cashbuddy.Activity.All.LoginActivity;
import com.example.asus.cashbuddy.Activity.Merchant.MerchantProfileActivity;
import com.example.asus.cashbuddy.Activity.User.UserProfileActivity;
import com.example.asus.cashbuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    Button editProfile, contactUs, about, signOut, changePass;
    FirebaseAuth firebaseAuth;
    DatabaseReference userDatabase;
    String uid;
    FirebaseUser user;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize view
        editProfile = view.findViewById(R.id.editProfileButton);
        contactUs = view.findViewById(R.id.contactButton);
        about = view.findViewById(R.id.aboutButton);
        signOut = view.findViewById(R.id.signOutButton);
        changePass = view.findViewById(R.id.changePassButton);

        editProfile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                checkRoleForProfile();
            }
        });

        contactUs.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getActivity(), ContactUsActivity.class);
                startActivity(intent);
            }
        });

        about.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
            }
        });

        changePass.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getActivity(), ChangeSecurityCodeVerificationActivity.class);
                startActivity(intent);
            }
        });

        signOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                logout();
            }
        });
    }

    public void checkRoleForProfile(){
        //Check user's role
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        uid = user.getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference();

        userDatabase.child("role").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.getValue().toString();
                    Intent intent;

                    switch (role){
                        case "USER":
                            intent = new Intent(getActivity(), UserProfileActivity.class);
                            startActivity(intent);
                            break;
                        case "MERCHANT":
                            intent = new Intent(getActivity(), MerchantProfileActivity.class);
                            startActivity(intent);
                            break;
                        default: break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void logout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.signOut_confirmation)
                .setCancelable(false)
                .setPositiveButton(R.string.signOut_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        firebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    }
                })
                .setNegativeButton(R.string.signOut_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.setTitle(R.string.signOut_title);
        alert.show();
    }
}
