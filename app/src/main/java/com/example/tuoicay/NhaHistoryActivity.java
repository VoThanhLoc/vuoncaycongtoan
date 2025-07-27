package com.example.tuoicay;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class NhaHistoryActivity extends AppCompatActivity {

    ExpandableListView expandableListView;
    HistoryExpandableAdapter adapter;
    List<String> listDates = new ArrayList<>();
    Button  btnBack;
    HashMap<String, List<HouseHistoryItem>> historyMap = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nha_lichsu);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        loadHistory();
    }
    private void loadHistory() {
        expandableListView = findViewById(R.id.expandableListView);

        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("historyHouse");
        historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dateSnap : snapshot.getChildren()) {
                    String date = dateSnap.getKey();
                    List<HouseHistoryItem> items = new ArrayList<>();

                    for (DataSnapshot itemSnap : dateSnap.getChildren()) {
                        HouseHistoryItem item = itemSnap.getValue(HouseHistoryItem.class);
                        item.id = itemSnap.getKey();
                        items.add(item);
                    }

                    listDates.add(date);
                    historyMap.put(date, items);
                }

                adapter = new HistoryExpandableAdapter(NhaHistoryActivity.this, listDates, historyMap);
                expandableListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(NhaHistoryActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
