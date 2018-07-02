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

import com.example.asus.cashbuddy.Activity.Admin.AdminConfirmWithdrawalDetailActivity;
import com.example.asus.cashbuddy.Model.Withdraw;
import com.example.asus.cashbuddy.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminConfirmWithdrawAdapter extends RecyclerView.Adapter<AdminConfirmWithdrawAdapter.ViewHolder> {

    // Withdraw History Items
    private ArrayList<Withdraw> withdrawHistory;

    public AdminConfirmWithdrawAdapter(@NonNull List<Withdraw> withdraw) {
        this.withdrawHistory = new ArrayList<>(withdraw);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_admin_confirm_withdraw, parent, false);

        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // Get withdraw Item
        final Withdraw withdraw = withdrawHistory.get(position);

        FirebaseDatabase.getInstance().getReference(withdraw.getRole()).child(withdraw.getUid())
                .addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(withdraw.getRole().equals("merchant")) {
                    if (dataSnapshot.child("merchantName").getValue() != null) {
                        holder.NameTextView.setText(dataSnapshot.child("merchantName").getValue().toString());
                    }
                }else {
                    if (dataSnapshot.child("name").getValue() != null) {
                        holder.NameTextView.setText(dataSnapshot.child("name").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.withdrawDateTextView.setText(withdraw.getRequestDateString(withdraw.getRequestdate()));
        holder.AmountTextView.setText("Rp " + String.valueOf(withdraw.getAmount()));
    }

    @Override
    public int getItemCount() {
        return withdrawHistory.size();
    }

    public void setwithdrawHistory(ArrayList<Withdraw> withdrawHistory) {
        this.withdrawHistory = withdrawHistory;
    }

    public void addWithdraw(Withdraw withdraw) {
        if(withdraw == null) return;
        if(!withdrawHistory.contains(withdraw)) {
            this.withdrawHistory.add(withdraw);
        }
        notifyDataSetChanged();
    }

    public void removeWithdraw(){
        withdrawHistory.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // TextView of store name
        public TextView NameTextView;
        // TextView of withdraw date
        public TextView withdrawDateTextView;
        // TextView of payment amount
        public TextView AmountTextView;
        // TextView of LocationTextView

        LinearLayout bg;

        /**
         * Construct {@link ViewHolder} instance
         * @param view layout view of withdraw items
         */
        public ViewHolder(View view) {
            super(view);
            final Context context = itemView.getContext();
            // Set the holder attributes
            NameTextView = view.findViewById(R.id.username);
            withdrawDateTextView = view.findViewById(R.id.date);
            AmountTextView = view.findViewById(R.id.amount);
            bg = view.findViewById(R.id.bg_withdraw);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AdminConfirmWithdrawalDetailActivity.class);
                    intent.putExtra("withdraw",withdrawHistory);
                    intent.putExtra("Position", getAdapterPosition());
                    context.startActivity(intent);
                }
            });
        }
    }
}
