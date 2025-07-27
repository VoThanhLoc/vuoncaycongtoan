package com.example.tuoicay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryChildAdapter extends RecyclerView.Adapter<HistoryChildAdapter.ChildViewHolder> {

    private List<HouseHistoryModel> childList;

    public HistoryChildAdapter(List<HouseHistoryModel> childList) {
        this.childList = childList;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_child, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        HouseHistoryModel item = childList.get(position);
        holder.txtTime.setText(item.getStartTime() + " - " + item.getEndtime());
        holder.txtDuration.setText(item.getDuration() + " phút");
        holder.txtMode.setText(item.getMode());
        holder.txtStatus.setText(item.getStatus());
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView txtTime, txtDuration, txtMode, txtStatus;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtDuration = itemView.findViewById(R.id.txtDuration);
            txtMode = itemView.findViewById(R.id.txtMode);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}

