package com.unizar.seu.coffeConnect.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.unizar.seu.coffeConnect.R;

public class CoffeeAdapter extends BaseAdapter {

    private final Context mContext;
    private final Coffee[] coffees;

    public CoffeeAdapter(Context context, Coffee[] coffees) {
        this.mContext = context;
        this.coffees = coffees;
    }

    @Override
    public int getCount() {
        return coffees.length;
    }

    @Override
    public long getItemId(int position) {
//        return 0;
        return coffees[position].getWater();
    }

    @Override
    public Object getItem(int position) {
//        return null;
        return coffees[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
        }

        ImageView imgCoffee = convertView.findViewById(R.id.imgCoffee);
        TextView nameCoffee = convertView.findViewById(R.id.coffe_title);
        TextView aroma = convertView.findViewById(R.id.txtAroma);
        TextView water = convertView.findViewById(R.id.txtWater);
        TextView temp = convertView.findViewById(R.id.txtTemp);

        final Coffee item = (Coffee) getItem(position);
        imgCoffee.setImageResource(item.getIdImage());
        nameCoffee.setText(item.getName());
        aroma.setText(String.valueOf(item.getAroma()));
        water.setText(String.valueOf(item.getWater()));
        temp.setText(item.getTemp());
        return convertView;
    }

}
