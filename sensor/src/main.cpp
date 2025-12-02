#include "config.h"
#include "fs_utils.h"
#include "led_utils.h"
#include "logger.h"
#include "mpu_utils.h"
#include "socket_utils.h"
#include "wifi_utils.h"

void sendBuffer() {
    WebSocketClient::send("measurement", buffer);

    measurement_count_total = 0;
    last_dispatch = measurement_count;

    buffer.clear();
}

void stop() {
    actual_command = CommandType::NONE;
    last_dispatch = 0;
    measurement_count = 0;
    measurement_count_total = 0;
    sendBuffer();
}

void restart() {
    actual_command = CommandType::START;
    last_dispatch = 0;
    measurement_count = 0;
    measurement_count_total = 0;
}

struct TaskParams {};

void stack() {
    if (mpu.update()) {
        if (!buffer["content"].is<JsonArray>()) {
            const bool has_array = buffer["content"].to<JsonArray>();
            if (has_array) {
                Logger::info("SENSOR", "'content' converted to array");
            }
        }

        buffer["origin"] = "SENSOR";
        buffer["type"] = "SENSOR_SERVER_MEASUREMENT";
        buffer["originIdentifier"] = WiFi.macAddress();

        const JsonObject measurement = SensorUtils::read();
        measurement_count_total++;
        measurement_count++;

        const bool has_array = buffer["content"].as<JsonArray>().add(measurement);
        if (has_array) {
            Logger::info("SENSOR", "Element has added");
        }

        // Buffer de 40 Measurement = BUFFER_LENGTH /  = 120Hz, default BUFFER_LENGTH = 40
        if (measurement_count_total == (last_dispatch + BUFFER_LENGTH)) {
            Logger::info("SENSOR", "Send buffer");
            sendBuffer();
        }
    }

    if (INT_MAX == measurement_count_total) {
        measurement_count_total = 0;
        sendBuffer();
    }
}

[[noreturn]] void Task1code(void* parameter) {
    auto const* params = static_cast<TaskParams*>(parameter);
    Logger::info("Task1code", "Running on core %d", xPortGetCoreID());
    for (;;) {
        const unsigned long current_millis = millis();
        Logger::info("Task1code", "Running on core %d", current_millis);

        if ((current_millis >= (previous_millis + delay_interval)) && connectedWifi == true) {
            switch (actual_command) {
                case CommandType::START:
                    stack();
                    break;
                case CommandType::STOP:
                    stop();
                    break;
                case CommandType::RESTART:
                    restart();
                    break;
                case CommandType::CALIBRATE:
                    break;
                default:
                    Logger::info("Task1code", "Nothing");
                    break;
            }
        }

        vTaskDelay(1 / portTICK_PERIOD_MS);
    }
}

[[noreturn]] void Task2code(void* parameter) {
    auto const* params = static_cast<TaskParams*>(parameter);
    Logger::info("Task2code", "Running on core %d", xPortGetCoreID());
    for (;;) {
        const unsigned long current_millis = millis();
        connectedWifi = WiFiClass::status() == WL_CONNECTED;

        if ((current_millis >= (last_wifi_try_connect + 1000)) && connectedWifi != true && setupExternalWifiCompleted) {
            last_wifi_try_connect = current_millis;
            checkExternalWifi();
        }

        WebSocketClient::loop();

        vTaskDelay(1 / portTICK_PERIOD_MS);
    }
}

void setup() {
    initFS();
    setupLed();
    Logger::setup();
    Logger::info("SETUP", "Initializing...");
    Logger::info("SETUP", "Chip model: %s", ESP.getChipModel());
    Logger::info("SETUP", "Chip revision: %d", ESP.getChipRevision());
    Logger::info("SETUP", "Number of cores: %d", ESP.getChipCores());

    SensorUtils::configure();
    sensor_instance.calibrate(true);

    setupInternalWifi();
    setupExternalWifi();


    TaskHandle_t Task1;
    TaskHandle_t Task2;

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
    // Not used: logic is handled by FreeRTOS tasks.
}
