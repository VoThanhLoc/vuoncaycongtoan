#include <WiFi.h>
#include <Firebase_ESP_Client.h>

// Add Firebase library addons
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

// WiFi credentials
#define WIFI_SSID "Loc Vo"
#define WIFI_PASSWORD "88888888"

// Firebase credentials
#define API_KEY "AIzaSyAgNyRpGNJ_Wks8IgJe5xM9aKpTP84dZqA"
#define DATABASE_URL "https://vuoncaycongtoan-default-rtdb.firebaseio.com"

// Relay control pin
#define RELAY_PIN 13

// Firebase objects
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

bool lastRelayStatus = false;

// ===== Callback prototype (PHẢI đặt trước setup) =====
void streamCallback(FirebaseStream data);
void streamTimeoutCallback(bool timeout);

void setup() {
  Serial.begin(115200);
  pinMode(RELAY_PIN, OUTPUT);
  digitalWrite(RELAY_PIN, LOW);

  // Connect to Wi-Fi
  Serial.print("🔌 Đang kết nối Wi-Fi");
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\n✅ Kết nối Wi-Fi thành công!");

  // Configure Firebase
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  // Bắt đầu stream
  if (!Firebase.RTDB.beginStream(&fbdo, "/irrigationHouse/start")) {
    Serial.printf("❌ Không thể bắt đầu stream: %s\n", fbdo.errorReason().c_str());
  } else {
    Firebase.RTDB.setStreamCallback(&fbdo, streamCallback, streamTimeoutCallback);
    Serial.println("📡 Đã bắt đầu stream thành công.");
  }
}

void loop() {
  // Không cần code trong loop vì đã có callback.
}

// ✅ Callback khi có thay đổi dữ liệu từ Firebase
void streamCallback(FirebaseStream data) {
  Serial.printf("📡 Nhận thay đổi tại: %s\n", data.dataPath().c_str());

  if (data.dataTypeEnum() == firebase_rtdb_data_type_boolean) {
    bool relayStatus = data.boolData();
    Serial.printf("🔄 Giá trị mới: %s\n", relayStatus ? "true" : "false");

    if (relayStatus != lastRelayStatus) {
      lastRelayStatus = relayStatus;
      digitalWrite(RELAY_PIN, relayStatus ? HIGH : LOW);
      Serial.println(relayStatus ? "🚿 Relay BẬT" : "💧 Relay TẮT");
    }
  } else {
    Serial.printf("⚠️ Kiểu dữ liệu không hợp lệ: %s\n", data.dataType().c_str());
  }
}

// ✅ Callback khi stream bị timeout
void streamTimeoutCallback(bool timeout) {
  if (timeout) {
    Serial.println("⏳ Stream bị timeout. Đang thử lại...");
  }
}
