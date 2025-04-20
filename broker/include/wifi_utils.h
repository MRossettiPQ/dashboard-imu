#ifndef WIFI_UTILS_H
#define WIFI_UTILS_H

#include <Arduino.h>
#include <logger.h>
#include <NTPClient.h>
#include <WiFi.h>
#include <WiFiUdp.h>

// Time API
#define NTP_TIME_API "a.st1.ntp.br"

// Filesystem inputs and paths
auto input_ssid = "ssid";
auto input_password = "password";

WiFiUDP ntpUDP;

class WiFiUtils;

extern WiFiUtils wifi_instance;

class WiFiUtils {
    public:
        class Internal {
            public:
                String ssid;
                String password;
        };

        class External {
            public:
                IPAddress ip;
                IPAddress gateway = IPAddress(192, 168, 1, 1);
                IPAddress subnet = IPAddress(255, 255, 0, 0);

                bool connected = false;
                String ssid;
                String password;
        };

        String address;
        static String mac;
        NTPClient timeClient = NTPClient(ntpUDP, NTP_TIME_API, -3 * 3600, 60000);
        External external;
        Internal internal;

        void configure() {
            Logger::info("WiFi", "Configuring WiFi");

            WiFiClass::mode(WIFI_AP_STA);

            WiFi.onEvent(events);
            const String name = "MPU-MANAGER-";
            mac = WiFi.macAddress();
            internal.ssid = name + mac;
            WiFi.softAP(internal.ssid);

            const String ip = WiFi.softAPIP().toString();
            Logger::info("WiFi", "AP IP address: %s, SSID: %s", ip.c_str(), internal.ssid.c_str());
            if (!WiFi.config(external.ip, external.gateway, external.subnet)) {
                Logger::info("WiFi", "STA Failed to configure");
                ESP.restart();
            }

            check();
        }

        void check() {
            Logger::info("WiFi", "Connecting to WiFi...");

            if (WiFiClass::status() != WL_CONNECTED) {
                Logger::info("WiFi", "WiFi not connected Wifi");
                WiFi.begin(external.ssid, external.password);
                vTaskDelay(100 / portTICK_PERIOD_MS);
            }

            if (WiFiClass::status() == WL_CONNECTED) {
                address = WiFi.localIP().toString();
                Logger::info("WiFi", "Wi-Fi connection established - IP address: %s", address.c_str());
            }
        }

        static void events(const WiFiEvent_t event) {
            Logger::info("WiFi", "Event: %d", event);
            switch (event) {
                case ARDUINO_EVENT_WIFI_READY:
                    Logger::info("WiFi", "Interface ready");
                    break;
                case ARDUINO_EVENT_WIFI_SCAN_DONE:
                    Logger::info("WiFi", "Completed scan for access points");
                    break;
                case ARDUINO_EVENT_WIFI_STA_START:
                    Logger::info("WiFi", "Client started");
                    break;
                case ARDUINO_EVENT_WIFI_STA_STOP:
                    Logger::info("WiFi", "Clients stopped");
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
                    wifi_instance.onConnect();
                    break;
                case ARDUINO_EVENT_WIFI_STA_LOST_IP:
                    Logger::info("WiFi", "Lost IP address and IP address is reset to 0");
                    break;
                case ARDUINO_EVENT_WPS_ER_SUCCESS:
                    Logger::info("WiFi", "Protected Setup (WPS): succeeded in enrollee mode");
                    break;
                case ARDUINO_EVENT_WPS_ER_FAILED:
                    Logger::info("WiFi", "Protected Setup (WPS): failed in enrollee mode");
                    break;
                case ARDUINO_EVENT_WPS_ER_TIMEOUT:
                    Logger::info("WiFi", "Protected Setup (WPS): timeout in enrollee mode");
                    break;
                case ARDUINO_EVENT_WPS_ER_PIN:
                    Logger::info("WiFi", "Protected Setup (WPS): pin code in enrollee mode");
                    break;
                case ARDUINO_EVENT_WIFI_AP_START:
                    Logger::info("WiFi", "Access point started");
                    break;
                case ARDUINO_EVENT_WIFI_AP_STOP:
                    Logger::info("WiFi", "Access point  stopped");
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

        void onConnect() {
            external.connected = true;
            address = WiFi.localIP().toString();
            Logger::info("WiFi", "Obtained IP address: %s", address);

            // Configure time zone
            timeClient.begin();
            timeClient.forceUpdate();
        }
};


#endif //WIFI_UTILS_H
