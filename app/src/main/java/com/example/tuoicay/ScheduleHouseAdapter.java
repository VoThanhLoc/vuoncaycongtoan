package com.example.tuoicay;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class ScheduleHouseAdapter extends BaseAdapter {
    Context context;
    List<ScheduleHouseModel> list;
    LayoutInflater inflater;
    DatabaseReference dbRef;

    public ScheduleHouseAdapter(Context context, List<ScheduleHouseModel> list, DatabaseReference dbRef) {
        this.context = context;
        this.list = list;
        this.dbRef = dbRef;
        this.inflater = LayoutInflater.from(context);
    }

    @Override public int getCount() { return list.size(); }

    @Override public Object getItem(int i) { return list.get(i); }

    @Override public long getItemId(int i) { return i; }

    @Override public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.schedule_home_item, null);
        TextView txtInfo = view.findViewById(R.id.txtInfo);
        Button btnDelete = view.findViewById(R.id.btnDelete);
        Button btnStop = view.findViewById(R.id.btnStop);

        ScheduleHouseModel model = list.get(i);

        txtInfo.setText("Tưới lúc " + model.getStartTime() + " trong " + model.getDuration() + " phút\nNgày: " + model.getRepeatDays());

        if ("on".equals(model.getStatus())) {
            btnStop.setEnabled(true);
            btnStop.setBackgroundColor(Color.RED);
            btnStop.setTextColor(Color.WHITE);
        } else {
            btnStop.setEnabled(false);
        }

        btnStop.setOnClickListener(v -> {
            dbRef.child(model.getId()).child("status").setValue("off");
        });

        btnDelete.setOnClickListener(v -> {
            dbRef.child(model.getId()).removeValue();
        });

        return view;
    }
}
