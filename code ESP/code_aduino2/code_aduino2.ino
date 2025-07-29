#include <WiFi.h>
#include <Firebase_ESP_Client.h>

// WiFi credentials
#define WIFI_SSID "THANH LOC_vnpt"
#define WIFI_PASSWORD "244466666"

// Firebase credentials
#define FIREBASE_URL "https://vuoncaycongtoan-default-rtdb.firebaseio.com"
#define FIREBASE_SECRET "2qVdRhSRCsjySGWagTC4UMjvSXsWkLgzmaR2ZX35"

// Firebase objects
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

// Relay pins
#define NUM_ZONES 7
const int relayPins[NUM_ZONES] = { 13, 12, 14, 27, 26, 25, 33 }; // GPIO tùy chỉnh
#define PUMP_RELAY_PIN 32 // relay máy bơm

// Trạng thái zone hiện tại
bool zoneStates[NUM_ZONES] = { false };

// Callback function khi data thay đổi
void streamCallback(FirebaseStream data) {
  Serial.println("📡 Dữ liệu đã thay đổi:");
  Serial.println("🔗 Path: " + data.dataPath());
  Serial.println("➡️  Dữ liệu mới: " + data.stringData());

  String path = data.dataPath(); // ví dụ: /zone1/status
  String value = data.stringData();

  int zoneIndex = -1;
  if (path.indexOf("zone") >= 0) {
    int start = path.indexOf("zone") + 4;
    int end = path.indexOf("/", start);
    String numStr = path.substring(start, end == -1 ? path.length() : end);
    zoneIndex = numStr.toInt() - 1; // zone1 -> 0

    if (zoneIndex >= 0 && zoneIndex < NUM_ZONES && path.endsWith("/status")) {
      if (value == "on") {
        digitalWrite(relayPins[zoneIndex], HIGH);
        zoneStates[zoneIndex] = true;
        Serial.printf("✅ BẬT zone %d (relay GPIO %d)\n", zoneIndex + 1, relayPins[zoneIndex]);
      } else {
        digitalWrite(relayPins[zoneIndex], LOW);
        zoneStates[zoneIndex] = false;
        Serial.printf("🛑 TẮT zone %d (relay GPIO %d)\n", zoneIndex + 1, relayPins[zoneIndex]);
      }
      updatePumpState(); // Cập nhật trạng thái máy bơm
    }
  }
}

// Bật máy bơm nếu có ít nhất 1 zone đang bật
void updatePumpState() {
  bool anyOn = false;
  for (int i = 0; i < NUM_ZONES; i++) {
    if (zoneStates[i]) {
      anyOn = true;
      break;
    }
  }
  digitalWrite(PUMP_RELAY_PIN, anyOn ? HIGH : LOW);
  Serial.printf("💧 Máy bơm %s\n", anyOn ? "BẬT" : "TẮT");
}

// Callback khi stream timeout
void streamTimeoutCallback(bool timeout) {
  if (timeout) {
    Serial.println("⚠️ Mất kết nối stream, đang reconnect...");
  }
}

void setup() {
  Serial.begin(115200);

  // Setup các chân relay
  for (int i = 0; i < NUM_ZONES; i++) {
    pinMode(relayPins[i], OUTPUT);
    digitalWrite(relayPins[i], LOW);
  }
  pinMode(PUMP_RELAY_PIN, OUTPUT);
  digitalWrite(PUMP_RELAY_PIN, LOW);

  // Kết nối Wi-Fi
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("⏳ Kết nối Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\n✅ Đã kết nối Wi-Fi");

  // Cấu hình Firebase
  config.database_url = FIREBASE_URL;
  config.signer.tokens.legacy_token = FIREBASE_SECRET;

  // Khởi động Firebase
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  // Gửi test kết nối
  if (Firebase.RTDB.setString(&fbdo, "/test", "ESP32 đã kết nối Firebase!")) {
    Serial.println("✅ Gửi test thành công!");
  } else {
    Serial.print("❌ Lỗi gửi test: ");
    Serial.println(fbdo.errorReason());
  }

  // Lắng nghe các thay đổi trong /irrigation/zones
  if (!Firebase.RTDB.beginStream(&fbdo, "/irrigation/zones")) {
    Serial.println("❌ Không thể bắt đầu stream");
    Serial.println(fbdo.errorReason());
  }

  Firebase.RTDB.setStreamCallback(&fbdo, streamCallback, streamTimeoutCallback);
}

void loop() {
  // không cần xử lý gì trong loop
}
