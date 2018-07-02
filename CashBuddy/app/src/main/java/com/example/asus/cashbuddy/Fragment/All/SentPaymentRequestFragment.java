package com.example.asus.cashbuddy.Fragment.All;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.asus.cashbuddy.Adapter.UserReceivePaymentReqAdapter;
import com.example.asus.cashbuddy.Adapter.UserSentPaymentReqAdapter;
import com.example.asus.cashbuddy.Model.PaymentRequest;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class SentPaymentRequestFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ArrayList<PaymentRequest> paymentReq;
    private UserSentPaymentReqAdapter adapter;
    private FirebaseUser user;

    public SentPaymentRequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sent_payment_request, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        paymentReq = new ArrayList<PaymentRequest>();
        adapter = new UserSentPaymentReqAdapter(paymentReq);
        recyclerView = view.findViewById(R.id.request_recycler_view);
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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("paymentrequest");

        Query query = ref.orderByChild("senderRequest").equalTo(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                adapter.removePaymentRequest();
                for (DataSnapshot child : snapshot.getChildren()) {
                    PaymentRequest paymentRequest = child.getValue(PaymentRequest.class);
                    adapter.addPaymentRequest(paymentRequest);
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
