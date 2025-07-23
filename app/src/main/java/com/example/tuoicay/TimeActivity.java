package com.example.tuoicay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeActivity extends AppCompatActivity {
    Button btnAddSchedule;
    Button btnSaveSchedule;
    LinearLayout scheduleContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        btnAddSchedule = findViewById(R.id.btnAddSchedule);
        btnSaveSchedule = findViewById(R.id.btnSaveSchedule);
        scheduleContainer = findViewById(R.id.scheduleContainer); // ⚠️ Bị thiếu dòng này

        btnAddSchedule.setOnClickListener(v -> {
            View scheduleView = LayoutInflater.from(this).inflate(R.layout.schedule_item, scheduleContainer, false);
            scheduleContainer.addView(scheduleView);
        });

        btnSaveSchedule.setOnClickListener(v -> {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference scheduleRef = database.getReference("schedule");
            scheduleRef.removeValue(); // Xoá cũ nếu cần

            int count = scheduleContainer.getChildCount();
            for (int i = 0; i < count; i++) {
                View scheduleView = scheduleContainer.getChildAt(i);

                EditText edtDuration = scheduleView.findViewById(R.id.editDuration);
                TimePicker timePicker = scheduleView.findViewById(R.id.timePickerStart);

                CheckBox[] dayChecks = new CheckBox[]{
                        scheduleView.findViewById(R.id.checkMon),
                        scheduleView.findViewById(R.id.checkTue),
                        scheduleView.findViewById(R.id.checkWed),
                        scheduleView.findViewById(R.id.checkThu),
                        scheduleView.findViewById(R.id.checkFri),
                        scheduleView.findViewById(R.id.checkSat),
                        scheduleView.findViewById(R.id.checkSun),
                };

                CheckBox[] zoneChecks = new CheckBox[]{
                        scheduleView.findViewById(R.id.checkZone1),
                        scheduleView.findViewById(R.id.checkZone2),
                        scheduleView.findViewById(R.id.checkZone3),
                        scheduleView.findViewById(R.id.checkZone4),
                        scheduleView.findViewById(R.id.checkZone5),
                        scheduleView.findViewById(R.id.checkZone6),
                        scheduleView.findViewById(R.id.checkZone7),
                        scheduleView.findViewById(R.id.checkZone8),
                };

                Map<String, Object> scheduleData = new HashMap<>();

                // Lấy thời gian bắt đầu
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                String timeStart = String.format("%02d:%02d", hour, minute);
                scheduleData.put("startTime", timeStart);

                // Thời lượng tưới
                scheduleData.put("duration", edtDuration.getText().toString());

                // Ngày lặp
                List<String> days = new ArrayList<>();
                String[] dayKeys = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                for (int d = 0; d < dayChecks.length; d++) {
                    if (dayChecks[d].isChecked()) {
                        days.add(dayKeys[d]);
                    }
                }
                scheduleData.put("repeatDays", days);

                // Van tưới
                List<String> enabledZones = new ArrayList<>();
                for (int z = 0; z < zoneChecks.length; z++) {
                    if (zoneChecks[z].isChecked()) {
                        enabledZones.add("zone" + (z + 1));
                    }
                }
                scheduleData.put("zones", enabledZones);

                // Đẩy lên Firebase
                scheduleRef.push().setValue(scheduleData);
            }

            Toast.makeText(this, "Đã lưu lịch tưới", Toast.LENGTH_SHORT).show();
        });
    }
}