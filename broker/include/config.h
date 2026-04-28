#ifndef CONFIG_H
#define CONFIG_H

#include <Arduino.h>
#include <AsyncTCP.h>
#include <cstring>
#include <DNSServer.h>
#include <ESPAsyncWebServer.h>
#include <NTPClient.h>
#include <WiFi.h>
#include "ArduinoJson.h"
#include "SPIFFS.h"

// Pin led
#define LED_READY 2

// Configuração da rede AP
String ap_ssid = "";
String ap_password = "";
String ap_ip = "";

String sta_ssid = "";
String sta_password = "";
String sta_ip = "";

// Configuração de IP para o AP
IPAddress local_ip(192, 168, 4, 1);
IPAddress gateway(192, 168, 4, 1);
IPAddress subnet(255, 255, 255, 0);

#define WEB_PORT 80
AsyncWebServer confServer(WEB_PORT);

// Time
#define NTP_TIME_API "a.st1.ntp.br"
DNSServer dnsServer;
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, NTP_TIME_API, -3 * 3600, 60000);


// Network
bool connectedWifi = false;
bool setupExternalWifCompleted = false;
unsigned long last_wifi_try_connect = 0;
void setupInternalWifi();
void loadConfigWifi();
void setupExternalWifi();
void checkExternalWifi();
JsonDocument listAvailableWifi();
void eventsWiFi(WiFiEvent_t event);

// Led
void blinkLed(int seconds);

// Server
void setupServer();
void startServer();

// Fs
void initFS();
File loadFile(const char* path);
String readText(const char* path);
JsonDocument readJson(const char* path);
void writeText(const char *path, const String &content);
void writeJson(const char *path, const JsonDocument &doc);

#endif //CONFIG_H
