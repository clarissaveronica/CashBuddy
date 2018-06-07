package com.example.asus.cashbuddy.Fragment.Admin;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.asus.cashbuddy.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminHistoryFragment extends Fragment {


    public AdminHistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_history, container, false);
    }

}
