package com.example.tuoicay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class NhaMainActivity extends AppCompatActivity {
    CardView btn_nhalive;
    CardView btn_nhatime;
    CardView btn_nhalichsu;
    CardView btn_nhawifi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nha);
        btn_nhalive = findViewById(R.id.btn_nhalive);
        btn_nhatime = findViewById(R.id.btn_nhatime);
        btn_nhalichsu = findViewById(R.id.btn_nhalichsu);
        btn_nhawifi = findViewById(R.id.btn_nhawifi);

        btn_nhalive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NhaMainActivity.this, NhaLiveActivity.class);
                startActivity(intent);
            }
        });

        btn_nhatime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NhaMainActivity.this, NhaTimeActivity.class);
                startActivity(intent);
            }
        });

        btn_nhalichsu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NhaMainActivity.this, NhaHistoryActivity.class);
                startActivity(intent);
            }
        });

        btn_nhawifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NhaMainActivity.this, NhaConnectActivity.class);
                startActivity(intent);
            }
        });
    }
}
