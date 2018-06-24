package com.example.asus.cashbuddy.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.asus.cashbuddy.Activity.Admin.AdminConfirmTopUpDetailActivity;
import com.example.asus.cashbuddy.Model.TopUp;
import com.example.asus.cashbuddy.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class AdminConfirmTopUpAdapter extends RecyclerView.Adapter<AdminConfirmTopUpAdapter.ViewHolder> {

    // topup History Items
    private ArrayList<TopUp> topUpHistory;

    public AdminConfirmTopUpAdapter(@NonNull List<TopUp> topup) {
        this.topUpHistory = new ArrayList<>(topup);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_admin_confirm_top_up, parent, false);

        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // Get topup Item
        final TopUp topup = topUpHistory.get(position);

        FirebaseDatabase.getInstance().getReference("users").child(topup.getUid()).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.NameTextView.setText(dataSnapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.topupDateTextView.setText(topup.getRequestDateString(topup.getRequestdate()));
        holder.AmountTextView.setText(changeToRupiahFormat(topup.getAmount()));

        if(topup.getRequeststatus()==1) {
            holder.bg.setBackgroundColor(Color.LTGRAY);
        }
        else holder.bg.setBackgroundColor(Color.WHITE);

    }

    @Override
    public int getItemCount() {
        return topUpHistory.size();
    }

    public void settopupHistory(ArrayList<TopUp> topupHistory) {
        this.topUpHistory = topupHistory;
    }

    public void addTopUp(TopUp topUp) {
        if(topUp == null) return;
        this.topUpHistory.add(topUp);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // TextView of store name
        public TextView NameTextView;
        // TextView of topup date
        public TextView topupDateTextView;
        // TextView of payment amount
        public TextView AmountTextView;
        // TextView of LocationTextView

        LinearLayout bg;

        public ViewHolder(View view) {
            super(view);
            final Context context = itemView.getContext();
            // Set the holder attributes
            NameTextView = view.findViewById(R.id.username);
            topupDateTextView = view.findViewById(R.id.date);
            AmountTextView = view.findViewById(R.id.amount);
            bg = view.findViewById(R.id.bg_topup);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AdminConfirmTopUpDetailActivity.class);
                    intent.putExtra("topup",topUpHistory);
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
