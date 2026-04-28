#ifndef WIFI_UTILS_H
#define WIFI_UTILS_H
#include "config.h"
#include "fs_utils.h"
#include "logger.h"


inline void setupInternalWifi() {
    Logger::info("WiFi", "Configuring internal network");
    WiFiClass::mode(WIFI_AP_STA);
    WiFi.onEvent(eventsWiFi);

    // Configura IP estático do AP
    WiFi.softAPConfig(local_ip, gateway, subnet);

    // Inicia o AP
    WiFi.softAP(ap_ssid, ap_password);
    Logger::info("WiFi", "Connecting to AP");

    ap_ip = WiFi.softAPIP().toString();
    Logger::info("WiFi", "AP IP address: %s, SSID: %s", ap_ip.c_str(), ap_ssid.c_str());
}

inline void loadConfigWifi() {
    // Obter SSID e Senha para STA
    JsonDocument doc = readJson("/memory.json");

    sta_ssid = doc["sta_ssid"].as<String>();
    sta_password = doc["sta_password"].as<String>();

    Logger::info("WiFi", "STA configuration loaded: SSID: %s, Password: %s", sta_ssid.c_str(), sta_password.c_str());
}

inline void setupExternalWifi() {
    Logger::info("WiFi", "Configuring external network");
    loadConfigWifi();

    if (sta_ssid.length() == 0) {
      return;
    }

    WiFi.begin(sta_ssid, sta_password);
    vTaskDelay(100 / portTICK_PERIOD_MS);
    setupExternalWifCompleted = true;

    checkExternalWifi();
}

inline void checkExternalWifi() {
    Logger::info("WiFi", "Connecting to WiFi...");

    if (WiFiClass::status() != WL_CONNECTED) {
        Logger::info("WiFi", "WiFi not connected Wifi");
        WiFi.begin(sta_ssid, sta_password);
        vTaskDelay(100 / portTICK_PERIOD_MS);
    }

    if (WiFiClass::status() == WL_CONNECTED) {
        sta_ip = WiFi.localIP().toString();
        Logger::info("WiFi", "Wi-Fi connection established - IP address: %s", sta_ip.c_str());
    }
}

inline JsonDocument listAvailableWifi() {
    const int n = WiFi.scanNetworks();

    JsonDocument doc;
    const JsonArray arr = doc.to<JsonArray>();

    for (int i = 0; i < n; ++i) {
        auto net = arr.add<JsonObject>();
        net["ssid"] = WiFi.SSID(i);
        net["rssi"] = WiFi.RSSI(i);
    }

    return doc;
}

inline void onConnected() {
    // Configure time zone
    timeClient.begin();
    timeClient.forceUpdate();
    sta_ip = WiFi.localIP().toString();
    connectedWifi = true;

    Logger::info("WiFi", "Wi-Fi connection established - IP address: %s", sta_ip.c_str());
}

inline void eventsWiFi(const WiFiEvent_t event) {
    Logger::info("WiFi", "Event: %d", event);
    switch (event) {
        case ARDUINO_EVENT_WIFI_READY:
            Logger::info("WiFi", "WiFi interface ready");
            break;
        case ARDUINO_EVENT_WIFI_SCAN_DONE:
            Logger::info("WiFi", "Completed scan for access points");
            break;
        case ARDUINO_EVENT_WIFI_STA_START:
            Logger::info("WiFi", "WiFi client started");
            break;
        case ARDUINO_EVENT_WIFI_STA_STOP:
            Logger::info("WiFi", "WiFi clients stopped");
            break;
        case ARDUINO_EVENT_WIFI_STA_CONNECTED:
            Logger::info("WiFi", "Connected to access point");
            break;
        case ARDUINO_EVENT_WIFI_STA_DISCONNECTED:
            Logger::info("WiFi", "Disconnected from WiFi access point");
            break;
        case ARDUINO_EVENT_WIFI_STA_AUTHMODE_CHANGE:
            Logger::info("WiFi", "Authentication mode of access point has changed");
            break;
        case ARDUINO_EVENT_WIFI_STA_GOT_IP:
            onConnected();
            break;
        case ARDUINO_EVENT_WIFI_STA_LOST_IP:
            Logger::info("WiFi", "Lost IP address and IP address is reset to 0");
            break;
        case ARDUINO_EVENT_WPS_ER_SUCCESS:
            Logger::info("WiFi", "WiFi Protected Setup (WPS): succeeded in enrollee mode");
            break;
        case ARDUINO_EVENT_WPS_ER_FAILED:
            Logger::info("WiFi", "WiFi Protected Setup (WPS): failed in enrollee mode");
            break;
        case ARDUINO_EVENT_WPS_ER_TIMEOUT:
            Logger::info("WiFi", "WiFi Protected Setup (WPS): timeout in enrollee mode");
            break;
        case ARDUINO_EVENT_WPS_ER_PIN:
            Logger::info("WiFi", "WiFi Protected Setup (WPS): pin code in enrollee mode");
            break;
        case ARDUINO_EVENT_WIFI_AP_START:
            Logger::info("WiFi", "WiFi access point started");
            break;
        case ARDUINO_EVENT_WIFI_AP_STOP:
            Logger::info("WiFi", "WiFi access point  stopped");
            break;
        case ARDUINO_EVENT_WIFI_AP_STACONNECTED:
            Logger::info("WiFi", "Client connected Wifi");
            break;
        case ARDUINO_EVENT_WIFI_AP_STADISCONNECTED:
            Logger::info("WiFi", "Client disconnected");
            break;
        case ARDUINO_EVENT_WIFI_AP_STAIPASSIGNED:
            Logger::info("WiFi", "Assigned IP address to client");
            break;
        case ARDUINO_EVENT_WIFI_AP_PROBEREQRECVED:
            Logger::info("WiFi", "Received probe request");
            break;
        case ARDUINO_EVENT_WIFI_AP_GOT_IP6:
            Logger::info("WiFi", "AP IPv6 is preferred");
            break;
        case ARDUINO_EVENT_WIFI_STA_GOT_IP6:
            Logger::info("WiFi", "STA IPv6 is preferred");
            break;
        case ARDUINO_EVENT_ETH_GOT_IP6:
            Logger::info("WiFi", "Ethernet IPv6 is preferred");
            break;
        case ARDUINO_EVENT_ETH_START:
            Logger::info("WiFi", "Ethernet started");
            break;
        case ARDUINO_EVENT_ETH_STOP:
            Logger::info("WiFi", "Ethernet stopped");
            break;
        case ARDUINO_EVENT_ETH_CONNECTED:
            Logger::info("WiFi", "Ethernet connected Wifi");
            break;
        case ARDUINO_EVENT_ETH_DISCONNECTED:
            Logger::info("WiFi", "Ethernet disconnected");
            break;
        case ARDUINO_EVENT_ETH_GOT_IP:
            Logger::info("WiFi", "Obtained IP address");
            break;
        default:
            break;
    }
}

#endif //WIFI_UTILS_H
