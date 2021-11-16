package com.runners.choi.runners.record;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.runners.choi.runners.R;

import java.util.List;

public class RecordListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;

    private List<RecordItem> recordItem;

    private RelativeLayout recorditemBackground;
    private TextView dateView;

    public RecordListAdapter(Activity activity, List<RecordItem> recordItem) {
        this.activity = activity;
        this.recordItem = recordItem;
    }

    @Override
    public int getCount() {
        return recordItem.size();
    }

    @Override
    public Object getItem(int location) {
        return recordItem.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.record_item, null);

        final RecordItem item = recordItem.get(position);

        recorditemBackground = (RelativeLayout)
                convertView.findViewById(R.id.record_item_background);
        dateView = (TextView) convertView
                .findViewById(R.id.record_date);

        dateView.setText(item.getDate());

        recorditemBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               Intent i = new Intent(activity, ResultDetailActivity.class);
               i.putExtra("repeat", item.getRepeat());
               i.putExtra("point", item.getPoint());
               i.putExtra("time", item.getTime());
               i.putExtra("distance", item.getDistance());
               i.putExtra("bonus", item.getBonus());
               i.putExtra("speed", item.getSpeed());
               i.putExtra("date", item.getDate());
               activity.startActivity(i);


            }
        });

        return convertView;
        }


}