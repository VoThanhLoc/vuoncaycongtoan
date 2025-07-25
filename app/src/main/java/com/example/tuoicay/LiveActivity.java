package com.example.tuoicay;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LiveActivity extends AppCompatActivity {

    private final int BUTTON_COUNT = 8;
    private boolean[] buttonStates = new boolean[BUTTON_COUNT];
    private boolean[] isOnArray = new boolean[BUTTON_COUNT];

    private GridLayout gridButtons;
    private Button btnStartWatering;
    private ImageButton btn_Back;

    private CardView[] cards = new CardView[BUTTON_COUNT];
    private TextView[] textViews = new TextView[BUTTON_COUNT];
    private boolean isWatering = false;
    private long startMillis = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference zoneRef = database.getReference("irrigation/zones");

        gridButtons = findViewById(R.id.gridButtons);
        btnStartWatering = findViewById(R.id.btnStartWatering);
        btn_Back = findViewById(R.id.btn_Back);

        btn_Back.setOnClickListener(view -> finish());

        LayoutInflater inflater = LayoutInflater.from(this);

        // Khởi tạo UI các card và xử lý sự kiện click
        for (int i = 0; i < BUTTON_COUNT; i++) {
            View cardView = inflater.inflate(R.layout.button_card, gridButtons, false);
            CardView card = cardView.findViewById(R.id.cardButton);
            TextView tv = cardView.findViewById(R.id.tvButton);

            cards[i] = card;
            textViews[i] = tv;

            int index = i;
            card.setOnClickListener(v -> {
                buttonStates[index] = !buttonStates[index];
                updateCardUI(cards[index], textViews[index], buttonStates[index], index);
                updateStartButtonState();

                // Cập nhật trạng thái lên Firebase
                String zoneKey = "zone" + (index + 1);
                zoneRef.child(zoneKey).child("status").setValue(buttonStates[index]);
            });

            gridButtons.addView(cardView);
        }

        // Lấy dữ liệu trạng thái ban đầu từ Firebase
        zoneRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < BUTTON_COUNT; i++) {
                    String zoneKey = "zone" + (i + 1);
                    Boolean status = snapshot.child(zoneKey).child("status").getValue(Boolean.class);
                    if (status != null) {
                        buttonStates[i] = status;
                        updateCardUI(cards[i], textViews[i], status, i);
                    }
                }
                updateStartButtonState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to read status", error.toException());
            }
        });

        btnStartWatering.setOnClickListener(v -> {
            DatabaseReference irrigationRef = database.getReference("irrigation");

            if (!isWatering) {
                // === BẮT ĐẦU TƯỚI ===
                irrigationRef.child("start").setValue(true);
                isWatering = true;

                startMillis = System.currentTimeMillis(); // Lưu lại thời gian bắt đầu tưới

                btnStartWatering.setText("Dừng tưới");
                btnStartWatering.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_red_dark));
                btnStartWatering.setTextColor(Color.WHITE);

                Toast.makeText(this, "Bắt đầu tưới nước!", Toast.LENGTH_SHORT).show();
            } else {
                // === DỪNG TƯỚI ===
                irrigationRef.child("start").setValue(false);
                isWatering = false;

                long endMillis = System.currentTimeMillis();
                String startTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(startMillis));
                String endTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(endMillis));
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(endMillis));

                long durationMillis = endMillis - startMillis;
                long durationMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis);
                String duration = String.valueOf(durationMinutes);

                // Ghi lịch sử vào Firebase
                DatabaseReference historyRef = database.getReference("history").push();

                Map<String, Object> history = new HashMap<>();
                history.put("zones", Arrays.asList("Thủ công")); // Tùy chọn: hoặc lấy vùng từ trạng thái đang mở nếu có
                history.put("startTime", startTime);
                history.put("endTime", endTime);
                history.put("duration", duration);
                history.put("repeatDays", Arrays.asList()); // không áp dụng
                history.put("status", "done");
                history.put("timestamp", timestamp);
                history.put("type", "Tưới Trực Tiếp");

                historyRef.setValue(history);

                btnStartWatering.setText("Bắt đầu tưới");
                btnStartWatering.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
                btnStartWatering.setTextColor(Color.BLACK);

                Toast.makeText(this, "Đã dừng tưới nước!", Toast.LENGTH_SHORT).show();
            }
        });
    }

        private void updateCardUI(CardView card, TextView tv, boolean isOn, int index) {
        if (isOn) {
            card.setCardBackgroundColor(Color.parseColor("#A5D6A7"));
            tv.setText("ON");
            tv.setTextColor(Color.BLACK);
        } else {
            card.setCardBackgroundColor(Color.WHITE);
            tv.setText("OFF");
            tv.setTextColor(Color.BLACK);
        }

        isOnArray[index] = isOn;

        // Kiểm tra có van nào bật không
        boolean anyOn = false;
        for (boolean status : isOnArray) {
            if (status) {
                anyOn = true;
                break;
            }
        }

        DatabaseReference irrigationRef = FirebaseDatabase.getInstance().getReference("irrigation");

        if (anyOn) {
            btnStartWatering.setEnabled(true);
            if (isWatering) {
                btnStartWatering.setText("Dừng tưới");
                btnStartWatering.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_red_dark));
                btnStartWatering.setTextColor(Color.WHITE);
            } else {
                btnStartWatering.setText("Bắt đầu tưới");
                btnStartWatering.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
                btnStartWatering.setTextColor(Color.WHITE);
            }
        } else {
            // Nếu tắt hết thì ngắt tưới và disable nút
            if (isWatering) {
                irrigationRef.child("start").setValue(false);
                isWatering = false;
                Toast.makeText(this, "Tự động dừng tưới vì đã tắt hết van!", Toast.LENGTH_SHORT).show();
            }

            btnStartWatering.setEnabled(false);
            btnStartWatering.setText("Bắt đầu tưới");
            btnStartWatering.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
            btnStartWatering.setTextColor(Color.BLACK);
        }
    }

    private void updateStartButtonState() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference irrigationRef = database.getReference("irrigation");
        boolean anyOn = false;
        for (boolean state : buttonStates) {
            if (state) {
                anyOn = true;
                break;
            }
        }
        var abc = btnStartWatering.getText().toString();
        if (!btnStartWatering.getText().toString().equals("Dừng tưới")) {
            if (anyOn) {
                btnStartWatering.setEnabled(true);
                btnStartWatering.setText("Bắt đầu tưới");
            } else {
                btnStartWatering.setText("Bắt đầu tưới");
                btnStartWatering.setEnabled(false);
                irrigationRef.child("start").setValue(false);  // <-- Thêm dòng này để tắt start nếu không có van nào bật
            }
//        btnStartWatering.setEnabled(anyOn);
//        btnStartWatering.setBackgroundTintList(ContextCompat.getColorStateList(this,
//                anyOn ? android.R.color.holo_green_dark : android.R.color.darker_gray));
//        btnStartWatering.setTextColor(anyOn ? Color.WHITE : Color.BLACK);
        }
    }
}
