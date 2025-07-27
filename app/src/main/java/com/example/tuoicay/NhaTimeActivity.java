package com.example.tuoicay;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NhaTimeActivity extends AppCompatActivity {
    TimePicker timePicker;
    EditText editDuration;
    ListView listView;
    Button btnSave, btnBack;
    CheckBox mon, tue, wed, thu, fri, sat, sun;
    List<ScheduleHouseModel> list = new ArrayList<>();
    ScheduleHouseAdapter adapter;
    private String currentId = null;
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("schedule_house");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nha_time);

        timePicker = findViewById(R.id.timePicker);
        editDuration = findViewById(R.id.editDuration);
        listView = findViewById(R.id.listView);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        mon = findViewById(R.id.mon);
        tue = findViewById(R.id.tue);
        wed = findViewById(R.id.wed);
        thu = findViewById(R.id.thu);
        fri = findViewById(R.id.fri);
        sat = findViewById(R.id.sat);
        sun = findViewById(R.id.sun);

        adapter = new ScheduleHouseAdapter(this, list, dbRef);
        listView.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveSchedule());

        btnBack.setOnClickListener(v -> finish());

        loadData();
    }

    private void saveSchedule() {
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        String startTime = String.format("%02d:%02d", hour, minute);

        String duration = editDuration.getText().toString().trim();
        if (duration.isEmpty()) {
            Toast.makeText(this, "Nhập thời lượng tưới", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> repeatDays = new ArrayList<>();
        if (mon.isChecked()) repeatDays.add("Mon");
        if (tue.isChecked()) repeatDays.add("Tue");
        if (wed.isChecked()) repeatDays.add("Wed");
        if (thu.isChecked()) repeatDays.add("Thu");
        if (fri.isChecked()) repeatDays.add("Fri");
        if (sat.isChecked()) repeatDays.add("Sat");
        if (sun.isChecked()) repeatDays.add("Sun");

        // Tạo ID mới
        String id = dbRef.push().getKey();

        // Lưu vào schedule
        ScheduleHouseModel model = new ScheduleHouseModel(startTime, duration, repeatDays, "off");
        dbRef.child(id).setValue(model).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show();

            // Reset form
            editDuration.setText("");
            mon.setChecked(false); tue.setChecked(false); wed.setChecked(false);
            thu.setChecked(false); fri.setChecked(false); sat.setChecked(false); sun.setChecked(false);

            // ➕ Thêm vào lịch sử (history)
            Calendar calendar = Calendar.getInstance();
            String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("historyHouse").child(dateStr);;
            Map<String, Object> historyItem = new HashMap<>();
            currentId = historyRef.push().getKey(); // tạo ID mới
            historyItem.put("startTime", startTime);
            historyItem.put("endtime", startTime); // hoặc tính từ start + duration
            historyItem.put("duration", duration);
            historyItem.put("mode", "hẹn giờ");
            historyItem.put("status", "đã lên lịch");

            historyRef.child(currentId).setValue(historyItem);
        });
    }

    private void loadData() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    ScheduleHouseModel model = item.getValue(ScheduleHouseModel.class);
                    if (model != null) {
                        model.setId(item.getKey());
                        list.add(model);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
