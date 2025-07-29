#include <WiFi.h>
#include <SPIFFS.h>
#include <Firebase_ESP_Client.h>

#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

// ------------------- Config -------------------
#define AP_SSID "SetupDevice"
#define AP_PASSWORD "12345678"

#define API_KEY "AIzaSyAgNyRpGNJ_Wks8IgJe5xM9aKpTP84dZqA"
#define DATABASE_URL "https://vuoncaycongtoan-default-rtdb.firebaseio.com"

// Relay GPIO
#define PUMP_RELAY_PIN 32

// ------------------- Firebase -------------------
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

// WiFi credentials
String ssid = "";
String pass = "";

// ------------------- Helper: decode dấu + -------------------
String decodeUrl(String input) {
  input.replace("+", " ");
  return input;
}

// ------------------- WiFi cấu hình -------------------
void startWiFiConfigPortal() {
  WiFi.mode(WIFI_AP);
  WiFi.softAP(AP_SSID, AP_PASSWORD);
  Serial.println("🚪 WiFi cấu hình bật");
  Serial.println("📲 Truy cập 192.168.4.1 để nhập WiFi");

  WiFiServer server(80);
  server.begin();

  while (true) {
    WiFiClient client = server.available();
    if (client) {
      Serial.println("📶 Thiết bị kết nối");

      String req = "";
      while (client.connected()) {
        if (client.available()) {
          char c = client.read();
          req += c;
          if (req.endsWith("\r\n\r\n")) break;
        }
      }

      if (req.indexOf("GET /?ssid=") >= 0) {
        int ssidStart = req.indexOf("ssid=") + 5;
        int passStart = req.indexOf("&pass=") + 6;
        int ssidEnd = req.indexOf("&pass=");
        int passEnd = req.indexOf(" HTTP");

        ssid = decodeUrl(req.substring(ssidStart, ssidEnd));
        pass = decodeUrl(req.substring(passStart, passEnd));

        Serial.printf("📶 SSID: %s\n", ssid.c_str());
        Serial.printf("🔐 Password: %s\n", pass.c_str());

        // Lưu SPIFFS
        File file = SPIFFS.open("/wifi.txt", FILE_WRITE);
        file.printf("%s\n%s\n", ssid.c_str(), pass.c_str());
        file.close();

        client.println("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n");
        client.println("<html><body><h2>✅ Đã lưu WiFi. Đang khởi động lại...</h2></body></html>");
        delay(2000);
        ESP.restart();
      } else {
        client.println("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n");
        client.println("<form action='/' method='GET'>");
        client.println("SSID: <input name='ssid'><br>");
        client.println("Password: <input name='pass'><br>");
        client.println("<input type='submit' value='Lưu WiFi'></form>");
      }

      client.stop();
    }
  }
}

// ------------------- Đọc WiFi từ SPIFFS -------------------
void readWiFiFromSPIFFS() {
  if (!SPIFFS.begin(true)) {
    Serial.println("❌ Không thể mount SPIFFS");
    return;
  }

  if (SPIFFS.exists("/wifi.txt")) {
    File file = SPIFFS.open("/wifi.txt");
    ssid = decodeUrl(file.readStringUntil('\n'));
    pass = decodeUrl(file.readStringUntil('\n'));
    ssid.trim();
    pass.trim();
    file.close();
  } else {
    startWiFiConfigPortal();
  }
}

// ------------------- Firebase Callback -------------------
void streamCallback(FirebaseStream data) {
  Serial.printf("📡 Firebase thay đổi: %s => %s\n", data.dataPath().c_str(), data.stringData().c_str());

  if (data.dataType() == "boolean") {
    bool state = data.boolData();
    digitalWrite(PUMP_RELAY_PIN, state ? HIGH : LOW);
    Serial.printf(state ? "💧 BẬT máy bơm\n" : "💤 TẮT máy bơm\n");
  }
}

void streamTimeoutCallback(bool timeout) {
  if (timeout) {
    Serial.println("⚠️ Mất kết nối stream Firebase!");
  }
}

// ------------------- SETUP -------------------
void setup() {
  Serial.begin(115200);
  pinMode(PUMP_RELAY_PIN, OUTPUT);
  digitalWrite(PUMP_RELAY_PIN, LOW);

  readWiFiFromSPIFFS();

  Serial.printf("🔌 Kết nối WiFi: %s\n", ssid.c_str());
  WiFi.begin(ssid.c_str(), pass.c_str());

  unsigned long start = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - start < 15000) {
    delay(500);
    Serial.print(".");
  }

  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("\n❌ Kết nối WiFi thất bại. Mở cấu hình lại...");
    startWiFiConfigPortal();
  }

  Serial.println("\n✅ Kết nối WiFi thành công");

  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  if (!Firebase.RTDB.beginStream(&fbdo, "/irrigationHouse/start")) {
    Serial.printf("❌ Lỗi bắt đầu stream: %s\n", fbdo.errorReason().c_str());
  }

  Firebase.RTDB.setStreamCallback(&fbdo, streamCallback, streamTimeoutCallback);
}

void loop() {
  // Không cần làm gì thêm
}
