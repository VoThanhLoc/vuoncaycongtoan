package com.example.tuoicay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    CardView btn_tuoilive;
    CardView btn_tuoitime;
    CardView btn_lichsu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        btn_tuoilive = findViewById(R.id.btn_tuoilive);
        btn_tuoitime = findViewById(R.id.btn_tuoitime);
        btn_lichsu = findViewById(R.id.btn_lichsu);

        //xử lý sự kiện khi nhấn icon

        btn_tuoilive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this, LiveActivity.class);
                startActivity(intent);
            }
        });

        btn_tuoitime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this, TimeActivity.class);
                startActivity(intent);
            }
        });

        btn_lichsu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });
    }
    private void toastMessage(String message)
    {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
