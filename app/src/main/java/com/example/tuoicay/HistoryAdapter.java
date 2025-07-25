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

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private List<HistoryModel> historyList;

    public HistoryAdapter(Context context, List<HistoryModel> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvZone, tvStartTime, tvDuration, tvStatus,tvGioTao,tvKieu;

        public ViewHolder(View itemView) {
            super(itemView);
            tvZone = itemView.findViewById(R.id.tvZone);
            tvStartTime = itemView.findViewById(R.id.tvStartTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvGioTao = itemView.findViewById(R.id.tvGioTao);
            tvKieu = itemView.findViewById(R.id.tvKieu);
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
        holder.tvZone.setText("Van: " + TextUtils.join(", ", item.getZones()));
        holder.tvStartTime.setText("Bắt đầu: " + item.getStartTime());
        if(item.getType().toString().equals("Hẹn Giờ")){
        holder.tvStartTime.setText("Kết thúc: " + sumTime(item.getStartTime(), item.getDuration()));
        }else {
            holder.tvStartTime.setText("Kết thúc: " + item.getEndTime());
        }
        holder.tvDuration.setText("Thời gian tưới: " + item.getDuration() + " phút");
        holder.tvGioTao.setText("Giờ thực hiện: " + item.getTimestamp() + " phút");
        holder.tvKieu.setText("Kiểu tưới: " + item.getType());
        holder.tvStatus.setText("Trạng thái: " + (item.getStatus().equals("done") ? "Hoàn tất" : "Chưa tưới"));
        holder.tvStatus.setTextColor(item.getStatus().equals("done") ?
                context.getResources().getColor(android.R.color.holo_green_dark) :
                context.getResources().getColor(android.R.color.holo_red_dark));
    }

    public String sumTime(String startTime,String duration)
    {

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            Date startDate = sdf.parse(startTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            int durationnum = Integer.parseInt(duration);
            calendar.add(Calendar.MINUTE, durationnum);

            String endTime = sdf.format(calendar.getTime());
            return endTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Lỗi giờ";
    }


    @Override
    public int getItemCount() {
        return historyList.size();
    }
}