package com.example.tuoicay;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.tuoicay.R;
import com.example.tuoicay.ScheduleModel;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
    private List<ScheduleModel> schedules;
    private DatabaseReference dbRef;

    public ScheduleAdapter(List<ScheduleModel> list, DatabaseReference dbRef) {
        this.schedules = list;
        this.dbRef = dbRef;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvZone, tvTime, tvDays;
        View statusIndicator;
        Button btnStop, btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvZone = itemView.findViewById(R.id.tvZone);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDays = itemView.findViewById(R.id.tvDays);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            btnStop = itemView.findViewById(R.id.btnStop);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ScheduleModel item = schedules.get(position);
        holder.tvZone.setText("Zone: " + TextUtils.join(", ", item.zone));
        holder.tvTime.setText("Time: " + item.startTime + " - " + item.duration + " phút");
        holder.tvDays.setText("Days: " + TextUtils.join(", ", item.repeatDays));

        if ("on".equals(item.status)) {
            holder.statusIndicator.setBackgroundResource(R.drawable.status_red);
            holder.btnStop.setVisibility(View.VISIBLE);
        } else {
            holder.statusIndicator.setBackgroundResource(R.drawable.status_green);
            holder.btnStop.setVisibility(View.GONE);
        }

        holder.btnDelete.setOnClickListener(v -> {
            dbRef.child(item.id).removeValue();
        });

        holder.btnStop.setOnClickListener(v -> {
            dbRef.child(item.id).child("status").setValue("off");
        });
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }
}