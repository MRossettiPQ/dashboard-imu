#ifndef WIFI_UTILS_H
#define WIFI_UTILS_H

// WiFi
#include <NTPClient.h>
#include <WiFi.h>
#include <WiFiUdp.h>

// Time API
#define NTP_TIME_API "a.st1.ntp.br"

// Filesystem inputs and paths
// Reference for the html
const char *input_ssid = "ssid";
const char *input_password = "password";

void setWiFi();
void startWiFi();
void eventsWiFi(WiFiEvent_t event);
void onConnectWifi();

#endif //WIFI_UTILS_H
