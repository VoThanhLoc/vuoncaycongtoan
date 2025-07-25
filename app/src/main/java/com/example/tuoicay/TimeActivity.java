package com.example.tuoicay;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tuoicay.R;
import com.example.tuoicay.ScheduleAdapter;
import com.example.tuoicay.ScheduleModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TimeActivity extends AppCompatActivity {
    private LinearLayout zoneContainer, dayContainer;
    private TimePicker timePicker;
    private EditText etDuration;
    private Button btnAddSchedule;
    private RecyclerView scheduleRecyclerView;
    private ScheduleAdapter adapter;
    private List<ScheduleModel> scheduleList = new ArrayList<>();
    private DatabaseReference databaseRef;
    private ImageButton btn_Back;
    private String[] vanOptions = {"Van1", "Van2", "Van3", "Van4", "Van5", "Van6", "Van7"};
    private String[] dayOptions = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        zoneContainer = findViewById(R.id.zoneContainer);
        dayContainer = findViewById(R.id.dayContainer);
        timePicker = findViewById(R.id.timePicker);
        etDuration = findViewById(R.id.etDuration);
        btnAddSchedule = findViewById(R.id.btnAddSchedule);
        scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView);

        databaseRef = FirebaseDatabase.getInstance().getReference("schedule");

        setupCheckBoxes();
        setupRecyclerView();

        btnAddSchedule.setOnClickListener(v -> addSchedule());

        loadSchedulesFromFirebase();

        btn_Back = findViewById(R.id.btn_Back);

        btn_Back.setOnClickListener(view -> finish());
    }

    private void setupCheckBoxes() {
        for (String van : vanOptions) {
            CheckBox cb = new CheckBox(this);
            cb.setText(van);
            zoneContainer.addView(cb);
        }

        for (String day : dayOptions) {
            CheckBox cb = new CheckBox(this);
            cb.setText(day);
            dayContainer.addView(cb);
        }
    }

    private void setupRecyclerView() {
        adapter = new ScheduleAdapter(scheduleList, databaseRef);
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scheduleRecyclerView.setAdapter(adapter);
    }

    private void addSchedule() {
        List<String> selectedVans = new ArrayList<>();
        for (int i = 0; i < zoneContainer.getChildCount(); i++) {
            CheckBox cb = (CheckBox) zoneContainer.getChildAt(i);
            if (cb.isChecked()) selectedVans.add(cb.getText().toString());
        }

        List<String> selectedDays = new ArrayList<>();
        for (int i = 0; i < dayContainer.getChildCount(); i++) {
            CheckBox cb = (CheckBox) dayContainer.getChildAt(i);
            if (cb.isChecked()) selectedDays.add(cb.getText().toString());
        }

        String hour = String.format(Locale.getDefault(), "%02d:%02d",
                timePicker.getHour(), timePicker.getMinute());

        String duration = etDuration.getText().toString();

        String id = databaseRef.push().getKey();
        ScheduleModel schedule = new ScheduleModel(id, selectedVans, hour, duration, selectedDays, "off");
        databaseRef.child(id).setValue(schedule);
    }

    private void loadSchedulesFromFirebase() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                scheduleList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ScheduleModel model = child.getValue(ScheduleModel.class);
                    scheduleList.add(model);
                }
                adapter.notifyDataSetChanged();
            }

            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}