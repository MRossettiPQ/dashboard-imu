#include "config.h"
#include "fs_utils.h"
#include "led_utils.h"
#include "logger.h"
#include "server_utils.h"
#include "wifi_utils.h"

void setup() {
    initFS();
    setupLed();
    Logger::setup();
    Logger::info("SETUP", "Initializing...");
    Logger::info("SETUP", "Chip model: %s", ESP.getChipModel());
    Logger::info("SETUP", "Chip revision: %d", ESP.getChipRevision());
    Logger::info("SETUP", "Number of cores: %d", ESP.getChipCores());

    setupInternalWifi();
    setupServer();
    startServer();
    setupExternalWifi();
}

void loop() {
    const unsigned long current_millis = millis();
    connectedWifi = WiFiClass::status() == WL_CONNECTED;

    if ((current_millis >= (last_wifi_try_connect + 1000)) && connectedWifi != true && setupExternalWifCompleted) {
        last_wifi_try_connect = current_millis;
        checkExternalWifi();
    }

    vTaskDelay(200 / portTICK_PERIOD_MS);
}
