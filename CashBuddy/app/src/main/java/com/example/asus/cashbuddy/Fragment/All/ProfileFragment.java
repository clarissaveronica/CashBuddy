package com.example.asus.cashbuddy.Fragment.All;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.asus.cashbuddy.Activity.All.AboutActivity;
import com.example.asus.cashbuddy.Activity.All.ContactUsActivity;
import com.example.asus.cashbuddy.Activity.All.FAQActivity;
import com.example.asus.cashbuddy.Activity.User.UserProfileActivity;
import com.example.asus.cashbuddy.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    Button editProfile, faq, contactUs, about, signOut;

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
        faq = view.findViewById(R.id.faqButton);
        contactUs = view.findViewById(R.id.contactButton);
        about = view.findViewById(R.id.aboutButton);
        signOut = view.findViewById(R.id.signOutButton);

        editProfile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                startActivity(intent);
            }
        });

        faq.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getActivity(), FAQActivity.class);
                startActivity(intent);
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
    }
}
