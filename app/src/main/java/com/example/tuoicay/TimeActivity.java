package com.example.tuoicay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.*;

public class TimeActivity extends AppCompatActivity {

    Button btnAddSchedule, btnSaveSchedule;
    LinearLayout scheduleContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        btnAddSchedule = findViewById(R.id.btnAddSchedule);
        btnSaveSchedule = findViewById(R.id.btnSaveSchedule);
        scheduleContainer = findViewById(R.id.scheduleContainer);

        btnAddSchedule.setOnClickListener(v -> {
            View scheduleView = LayoutInflater.from(this).inflate(R.layout.schedule_item, scheduleContainer, false);
            scheduleContainer.addView(scheduleView);
        });

        btnSaveSchedule.setOnClickListener(v -> {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference scheduleRef = database.getReference("schedule");
            scheduleRef.removeValue(); // Xoá dữ liệu cũ

            int count = scheduleContainer.getChildCount();
            List<View> newViews = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                View scheduleView = scheduleContainer.getChildAt(i);

                EditText edtDuration = scheduleView.findViewById(R.id.editDuration);
                TimePicker timePicker = scheduleView.findViewById(R.id.timePickerStart);

                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                String timeStart = String.format("%02d:%02d", hour, minute);

                String duration = edtDuration.getText().toString();

                CheckBox[] dayChecks = {
                        scheduleView.findViewById(R.id.checkMon),
                        scheduleView.findViewById(R.id.checkTue),
                        scheduleView.findViewById(R.id.checkWed),
                        scheduleView.findViewById(R.id.checkThu),
                        scheduleView.findViewById(R.id.checkFri),
                        scheduleView.findViewById(R.id.checkSat),
                        scheduleView.findViewById(R.id.checkSun),
                };

                String[] dayKeys = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                List<String> days = new ArrayList<>();
                for (int d = 0; d < dayChecks.length; d++) {
                    if (dayChecks[d].isChecked()) {
                        days.add(dayKeys[d]);
                    }
                }

                CheckBox[] zoneChecks = {
                        scheduleView.findViewById(R.id.checkZone1),
                        scheduleView.findViewById(R.id.checkZone2),
                        scheduleView.findViewById(R.id.checkZone3),
                        scheduleView.findViewById(R.id.checkZone4),
                        scheduleView.findViewById(R.id.checkZone5),
                        scheduleView.findViewById(R.id.checkZone6),
                        scheduleView.findViewById(R.id.checkZone7),
                };

                List<String> zones = new ArrayList<>();
                for (int z = 0; z < zoneChecks.length; z++) {
                    if (zoneChecks[z].isChecked()) {
                        zones.add("zone" + (z + 1));
                    }
                }

                // Push lên Firebase
                Map<String, Object> scheduleData = new HashMap<>();
                scheduleData.put("startTime", timeStart);
                scheduleData.put("duration", duration);
                scheduleData.put("repeatDays", days);
                scheduleData.put("zones", zones);

                scheduleRef.push().setValue(scheduleData);

                // UI rút gọn lại
                TextView item = new TextView(this);
                item.setPadding(8, 8, 8, 8);
                item.setBackgroundColor(0xFFE0F7FA);
                item.setText("⏰ " + timeStart + " - " + duration + " phút\nVòi: " + zones + "\nNgày: " + days);
                newViews.add(item);
            }

            // Xoá các layout gốc và hiển thị TextView đơn giản
            scheduleContainer.removeAllViews();
            for (View vs : newViews) {
                scheduleContainer.addView(vs);
            }

            Toast.makeText(this, "Đã lưu lịch tưới", Toast.LENGTH_SHORT).show();
        });
    }
}
