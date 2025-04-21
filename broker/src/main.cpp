#include <Arduino.h>
#include <ArduinoJson.h>
#include <esp_now.h>
#include <WiFi.h>
#include "logger.h"

#define IMU_MANUFACTURER "rot"
#define IMU_PRODUCT "imu-dashboard-broker"
#define IMU_DESCRIPTOR "Broker esp-now"

void sendToPeers(const String& msg) {
    const uint8_t broadcastAddress[] = {0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF}; // Broadcast
    esp_now_send(broadcastAddress, reinterpret_cast<const uint8_t*>(msg.c_str()), msg.length());
}

void setupEspNow() {
    // Inicializa Wi-Fi no modo STA
    WiFiClass::mode(WIFI_STA);
    if (esp_now_init() != ESP_OK) {
        Logger::error("ESP-NOW", "Erro ao inicializar ESP-NOW");
        return;
    }
    Logger::info("ESP-NOW", "ESP-NOW inicializado");
}

void sendAvailableNetworks() {
    const int n = WiFi.scanNetworks();

    JsonDocument doc;
    doc["type"] = "WIFI_SCAN_RESULT";

    // Novo jeito de criar um array
    const JsonArray arr = doc["data"].to<JsonArray>();

    for (int i = 0; i < n; ++i) {
        // Novo jeito de adicionar objetos ao array
        auto net = arr.add<JsonObject>();
        net["ssid"] = WiFi.SSID(i);
        net["rssi"] = WiFi.RSSI(i);
    }

    // Serializa o JSON e envia via ESP-NOW
    String payload;
    serializeJson(doc, payload);
    sendToPeers(payload);
}

void sendDeviceInfo() {
    JsonDocument doc;
    doc["type"] = "DEVICE_INFO_RESPONSE";
    const JsonObject data = doc["data"].to<JsonObject>();

    data["descriptor"] = IMU_DESCRIPTOR;
    data["productName"] = IMU_PRODUCT;
    data["manufacturer"] = IMU_MANUFACTURER;

    serializeJson(doc, Serial);
    Serial.println();
    alreadySent = true;
}

void processCommand(String command) {
    command.trim();

    // Se o comando começar com '{', trata como JSON
    if (command.startsWith("{")) {
        JsonDocument doc;
        const DeserializationError error = deserializeJson(doc, command);

        if (error) {
            Logger::error("COMMAND", R"({"error":"INVALID_JSON"})");
            return;
        }

        // Verifica se o JSON tem o campo "type" (nova sintaxe)
        if (doc["type"].is<const char*>()) {
            // Verifica se "type" existe e é string
            const char* type = doc["type"];
            if (strcmp(type, "DEVICE_INFO_REQUEST") == 0) {
                sendDeviceInfo();
            }
            else if (doc["type"] == "WIFI_REGISTER_REQUEST") {
                const char* ssid = doc["ssid"];
                const char* password = doc["password"];

                // Monta JSON para enviar para os peers via ESP-NOW
                JsonDocument out;
                out["type"] = "WIFI_REGISTER";
                out["ssid"] = ssid;
                out["password"] = password;

                String payload;
                serializeJson(out, payload);
                sendToPeers(payload);
            }
        }
    }
    else if (command == "DEVICE_INFO_REQUEST") {
        // Se for um comando de texto simples
        sendDeviceInfo();
    }
}

void setup() {
    Serial.begin(115200);
    Serial.setDebugOutput(true);
    Logger::info("SETUP", "Initializing...");
    Logger::info("SETUP", "Chip model: %s\n", ESP.getChipModel());
    Logger::info("SETUP", "Chip revision: %d\n", ESP.getChipRevision());
    Logger::info("SETUP", "Number of cores: %d\n", ESP.getChipCores());
}

void loop() {
    // Verifica se há dados na serial e se é um comando
    if (Serial.available()) {
        const String command = Serial.readStringUntil('\n');
        processCommand(command);
    }

    // Detecta reconexão (se a serial foi aberta após fechamento)
    if (!alreadySent) {
        sendDeviceInfo();
    }

    if (!Serial.available() && alreadySent) {
        alreadySent = false;
    }
    vTaskDelay(200 / portTICK_PERIOD_MS);
}
