#ifndef WIFI_UTILS_H
#define WIFI_UTILS_H

// WiFi
#include <NTPClient.h>
#include <WiFi.h>
#include <WiFiUdp.h>

// Time API
#define NTP_TIME_API "a.st1.ntp.br"

// Filesystem inputs and paths
const char* input_ssid = "ssid";
const char* input_password = "password";

WiFiUDP ntpUDP;

class ExternalConnection {
public:
    static IPAddress ip;
    IPAddress gateway = IPAddress(192, 168, 1, 1);
    IPAddress subnet = IPAddress(255, 255, 0, 0);

    static bool connected;
    static String ssid;
    static String password;
};

class InternalConnection {
public:
    static String ssid;
    static String password;
};

class WiFiUtils {
public:
    static String mac;
    static NTPClient timeClient;
    static ExternalConnection external_connection;
    static InternalConnection internal_connection;

    static String address;

    static void configure();
    static void check();
    static void events(WiFiEvent_t event);
    static void onConnect();
};


#endif //WIFI_UTILS_H
