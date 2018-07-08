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

import com.example.asus.cashbuddy.Activity.User.UserReceiveSplitBillDetailActivity;
import com.example.asus.cashbuddy.Model.SplitBill;
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


public class UserReceiveSplitBillAdapter extends RecyclerView.Adapter<UserReceiveSplitBillAdapter.ViewHolder> {

    //Pending Payment Items
    private ArrayList<SplitBill> splitBills;

    public UserReceiveSplitBillAdapter(@NonNull List<SplitBill> splitBills) {
        this.splitBills = new ArrayList<>(splitBills);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_user_receive_payment_req, parent, false);

        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // Get Payment Requests Item
        final SplitBill splitBill = splitBills.get(position);
        long remaining = splitBill.getRequestdate() + (5 * 24 * 60 * 60 * 1000) - Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime() ;
        long day = (remaining / (60*60*24*1000));

        FirebaseDatabase.getInstance().getReference("users").child(splitBill.getSender()).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                 holder.NameTextView.setText(dataSnapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.dateTextView.setText(splitBill.getRequestDateString(splitBill.getRequestdate()));
        holder.AmountTextView.setText(changeToRupiahFormat(splitBill.getAmount()));
        holder.expiry.setText(day + "D");
    }

    @Override
    public int getItemCount() {
        return splitBills.size();
    }

    public void setSplitBills(ArrayList<SplitBill> splitBills) {
        this.splitBills = splitBills;
    }

    public void addSplitBills (SplitBill splitBill) {
        if(splitBill == null) return;
        if(!splitBills.contains(splitBill)) {
            this.splitBills.add(splitBill);
        }
        notifyDataSetChanged();
    }

    public void removeSplitBills(){
        splitBills.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView NameTextView;
        public TextView dateTextView;
        public TextView AmountTextView;
        public TextView expiry;

        LinearLayout bg;

        public ViewHolder(View view) {
            super(view);
            final Context context = itemView.getContext();
            // Set the holder attributes
            NameTextView = view.findViewById(R.id.username);
            dateTextView = view.findViewById(R.id.date);
            AmountTextView = view.findViewById(R.id.amount);
            expiry = view.findViewById(R.id.expiry);
            bg = view.findViewById(R.id.bg_split);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, UserReceiveSplitBillDetailActivity.class);
                    intent.putExtra("splitbills",splitBills);
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
