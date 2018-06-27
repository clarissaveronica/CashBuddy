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

import com.example.asus.cashbuddy.Adapter.HistoryAdapter;
import com.example.asus.cashbuddy.Model.History;
import com.example.asus.cashbuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ArrayList<History> history;
    private HistoryAdapter adapter;
    private FirebaseAuth auth;
    private FirebaseUser user;

    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Get data from firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        history = new ArrayList<History>();
        adapter = new HistoryAdapter (history);
        recyclerView = view.findViewById(R.id.history_recycler);
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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("history").child(user.getUid());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                adapter.removeHistory();
                for (DataSnapshot child : snapshot.getChildren()) {
                    History history = child.getValue(History.class);
                    adapter.addHistory(history);
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
