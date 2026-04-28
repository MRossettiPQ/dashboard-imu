#ifndef SERVER_UTILS_H
#define SERVER_UTILS_H
#include "config.h"
#include "logger.h"
#include "wifi_utils.h"

inline String resultContent(const int code, JsonDocument content, const String& message) {
    JsonDocument doc;
    doc["code"] = code;
    doc["message"] = message;
    doc["content"].set(content.as<JsonVariant>());

    String payload;
    serializeJson(doc, payload);
    return payload;
}

inline void notFoundController(AsyncWebServerRequest *request) {
    if (request->method() == HTTP_OPTIONS) {
        request->send(200);
        return;
    }

    const JsonDocument doc;
    Logger::info("Server", "Not found");
    request->send(404, "application/json", resultContent(404, doc, "Not Found"));
}

inline void configurationListController(AsyncWebServerRequest *request) {
    const JsonDocument content = listAvailableWifi();
    request->send(200, "application/json", resultContent(200, content, ""));
}

inline void configurationGetWifi(AsyncWebServerRequest *request) {
    const JsonDocument memory = readJson("/memory.json");
    const JsonDocument content;
    content["sta_ssid"] = memory["sta_ssid"];
    request->send(200, "application/json", resultContent(200, content, ""));
}

inline void configurationSaveController(AsyncWebServerRequest *request) {
    JsonDocument doc = readJson("/memory.json");
    String before;
    serializeJsonPretty(doc, before);
    Logger::info("Memory", "Before save: %s", before.c_str());

    const int params = request->params();
    for (int i = 0; i < params; i++) {
        const AsyncWebParameter *p = request->getParam(i);
        if (p->name() == "sta_ssid") {
            doc["sta_ssid"] = p->value();
        }
        if (p->name() == "sta_password") {
            doc["sta_password"] = p->value();
        }
    }

    // Salvar JSON atualizado
    writeJson("/data/memory.json", doc);

    // Chamar setup da rede externa com novos dados
    setupExternalWifi();

    const JsonDocument content;
    request->send(200, "application/json", resultContent(200, content, "Configuration saved"));
}

inline void setupServer() {
    Logger::info("Server", "Setting up server");
    DefaultHeaders::Instance().addHeader("Access-Control-Allow-Origin", "*");
    DefaultHeaders::Instance().addHeader("Access-Control-Allow-Methods", "GET, POST");
    DefaultHeaders::Instance().addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

    confServer.onNotFound(notFoundController);
    confServer.on("/api/configuration", HTTP_POST, configurationSaveController);
    confServer.on("/api/configuration", HTTP_GET, configurationGetWifi);
    confServer.on("/api/wifi", HTTP_GET, configurationListController);
}

inline void startServer() {
    Logger::info("Server", "Starting the Server");
    confServer.begin();
}


#endif //SERVER_UTILS_H
