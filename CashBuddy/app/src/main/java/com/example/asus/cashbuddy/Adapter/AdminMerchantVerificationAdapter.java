package com.example.asus.cashbuddy.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.asus.cashbuddy.Model.Merchant;
import com.example.asus.cashbuddy.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class AdminMerchantVerificationAdapter extends RecyclerView.Adapter<AdminMerchantVerificationAdapter.ViewHolder> {

    DatabaseReference merchantDatabase;
    // topup History Items
    private ArrayList<Merchant> merchantList;

    public AdminMerchantVerificationAdapter(@NonNull List<Merchant> merchants) {
        this.merchantList = new ArrayList<>(merchants);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_admin_merchant_verification, parent, false);

        return new ViewHolder(layoutView);
    }

    /**
     * Bind the view with data at the specified position
     * @param holder ViewHolder which should be updated
     * @param position position of items in the adapter
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // Get topup Item
        final Merchant merchant = merchantList.get(position);

        holder.NameTextView.setText(merchant.getMerchantName());
        holder.numET.setText(merchant.getPhoneNumber());
        holder.locationET.setText(merchant.getLocation());

        holder.verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                merchantDatabase = FirebaseDatabase.getInstance().getReference();

                merchantDatabase.child("phonenumbertouid").child(merchant.getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String id = dataSnapshot.getValue().toString();
                            merchantDatabase.child("role").child(id).setValue("MERCHANT");
                            merchantList.remove(position);
                            merchantList.clear();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        });
    }

    @Override
    public int getItemCount() {
        return merchantList.size();
    }

    public void setMerchantHistory(ArrayList<Merchant> merchantList) {
        this.merchantList = merchantList;
    }

    public void addMerchant(String merchantUid) {
        FirebaseDatabase.getInstance().getReference("merchant").child(merchantUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Merchant merchant = dataSnapshot.getValue(Merchant.class);
                merchantList.add(merchant);
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void removeMerchants(){
        merchantList.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // TextView of merchant name
        public TextView NameTextView, numET, locationET;
        public Button verifyButton;
        LinearLayout bg;

        public ViewHolder(View view) {
            super(view);
            final Context context = itemView.getContext();
            // Set the holder attributes
            NameTextView = view.findViewById(R.id.merchantName);
            numET = view.findViewById(R.id.merchantNum);
            locationET = view.findViewById(R.id.merchantLocation);
            bg = view.findViewById(R.id.bg_admin_verify_store);

            verifyButton = view.findViewById(R.id.verifyButton);
        }
    }
}
