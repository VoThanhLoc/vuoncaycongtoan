#include <WiFi.h>
#include <ESPAsyncWebServer.h>
#include <SPIFFS.h>
#include <Firebase_ESP_Client.h>

// Replace with your Firebase project credentials
#define FIREBASE_HOST "https://vuoncaycongtoan-default-rtdb.firebaseio.com/"
#define FIREBASE_SECRET "2qVdRhSRCsjySGWagTC4UMjvSXsWkLgzmaR2ZX35"

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

#define RELAY_PIN 13
#define WIFI_CONFIG_FILE "/wifi.txt"
#define WIFI_TIMEOUT_MS 300000  // 5 phút

AsyncWebServer server(80);

void setupRelay() {
  pinMode(RELAY_PIN, OUTPUT);
  digitalWrite(RELAY_PIN, LOW);
}

bool loadWiFiConfig(String &ssid, String &pass) {
  if (!SPIFFS.exists(WIFI_CONFIG_FILE)) return false;
  File file = SPIFFS.open(WIFI_CONFIG_FILE);
  if (!file) return false;

  ssid = file.readStringUntil('\n');
  pass = file.readStringUntil('\n');
  ssid.trim();
  pass.trim();
  file.close();
  return true;
}

void saveWiFiConfig(String ssid, String pass) {
  File file = SPIFFS.open(WIFI_CONFIG_FILE, FILE_WRITE);
  if (!file) return;
  file.println(ssid);
  file.println(pass);
  file.close();
}

bool connectWiFi(String ssid, String pass) {
  WiFi.begin(ssid.c_str(), pass.c_str());
  unsigned long startAttempt = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - startAttempt < WIFI_TIMEOUT_MS) {
    delay(500);
    Serial.print(".");
  }
  return WiFi.status() == WL_CONNECTED;
}

void startAPMode() {
  WiFi.softAP("ESP32_Setup", "12345678");
  IPAddress IP = WiFi.softAPIP();
  Serial.print("AP IP address: ");
  Serial.println(IP);

  server.on("/", HTTP_GET, [](AsyncWebServerRequest *request) {
    request->send(200, "text/html",
      "<form action=\"/save\" method=\"POST\">"
      "SSID: <input name=\"ssid\"><br>"
      "Password: <input name=\"pass\"><br>"
      "<input type=\"submit\" value=\"Save\">"
      "</form>");
  });

  server.on("/save", HTTP_POST, [](AsyncWebServerRequest *request) {
    String ssid = request->getParam("ssid", true)->value();
    String pass = request->getParam("pass", true)->value();
    saveWiFiConfig(ssid, pass);
    request->send(200, "text/html", "Saved! Rebooting...");
    delay(2000);
    ESP.restart();
  });

  server.begin();
}

void streamCallback(FirebaseStream data) {
  if (data.dataType() == "boolean") {
    bool state = data.boolData();
    digitalWrite(RELAY_PIN, state ? LOW : HIGH);
    Serial.printf("[Firebase] start = %s\n", state ? "true" : "false");
  }
}

void streamTimeoutCallback(bool timeout) {
  if (timeout) Serial.println("[Firebase] Stream timeout, reconnecting...");
}

void setupFirebase() {
  config.database_url = FIREBASE_HOST;
  config.signer.tokens.legacy_token = FIREBASE_SECRET;

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  if (!Firebase.RTDB.beginStream(&fbdo, "/irrigationHouse/start")) {
    Serial.println("[Firebase] Stream begin failed:");
    Serial.println(fbdo.errorReason());
  }

  Firebase.RTDB.setStreamCallback(&fbdo, streamCallback, streamTimeoutCallback);
}

void setup() {
  Serial.begin(115200);
  setupRelay();
  SPIFFS.begin(true);

  String ssid, pass;
  if (loadWiFiConfig(ssid, pass)) {
    Serial.println("[WiFi] Trying saved credentials...");
    if (connectWiFi(ssid, pass)) {
      Serial.print("[WiFi] Connected: ");
      Serial.println(WiFi.localIP());
      setupFirebase();
    } else {
      Serial.println("[WiFi] Failed. Switching to AP Mode.");
      startAPMode();
    }
  } else {
    Serial.println("[WiFi] No saved credentials. AP Mode.");
    startAPMode();
  }
}

void loop() {
  // Nothing needed here. Firebase callbacks handle changes.
}
