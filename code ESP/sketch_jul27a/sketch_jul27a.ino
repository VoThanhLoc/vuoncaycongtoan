#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include <NTPClient.h>
#include <WiFiUdp.h>

// ----------- THÔNG TIN CẦN CẬP NHẬT ------------
#define DEFAULT_SSID "LOC VO"
#define DEFAULT_PASSWORD "88888888"

#define FIREBASE_HOST "https://vuoncaycongtoan-default-rtdb.firebaseio.com/"
#define FIREBASE_AUTH ""  // Nếu dùng DB public thì để trống

// ------------ PIN RELAY -------------
const int relayPump = 32;  // Relay máy bơm
const int relayZones[7] = {25, 26, 27, 14, 12, 13, 33}; // 7 van tưới

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

unsigned long lastScheduleCheck = 0;
const unsigned long scheduleInterval = 60000; // kiểm tra mỗi 60 giây

// ------------------------- KẾT NỐI WIFI TỪ FIREBASE -------------------------
void fetchWiFiFromFirebase() {
  Serial.println("Đang thử lấy thông tin Wi-Fi từ Firebase...");

  if (Firebase.RTDB.getString(&fbdo, "/wifi/ssid")) {
    String ssid = fbdo.stringData();
    if (Firebase.RTDB.getString(&fbdo, "/wifi/password")) {
      String password = fbdo.stringData();
      Serial.println("Kết nối Wi-Fi từ Firebase...");
      WiFi.begin(ssid.c_str(), password.c_str());

      unsigned long startAttempt = millis();
      while (WiFi.status() != WL_CONNECTED && millis() - startAttempt < 10000) {
        delay(500);
        Serial.print(".");
      }

      if (WiFi.status() == WL_CONNECTED) {
        Serial.println("\n✅ Kết nối Wi-Fi thành công.");
        return;
      }
    }
  }

  Serial.println("Không lấy được Wi-Fi từ Firebase, dùng mặc định.");
  WiFi.begin(DEFAULT_SSID, DEFAULT_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\n✅ Kết nối Wi-Fi mặc định thành công.");
}

// -------------------- CẤU HÌNH FIREBASE + THỜI GIAN --------------------
void setupFirebase() {
  config.database_url = FIREBASE_HOST;
  config.signer.tokens.legacy_token = FIREBASE_AUTH;
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
}

void setupTime() {
  configTime(7 * 3600, 0, "pool.ntp.org", "time.nist.gov");  // GMT+7
  Serial.print("Đang đồng bộ thời gian...");
  while (time(nullptr) < 100000) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("✔️ Xong.");
}

// ---------------------- HÀM BẬT TẮT RELAY ----------------------
void updateRelayStates() {
  if (Firebase.RTDB.getBool(&fbdo, "/irrigation/start")) {
    bool start = fbdo.boolData();

    // Nếu có ít nhất một zone đang mở thì bật máy bơm
    bool anyZoneOn = false;
    for (int i = 0; i < 7; i++) {
      String path = "/irrigation/zones/zone" + String(i + 1) + "/status";
      if (Firebase.RTDB.getString(fbdo, path)) {
        String status = fbdo.stringData();
        bool isOn = (status == "on");
        digitalWrite(relayZones[i], isOn ? LOW : HIGH);
        if (isOn) anyZoneOn = true;
      }
    }
    digitalWrite(relayPump, (start && anyZoneOn) ? LOW : HIGH);
  }
}

// ------------------------ KIỂM TRA LỊCH HẸN GIỜ ------------------------
void checkSchedules() {
  time_t now = time(nullptr);
  struct tm* timeinfo = localtime(&now);
  char currentTime[6];
  strftime(currentTime, sizeof(currentTime), "%H:%M", timeinfo);
  const char* weekdays[] = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
  String today = weekdays[timeinfo->tm_wday];

  if (Firebase.RTDB.getJSON(&fbdo, "/schedule")) {
    FirebaseJson& json = fbdo.to<FirebaseJson>();
    size_t count = json.iteratorBegin();

    for (size_t i = 0; i < count; i++) {
      int type;
      String key, value;
      json.iteratorGet(i, type, key, value);
      String schedPath = "/schedule/" + key;

      // Lấy startTime
      if (Firebase.RTDB.getString(&fbdo, schedPath + "/startTime")) {
        String startTime = fbdo.stringData();

        // Lấy duration
        Firebase.RTDB.getString(&fbdo, schedPath + "/duration");
        String duration = fbdo.stringData();

        // Lấy status
        Firebase.RTDB.getString(&fbdo, schedPath + "/status");
        String status = fbdo.stringData();

        // Lấy danh sách zone
        Firebase.RTDB.getArray(&fbdo, schedPath + "/zone");
        FirebaseJsonArray zoneArray = fbdo.to<FirebaseJsonArray>();

        // Lấy danh sách ngày lặp lại
        Firebase.RTDB.getArray(&fbdo, schedPath + "/repeatDays");
        FirebaseJsonArray dayArray = fbdo.to<FirebaseJsonArray>();

        // So sánh thời gian và thực thi nếu đúng
        if (status == "off" && startTime == String(currentTime)) {
          bool matchDay = false;
          for (size_t j = 0; j < dayArray.size(); j++) {
            FirebaseJsonData dayItem;
            dayArray.get(dayItem, j);
            if (dayItem.stringValue == today) {
              matchDay = true;
              break;
            }
          }

          if (matchDay) {
            Serial.println("▶️ Bắt đầu tưới theo lịch: " + key);

            for (size_t j = 0; j < zoneArray.size(); j++) {
              FirebaseJsonData zoneItem;
              zoneArray.get(zoneItem, j);
              int zoneIdx = zoneItem.stringValue.substring(4).toInt();  // "zone1" => 1
              digitalWrite(relayZones[zoneIdx - 1], LOW);
              Firebase.RTDB.setString(&fbdo, "/irrigation/zones/zone" + String(zoneIdx) + "/status", "on");
            }

            Firebase.RTDB.setBool(&fbdo, "/irrigation/start", true);
            Firebase.RTDB.setString(&fbdo, schedPath + "/status", "on");

            delay(duration.toInt() * 60000);

            for (size_t j = 0; j < zoneArray.size(); j++) {
              FirebaseJsonData zoneItem;
              zoneArray.get(zoneItem, j);
              int zoneIdx = zoneItem.stringValue.substring(4).toInt();
              digitalWrite(relayZones[zoneIdx - 1], HIGH);
              Firebase.RTDB.setString(&fbdo, "/irrigation/zones/zone" + String(zoneIdx) + "/status", "off");
            }

            Firebase.RTDB.setBool(&fbdo, "/irrigation/start", false);
            Firebase.RTDB.setString(&fbdo, schedPath + "/status", "off");

            Serial.println("✅ Đã tưới xong lịch: " + key);
          }
        }
      }
    }

    json.iteratorEnd();
  }
}


// ------------------------- SETUP -------------------------
void setup() {
  Serial.begin(115200);

  for (int i = 0; i < 7; i++) {
    pinMode(relayZones[i], OUTPUT);
    digitalWrite(relayZones[i], HIGH);
  }
  pinMode(relayPump, OUTPUT);
  digitalWrite(relayPump, HIGH);

  fetchWiFiFromFirebase();
  setupFirebase();
  setupTime();
  Serial.println("✅ Hệ thống sẵn sàng");
}

// ------------------------ LOOP ------------------------
void loop() {
  updateRelayStates();

  if (millis() - lastScheduleCheck > scheduleInterval) {
    lastScheduleCheck = millis();
    checkSchedules();
  }

  delay(500);
}
