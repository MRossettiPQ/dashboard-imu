#ifndef CONFIG_H
#define CONFIG_H

#include <Arduino.h>
#include <cstring>
#include <EEPROM.h>
#include <NTPClient.h>
#include <SocketIOclient.h>
#include <WebSocketsClient.h>
#include <WiFi.h>
#include <Wire.h>
#include "ArduinoJson.h"
#include "MPU9250.h"
#include "SPIFFS.h"

String server_url = "http://dashboard.local";
String server_ip = "192.168.0.1";
#define API_PORT 8000
#define SOCKET_PORT 8001
#define MQTT_PORT 8002

// Pin led
#define LED_READY 2

// Configuração da rede AP
String ap_ssid = "";
String ap_password = "";
String ap_ip = "";

String sta_ssid = "DASHBOARD_NETWORK_AP";
String sta_password = "12345678";
String sta_ip = "";

String sensor_name = "";

// Configuração de IP para o AP
IPAddress local_ip(192, 168, 4, 1);
IPAddress gateway(192, 168, 4, 1);
IPAddress subnet(255, 255, 255, 0);

// Time
#define NTP_TIME_API "a.st1.ntp.br"
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, NTP_TIME_API, -3 * 3600, 60000);

WiFiMulti WiFiMulti;
SocketIOclient socketIO;

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

// Fs
void initFS();
File loadFile(const char* path);
String readText(const char* path);
JsonDocument readJson(const char* path);
void writeText(const char *path, const String &content);
void writeJson(const char *path, const JsonDocument &doc);

// Sensor
void configureMpu();
void scannerMpu();

#endif //CONFIG_H
