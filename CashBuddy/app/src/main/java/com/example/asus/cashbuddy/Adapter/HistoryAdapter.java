package com.example.asus.cashbuddy.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.asus.cashbuddy.Model.History;
import com.example.asus.cashbuddy.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    // topup History Items
    private ArrayList<History> historyList;

    public HistoryAdapter(@NonNull List<History> history) {
        this.historyList = new ArrayList<>(history);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_history, parent, false);

        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // Get topup Item
        final History history = historyList.get(position);

        holder.desc.setText(history.getDesc());
        holder.type.setText(history.getType());
        holder.date.setText(history.getDateString(history.getDate()));
        if(history.getType().equals("Payment Request Accepted") || history.getType().equals("Withdraw") || history.getType().equals("Send CB Cash") || history.getType().equals("Purchase")) {
            holder.amount.setTextColor(holder.context.getResources().getColor(R.color.red));
            holder.amount.setText("-" + changeToRupiahFormat(history.getAmount()));
        }else{
            holder.amount.setTextColor(holder.context.getResources().getColor(R.color.green));
            holder.amount.setText("+" + changeToRupiahFormat(history.getAmount()));
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void setHistory(ArrayList<History> history) {
        this.historyList = history;
    }

    public void addHistory(History history) {
        if(history == null) return;
        if(!historyList.contains(history)) {
            this.historyList.add(history);
        }
        notifyDataSetChanged();
    }

    public void removeHistory(){
        historyList.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView desc;
        public TextView type;
        public TextView amount;
        public TextView date;
        public Context context;

        LinearLayout bg;

        public ViewHolder(View view) {
            super(view);
            context = itemView.getContext();
            // Set the holder attributes
            desc = view.findViewById(R.id.desc);
            type = view.findViewById(R.id.historyType);
            amount = view.findViewById(R.id.amount);
            date = view.findViewById(R.id.date);
            bg = view.findViewById(R.id.bg_history);
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
