package com.example.weather;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] itemData;
    private final String[] itemTemp;
    private final String[] itemHumidity; // Add humidity data
    private final Integer[] icons;
    private final String unit;

    public CustomListAdapter(Activity context, String[] itemData, String[] itemTemp, String[] itemHumidity, Integer[] icons, String unit) {
        super(context, R.layout.my_list, itemData);
        this.context = context;
        this.itemData = itemData;
        this.itemTemp = itemTemp;
        this.itemHumidity = itemHumidity; // Initialize humidity
        this.icons = icons;
        this.unit = unit;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.my_list, null, true);

        TextView itemText = rowView.findViewById(R.id.txt_date);
        TextView tempText = rowView.findViewById(R.id.txt_temp);
        ImageView icon = rowView.findViewById(R.id.icon);

        itemText.setText(itemData[position]);
        String tempDisplay = itemTemp[position] + (unit.equals("C") ? " °C" : " °F");
        tempText.setText(tempDisplay);
        icon.setImageResource(icons[position]);

        // Handle item click
        rowView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DayViewActivity.class);
            intent.putExtra("date", itemData[position]);
            intent.putExtra("temperature", itemTemp[position]);
            intent.putExtra("humidity", itemHumidity[position]); // Pass humidity data
            intent.putExtra("icon", icons[position]);
            intent.putExtra("unit", unit);
            context.startActivity(intent);
        });

        return rowView;
    }
}
