package com.example.tuoicay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HouseHistoryAdapter extends RecyclerView.Adapter<HouseHistoryAdapter.HistoryViewHolder> {

    private List<HouseHistoryGroup> groupList;

    public HouseHistoryAdapter(List<HouseHistoryGroup> groupList) {
        this.groupList = groupList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_group, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HouseHistoryGroup group = groupList.get(position);
        holder.txtDate.setText(group.getDate());

        // Hiển thị danh sách con
        HistoryChildAdapter childAdapter = new HistoryChildAdapter(group.getHistoryList());
        holder.rvChildren.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.rvChildren.setAdapter(childAdapter);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate;
        RecyclerView rvChildren;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
            rvChildren = itemView.findViewById(R.id.rvChildren);
        }
    }
}

