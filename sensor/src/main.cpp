#include "main.h"
#include <Arduino.h>

#include <WiFi.h>
#include <esp_now.h>
#include <ArduinoJson.h>

#define CHANNEL 1  // Canal padrão
#define WIFI_TIMEOUT_MS 10000


TaskHandle_t Task1;
TaskHandle_t Task2;

void onDataReceive(const uint8_t *mac, const uint8_t *incomingData, int len) {
    String msg((char*)incomingData, len);
    Serial.println("[RECEBIDO VIA ESP-NOW] " + msg);

    // Tenta decodificar JSON
    StaticJsonDocument<256> doc;
    if (deserializeJson(doc, msg)) {
        Serial.println("Erro ao ler JSON");
        return;
    }

    const char* type = doc["type"];
    if (strcmp(type, "WIFI_REGISTER") == 0) {
        const char* ssid = doc["ssid"];
        const char* password = doc["password"];

        Serial.printf("Conectando ao WiFi SSID: %s\n", ssid);

        WiFi.disconnect(true);
        WiFi.begin(ssid, password);

        unsigned long startAttempt = millis();
        while (WiFi.status() != WL_CONNECTED && millis() - startAttempt < WIFI_TIMEOUT_MS) {
            delay(500);
            Serial.print(".");
        }

        if (WiFi.status() == WL_CONNECTED) {
            Serial.println("\n✅ Conectado à rede Wi-Fi com sucesso!");
            Serial.println(WiFi.localIP());
        } else {
            Serial.println("\n❌ Falha ao conectar ao Wi-Fi.");
        }

        // Depois de conectar, você pode continuar usando ESP-NOW normalmente
    }
}

void setupEspNowReceiver() {
    WiFi.mode(WIFI_STA);
    WiFi.disconnect();
    if (esp_now_init() != ESP_OK) {
        Logger::error("ESP-NOW", "Erro ao inicializar ESP-NOW");
        return;
    }
    esp_now_register_recv_cb(onDataReceive);

    // Registra broadcast
    esp_now_peer_info_t peerInfo{};
    memcpy(peerInfo.peer_addr, "\xFF\xFF\xFF\xFF\xFF\xFF", 6);
    peerInfo.channel = CHANNEL;
    peerInfo.encrypt = false;
    esp_now_add_peer(&peerInfo);
}

[[noreturn]] void Task1code(void* parameter) {
    Logger::info("Task1code", "Running on core %d", xPortGetCoreID());
    for (;;) {
        const unsigned long current_millis = millis();
        Logger::info("Task1code", "Running on core %d", current_millis);

        vTaskDelay(50 / portTICK_PERIOD_MS);
    }
}

[[noreturn]] void Task2code(void* parameter) {
    Logger::info("Task2code", "Running on core %d", xPortGetCoreID());
    for (;;) {
        const unsigned long current_millis = millis();
        Logger::info("Task2code", "Running on core %d", current_millis);

        sensor_instance.scanner();
        sensor_instance.configure();

        wifi_instance.check();

        vTaskDelay(50 / portTICK_PERIOD_MS);
    }
}

void setup() {
    Serial.begin(115200);
    Serial.setDebugOutput(true);
    Logger::info("SETUP", "Initialize sensor");

    wifi_instance.configure();

    xTaskCreatePinnedToCore(
        Task1code,
        "Task1",
        10000, /* Stack size in words */
        nullptr,
        1,
        &Task1,
        0
    );

    xTaskCreatePinnedToCore(
        Task2code,
        "Task2",
        10000, /* Stack size in words */
        nullptr,
        1,
        &Task2,
        1
    );
}

void loop() {
}
