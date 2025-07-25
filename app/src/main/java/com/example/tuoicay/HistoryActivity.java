package com.example.tuoicay;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private ImageButton btn_Back;
    RecyclerView historyRecyclerView;
    List<HistoryModel> historyList;
    HistoryAdapter historyAdapter;
    DatabaseReference historyRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        btn_Back = findViewById(R.id.btn_Back);

        btn_Back.setOnClickListener(view -> finish());
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(this, historyList);
        historyRecyclerView.setAdapter(historyAdapter);

        historyRef = FirebaseDatabase.getInstance().getReference("history");

        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                historyList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    HistoryModel item = data.getValue(HistoryModel.class);
                    historyList.add(item);
                }
                historyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HistoryActivity.this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
