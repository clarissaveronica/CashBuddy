package com.example.asus.cashbuddy.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.example.asus.cashbuddy.Activity.User.UserDealsDetailActivity;
import com.example.asus.cashbuddy.Model.Deals;
import com.example.asus.cashbuddy.R;

import java.util.ArrayList;
import java.util.List;

public class UserDealsAdapter extends RecyclerView.Adapter<UserDealsAdapter.ViewHolder> {

    private ArrayList<Deals> dealsArrayList;
    private Context context;

    public UserDealsAdapter(@NonNull List<Deals> deals) {
        this.dealsArrayList = new ArrayList<>(deals);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_user_deals, parent, false);

        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // Get deals
        final Deals deals = dealsArrayList.get(position);
        if(deals.getPicUrl() != null) {
            Glide.with(context.getApplicationContext())
                    .load(deals.getPicUrl())
                    .into(holder.dealPic);
        }
    }

    @Override
    public int getItemCount() {
        return dealsArrayList.size();
    }

    public void setDealsArrayList(ArrayList<Deals> dealsArrayList) {
        this.dealsArrayList = dealsArrayList;
    }

    public void addDeals(Deals deals) {
        if(deals == null) return;
        if(!dealsArrayList.contains(deals)) {
            this.dealsArrayList.add(deals);
        }
        notifyDataSetChanged();
    }

    public void removeDeals(){
        dealsArrayList.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView dealPic;
        LinearLayout bg;

        public ViewHolder(View view) {
            super(view);
            context = itemView.getContext();
            // Set the holder attributes
            dealPic = view.findViewById(R.id.dealsPic);
            bg = view.findViewById(R.id.bg_deals);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, UserDealsDetailActivity.class);
                    intent.putExtra("deals",dealsArrayList);
                    intent.putExtra("Position", getAdapterPosition());
                    context.startActivity(intent);
                }
            });
        }
    }
}
