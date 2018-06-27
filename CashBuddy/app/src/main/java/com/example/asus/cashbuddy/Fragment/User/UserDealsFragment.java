package com.example.asus.cashbuddy.Fragment.User;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.asus.cashbuddy.Adapter.UserDealsAdapter;
import com.example.asus.cashbuddy.Model.Deals;
import com.example.asus.cashbuddy.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserDealsFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ArrayList<Deals> deals;
    private UserDealsAdapter adapter;

    public UserDealsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_deals, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deals = new ArrayList<Deals>();
        adapter = new UserDealsAdapter (deals);
        recyclerView = view.findViewById(R.id.deals_recycler);
        layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        attachDatabaseReadListener();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void attachDatabaseReadListener() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("deals");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                adapter.removeDeals();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Deals deals = child.getValue(Deals.class);
                    adapter.addDeals(deals);
                }
                layoutManager.setReverseLayout(true);
                layoutManager.setStackFromEnd(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
