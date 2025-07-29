#include <WiFi.h>
#include <Firebase_ESP_Client.h>

// WiFi
#define WIFI_SSID "THANH LOC_vnpt"
#define WIFI_PASSWORD "88888888"

// Firebase
#define DATABASE_URL "https://vuoncaycongtoan-default-rtdb.firebaseio.com/"
#define DATABASE_SECRET "2qVdRhSRCsjySGWagTC4UMjvSXsWkLgzmaR2ZX35"

// Relay pins
#define RELAY1_PIN 13
#define RELAY2_PIN 12

// Firebase config
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

void streamCallback(FirebaseStream data) {
  Serial.println("🔄 Dữ liệu thay đổi:");
  Serial.printf("Path: %s\n", data.dataPath().c_str());
  Serial.printf("Type: %s\n", data.dataType().c_str());
  Serial.printf("Value: %s\n", data.stringData().c_str());

  String path = data.dataPath();
  String value = data.stringData();

  if (path == "/relay1") {
    digitalWrite(RELAY1_PIN, value == "on" ? LOW : HIGH);  // Active LOW
    Serial.printf("Relay 1 -> %s\n", value.c_str());
  } else if (path == "/relay2") {
    digitalWrite(RELAY2_PIN, value == "on" ? LOW : HIGH);
    Serial.printf("Relay 2 -> %s\n", value.c_str());
  }
}

void streamTimeoutCallback(bool timeout) {
  if (timeout) {
    Serial.println("⚠️ Stream timeout, đang kết nối lại...");
  }
}

void setup() {
  Serial.begin(115200);

  // Kết nối Wi-Fi
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("⏳ Đang kết nối Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\n✅ Kết nối Wi-Fi thành công!");

  // Cấu hình Firebase
  config.database_url = DATABASE_URL;
  config.signer.tokens.legacy_token = DATABASE_SECRET;

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  // Cấu hình chân relay
  pinMode(RELAY1_PIN, OUTPUT);
  pinMode(RELAY2_PIN, OUTPUT);
  digitalWrite(RELAY1_PIN, HIGH); // OFF
  digitalWrite(RELAY2_PIN, HIGH);

  // Bắt đầu stream
  if (!Firebase.RTDB.beginStream(&fbdo, "/relay")) {
    Serial.printf("❌ Lỗi stream: %s\n", fbdo.errorReason().c_str());
  }
  Firebase.RTDB.setStreamCallback(&fbdo, streamCallback, streamTimeoutCallback);
}

void loop() {
  // Không cần gì ở đây
}
