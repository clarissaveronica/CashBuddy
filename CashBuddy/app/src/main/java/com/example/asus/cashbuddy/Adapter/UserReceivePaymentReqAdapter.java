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

import com.example.asus.cashbuddy.Activity.User.UserSentPaymentDetailActivity;
import com.example.asus.cashbuddy.Model.PaymentRequest;
import com.example.asus.cashbuddy.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class UserReceivePaymentReqAdapter extends RecyclerView.Adapter<UserReceivePaymentReqAdapter.ViewHolder> {

    //Pending Payment Items
    private ArrayList<PaymentRequest> paymentRequests;

    public UserReceivePaymentReqAdapter(@NonNull List<PaymentRequest> paymentRequests) {
        this.paymentRequests = new ArrayList<>(paymentRequests);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_user_receive_payment_req, parent, false);

        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // Get Payment Requests Item
        final PaymentRequest paymentRequest = paymentRequests.get(position);

        FirebaseDatabase.getInstance().getReference(paymentRequest.getFrom()).child(paymentRequest.getSenderRequest()).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.NameTextView.setText(dataSnapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.dateTextView.setText(paymentRequest.getRequestDateString(paymentRequest.getRequestdate()));
        holder.AmountTextView.setText(changeToRupiahFormat(paymentRequest.getAmount()));
    }

    @Override
    public int getItemCount() {
        return paymentRequests.size();
    }

    public void setPaymentRequests(ArrayList<PaymentRequest> paymentRequests) {
        this.paymentRequests = paymentRequests;
    }

    public void addPaymentRequest (PaymentRequest paymentRequest) {
        if(paymentRequest == null) return;
        if(!paymentRequests.contains(paymentRequest)) {
            this.paymentRequests.add(paymentRequest);
        }
        notifyDataSetChanged();
    }

    public void removePaymentRequest(){
        paymentRequests.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // TextView of store name
        public TextView NameTextView;
        // TextView of topup date
        public TextView dateTextView;
        // TextView of payment amount
        public TextView AmountTextView;
        // TextView of LocationTextView

        LinearLayout bg;

        public ViewHolder(View view) {
            super(view);
            final Context context = itemView.getContext();
            // Set the holder attributes
            NameTextView = view.findViewById(R.id.username);
            dateTextView = view.findViewById(R.id.date);
            AmountTextView = view.findViewById(R.id.amount);
            bg = view.findViewById(R.id.bg_pending);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, UserSentPaymentDetailActivity.class);
                    intent.putExtra("paymentReq",paymentRequests);
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
