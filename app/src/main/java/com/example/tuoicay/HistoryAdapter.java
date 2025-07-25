package com.example.tuoicay;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.tuoicay.HistoryModel;
import com.example.tuoicay.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private List<HistoryModel> historyList;

    public HistoryAdapter(Context context, List<HistoryModel> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvZone, tvStartTime, tvDuration, tvStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            tvZone = itemView.findViewById(R.id.tvZone);
            tvStartTime = itemView.findViewById(R.id.tvStartTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HistoryModel item = historyList.get(position);
        holder.tvZone.setText("Van: " + TextUtils.join(", ", item.zone));
        holder.tvStartTime.setText("Bắt đầu: " + item.startTime);
        holder.tvDuration.setText("Thời gian tưới: " + item.duration + " phút");
        holder.tvStatus.setText("Trạng thái: " + (item.status.equals("success") ? "Thành công" : "Thất bại"));
        holder.tvStatus.setTextColor(item.status.equals("success") ?
                context.getResources().getColor(android.R.color.holo_green_dark) :
                context.getResources().getColor(android.R.color.holo_red_dark));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }
}