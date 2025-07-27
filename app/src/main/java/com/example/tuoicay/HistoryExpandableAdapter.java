package com.example.tuoicay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class HistoryExpandableAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listDates;
    private HashMap<String, List<HouseHistoryItem>> historyMap;

    public HistoryExpandableAdapter(Context context, List<String> listDates, HashMap<String, List<HouseHistoryItem>> historyMap) {
        this.context = context;
        this.listDates = listDates;
        this.historyMap = historyMap;
    }

    @Override
    public int getGroupCount() {
        return listDates.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String date = listDates.get(groupPosition);
        List<HouseHistoryItem> items = historyMap.get(date);
        return items != null ? items.size() : 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listDates.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return historyMap.get(listDates.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String date = (String) getGroup(groupPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.house_group_item, parent, false);
        }
        TextView tvDate = convertView.findViewById(R.id.tvDate);
        tvDate.setText(date);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        HouseHistoryItem item = (HouseHistoryItem) getChild(groupPosition, childPosition);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.house_child_item, parent, false);
        }

        TextView tvStartTime = convertView.findViewById(R.id.tvStartTime);
        TextView tvDuration = convertView.findViewById(R.id.tvDuration);
        TextView tvEndTime = convertView.findViewById(R.id.tvEndTime);
        TextView tvMode = convertView.findViewById(R.id.tvMode);
        TextView tvStatus = convertView.findViewById(R.id.tvStatus);
        ImageButton btnDelete = convertView.findViewById(R.id.btnDeleteHistory);

        tvStartTime.setText("Bắt đầu: " + item.startTime);
        tvDuration.setText("Thời lượng: " + item.duration + " phút");
        tvEndTime.setText("Kết thúc: " + item.endtime);
        tvMode.setText("Chế độ: " + item.mode);
        tvStatus.setText("Trạng thái: " + item.status);

        // Xử lý nút Xóa
        btnDelete.setOnClickListener(v -> {
            String date = listDates.get(groupPosition); // nhóm ngày hiện tại
            String historyId = item.id;

            if (historyId != null && !historyId.isEmpty()) {
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("historyHouse")
                        .child(date)
                        .child(historyId);

                ref.removeValue().addOnSuccessListener(unused -> {
                    // Xóa khỏi danh sách local
                    historyMap.get(date).remove(childPosition);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Đã xóa lịch sử", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(context, "Không thể xác định ID để xóa", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}