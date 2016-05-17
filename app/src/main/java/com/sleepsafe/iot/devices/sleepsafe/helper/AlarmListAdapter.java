package com.sleepsafe.iot.devices.sleepsafe.helper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.sleepsafe.iot.devices.sleepsafe.R;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class AlarmListAdapter extends ArrayAdapter<Alarm> {
    private Context mContext;
    private LayoutInflater mInflater;

    public AlarmListAdapter(Context context, int resource) {
        super(context, resource);
        mContext = context;
        mInflater = (LayoutInflater.from(context));
    }



    public AlarmListAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId, new ArrayList<Alarm>());
        mContext = context;
        mInflater = (LayoutInflater.from(context));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null) {
            view = mInflater.inflate(R.layout.alarm_list_item, parent, false);
        }
        final Alarm a = getItem(position);
        TextView val = (TextView) view.findViewById(R.id.alarm_list_item_text);
        ImageView del = (ImageView) view.findViewById(R.id.alarm_delete_button);
        if (a != null && val != null) {
            if (a.ismLessThan()) val.setText("Less than " + a.getmValue());
            else val.setText("Greater than " + a.getmValue());
        }
        if (del != null) {
            del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(a);
                    AlarmListAdapter.this.notifyDataSetChanged();
                }
            });
        }
        return view;
    }
}
