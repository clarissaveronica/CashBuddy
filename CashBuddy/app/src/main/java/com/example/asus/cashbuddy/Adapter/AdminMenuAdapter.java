package com.example.asus.cashbuddy.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.asus.cashbuddy.R;

/**
 * Created by ASUS on 3/17/2018.
 */

public class AdminMenuAdapter extends BaseAdapter {

        Context context;
        private final String[] values;
        private final String [] numbers;
        private final String[] TAG;

        public AdminMenuAdapter(Context context, String[] values, String[] numbers, String[] tag){
            this.context = context;
            this.values = values;
            this.numbers = numbers;
            this.TAG = tag;
        }

        @Override
        public int getCount() {
            return values.length;
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;

            final View result;

            if (convertView == null) {

                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.adapter_admin_menu, parent, false);
                viewHolder.txtName = (TextView) convertView.findViewById(R.id.aNametxt);
                viewHolder.txtVersion = (TextView) convertView.findViewById(R.id.aVersiontxt);

                result=convertView;

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
                result=convertView;
            }

            viewHolder.txtName.setText(values[position]);
            viewHolder.txtVersion.setText(numbers[position]);

            return convertView;
        }

        private static class ViewHolder {
            TextView txtName;
            TextView txtVersion;
        }

}
