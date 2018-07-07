package com.example.asus.cashbuddy.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.asus.cashbuddy.Activity.User.UserSplitBillDetailActivity;
import com.example.asus.cashbuddy.Model.Transaction;
import com.example.asus.cashbuddy.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class UserSplitBillAdapter extends RecyclerView.Adapter<UserSplitBillAdapter.ViewHolder> {

    //Split Bills items
    private ArrayList<Transaction> transactions;

    public UserSplitBillAdapter(@NonNull List<Transaction> transactions) {
        this.transactions = new ArrayList<>(transactions);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_user_split_bill, parent, false);

        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Transaction transaction = transactions.get(position);

        FirebaseDatabase.getInstance().getReference("merchant").child(transaction.getSid()).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.NameTextView.setText(dataSnapshot.child("merchantName").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.dateTextView.setText(transaction.getPurchasedDateString(transaction.getPurchaseDate()));
        holder.AmountTextView.setText(changeToRupiahFormat(transaction.getTotalPrice()));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void addTransaction (Transaction transaction) {
        if(transaction == null) return;
        if(!transactions.contains(transaction)) {
            this.transactions.add(transaction);
        }
        notifyDataSetChanged();
    }

    public void removeTransactions(){
        transactions.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView NameTextView;
        public TextView dateTextView;
        public TextView AmountTextView;

        LinearLayout bg;

        public ViewHolder(View view) {
            super(view);
            final Context context = itemView.getContext();
            // Set the holder attributes
            NameTextView = view.findViewById(R.id.username);
            dateTextView = view.findViewById(R.id.date);
            AmountTextView = view.findViewById(R.id.amount);
            bg = view.findViewById(R.id.bg_split);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, UserSplitBillDetailActivity.class);
                    intent.putExtra("transaction",transactions);
                    intent.putExtra("Position", getAdapterPosition());
                    context.startActivity(intent);
                }
            });
        }
    }

    //Change number format to IDR
    public String changeToRupiahFormat(int money){
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        String temp = formatRupiah.format((double)money);

        return temp;
    }
}
