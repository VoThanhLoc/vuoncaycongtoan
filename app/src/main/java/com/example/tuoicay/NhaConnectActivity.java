package com.example.tuoicay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NhaConnectActivity extends AppCompatActivity {
    private WifiManager wifiManager;
    Spinner spinnerSSID;
    EditText editPassword;
    Button btnConnect;
    private List<String> ssidList = new ArrayList<>();
    private ArrayAdapter<String> ssidAdapter;
    Button  btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        spinnerSSID = findViewById(R.id.spinnerSSID);
        editPassword = findViewById(R.id.editPassword);
        btnConnect = findViewById(R.id.btnConnect);

        ssidAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ssidList);
        ssidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSSID.setAdapter(ssidAdapter);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        // Bật Wi-Fi nếu đang tắt
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        // Xin quyền vị trí (quét Wi-Fi cần quyền này)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            scanWifi();
        }

        btnConnect.setOnClickListener(view -> connectToWifi());
    }

    private void scanWifi() {
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                @SuppressLint("MissingPermission")
                List<ScanResult> results = wifiManager.getScanResults();
                ssidList.clear();
                for (ScanResult result : results) {
                    if (!ssidList.contains(result.SSID) && !result.SSID.isEmpty()) {
                        ssidList.add(result.SSID);
                    }
                }
                ssidAdapter.notifyDataSetChanged();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wifiManager.startScan();
    }

    private void connectToWifi() {
        String selectedSSID = spinnerSSID.getSelectedItem().toString();
        String password = editPassword.getText().toString();

        if (selectedSSID.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Chọn Wi-Fi và nhập mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + selectedSSID + "\"";
        conf.preSharedKey = "\"" + password + "\"";

        int netId = wifiManager.addNetwork(conf);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

        Toast.makeText(this, "Đang kết nối Wi-Fi...", Toast.LENGTH_SHORT).show();

        // Chờ vài giây rồi lưu nếu kết nối thành công
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null && ni.isConnected() && ni.getType() == ConnectivityManager.TYPE_WIFI) {
                saveToFirebase(selectedSSID, password);
            } else {
                Toast.makeText(this, "Kết nối thất bại!", Toast.LENGTH_SHORT).show();
            }
        }, 5000);
    }

    private void saveToFirebase(String ssid, String password) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("wifihouse");
        Map<String, String> wifiInfo = new HashMap<>();
        wifiInfo.put("ssid", ssid);
        wifiInfo.put("password", password);
        dbRef.setValue(wifiInfo).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Đã lưu vào Firebase!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(NhaConnectActivity.this, HomeMainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Lỗi lưu Firebase!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
