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

import com.example.asus.cashbuddy.Adapter.UserReceiveSplitBillAdapter;
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
public class UserReceiveSplitBillFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ArrayList<SplitBill> splitBills;
    private UserReceiveSplitBillAdapter adapter;
    private FirebaseUser user;
    private DatabaseReference refReq;
    private SplitBill splitBill;

    public UserReceiveSplitBillFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_receive_split_bill, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        refReq = FirebaseDatabase.getInstance().getReference("splitbill");

        splitBills = new ArrayList<SplitBill>();
        adapter = new UserReceiveSplitBillAdapter(splitBills);
        recyclerView = view.findViewById(R.id.received_split_recycler_view);
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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("splitbill");

        Query query = ref.orderByChild("receiver").equalTo(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                adapter.removeSplitBills();
                for (DataSnapshot child : snapshot.getChildren()) {
                    splitBill = child.getValue(SplitBill.class);
                    if(splitBill.getRequeststatus()==0) {
                        if(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime() < splitBill.getRequestdate() + (5 * 24 * 60 * 60 * 1000)) {
                            adapter.addSplitBills(splitBill);
                        }else{
                            HashMap<String, Object> result = new HashMap<>();
                            result.put("requeststatus", 3);
                            refReq.child(child.getKey()).updateChildren(result);
                        }
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
