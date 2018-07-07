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

import com.example.asus.cashbuddy.Adapter.UserSentSplitBillAdapter;
import com.example.asus.cashbuddy.Model.SplitBill;
import com.example.asus.cashbuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserSentSplitBillFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ArrayList<SplitBill> splitBills;
    private UserSentSplitBillAdapter adapter;
    private FirebaseUser user;
    private SplitBill splitBill;
    private DatabaseReference refReq;

    public UserSentSplitBillFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_sent_split_bill, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        splitBills = new ArrayList<SplitBill>();
        adapter = new UserSentSplitBillAdapter(splitBills);
        recyclerView = view.findViewById(R.id.split_recycler_view);
        layoutManager = new LinearLayoutManager(getActivity());
        refReq = FirebaseDatabase.getInstance().getReference("splitbill");

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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("splitbill");

        Query query = ref.orderByChild("sender").equalTo(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                adapter.removeSplitBills();
                for (DataSnapshot child : snapshot.getChildren()) {
                    splitBill = child.getValue(SplitBill.class);
                    adapter.addSplitBills(splitBill);
                    if(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime() > splitBill.getRequestdate() + (5 * 24 * 60 * 60 * 1000)) {
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("requeststatus", 3);
                        refReq.child(child.getKey()).updateChildren(result);
                    }
                    layoutManager.setReverseLayout(true);
                    layoutManager.setStackFromEnd(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
