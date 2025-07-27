package com.example.tuoicay;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class NhaLiveActivity extends AppCompatActivity {
    Switch customSwitch;
    ImageButton btn_Back;
    TextView txtStatus;
    DatabaseReference irrigationRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nha_live);
        customSwitch = findViewById(R.id.customSwitch);
        btn_Back = findViewById(R.id.btn_Back);
        btn_Back.setOnClickListener(view -> finish());
        // Đặt trạng thái ban đầu (ví dụ: tắt)
        customSwitch.setChecked(false);
        // Lắng nghe sự kiện thay đổi trạng thái của Switch
        customSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            private String currentId = null;
            private long startMillis = 0;

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DatabaseReference pumpRef = FirebaseDatabase.getInstance()
                        .getReference("irrigationHouse")
                        .child("start");

                // Thời gian hiện tại
                Calendar calendar = Calendar.getInstance();
                String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
                String timeStr = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime());

                DatabaseReference historyRef = FirebaseDatabase.getInstance()
                        .getReference("historyHouse")
                        .child(dateStr);

                if (isChecked) {
                    // Khi bật máy bơm: tạo 1 mục lịch sử mới
                    currentId = historyRef.push().getKey(); // tạo ID mới
                    startMillis = System.currentTimeMillis(); // ghi lại thời gian bắt đầu

                    HashMap<String, Object> data = new HashMap<>();
                    data.put("startTime", timeStr);
                    data.put("mode", "thủ công");
                    data.put("status", "đang tưới");

                    historyRef.child(currentId).setValue(data);

                    pumpRef.setValue(true).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(NhaLiveActivity.this, "Máy Bơm ĐANG BẬT (ON)", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NhaLiveActivity.this, "Lỗi bật máy bơm", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    // Khi tắt máy bơm: cập nhật duration + endTime
                    long endMillis = System.currentTimeMillis();
                    long durationMin = (endMillis - startMillis) / 60000; // đổi sang phút
                    String endTimeStr = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime());

                    if (currentId != null) {
                        HashMap<String, Object> update = new HashMap<>();
                        update.put("endtime", endTimeStr);
                        update.put("duration", String.valueOf(durationMin));
                        update.put("status",  "hoàn tất");

                        historyRef.child(currentId).updateChildren(update);
                    }

                    pumpRef.setValue(false).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(NhaLiveActivity.this, "Máy Bơm ĐANG TẮT (OFF)", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NhaLiveActivity.this, "Lỗi tắt máy bơm", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        irrigationRef = FirebaseDatabase.getInstance().getReference("irrigationHouse/start");
        irrigationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean value = snapshot.getValue(Boolean.class);
                boolean isOn = value != null && value;
                customSwitch.setChecked(isOn);
                updateStatusText(isOn);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NhaLiveActivity.this, "Lỗi tải trạng thái máy bơm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatusText(boolean isOn) {
        if (txtStatus != null) {
            txtStatus.setText(isOn ? "Trạng thái: ĐANG TƯỚI" : "Trạng thái: ĐÃ TẮT");
        }
    }
}
