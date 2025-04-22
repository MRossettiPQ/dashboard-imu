#include "config.h"
#include "fs_utils.h"
#include "led_utils.h"
#include "logger.h"
#include "mpu_utils.h"
#include "wifi_utils.h"

#define CHANNEL 1  // Canal padrão
#define WIFI_TIMEOUT_MS 10000


TaskHandle_t Task1;
TaskHandle_t Task2;

int count = 0;
JsonDocument command;
JsonDocument measurements;

void StopMeasurement() {
    // measurements = "";
    cmdActual = 0;
    numberOfBuffer = 0;
}

void RestartMeasurement() {
    StopMeasurement();
    numberMeasurement = 0;
    lastDispatch = 0;
    numberOfBuffer = 0;
}

void MountBufferToSend() {
    if (numberOfBuffer != 0) {
        // Adiciona uma , no fim da posição do objeto quando não for o primeiro
        // elemento do array
        measurements += ",";
    }
    numberMeasurement = numberMeasurement + 1;
    numberOfBuffer = numberOfBuffer + 1;
    measurements += CreateJsonFromMeasurement(numberMeasurement);

    // Buffer de 40 Measurement = BUFFER_LENGTH /  = 120Hz, default BUFFER_LENGTH = 40
    if (numberMeasurement == (lastDispatch + BUFFER_LENGTH)) {
        Serial.println("\n[SENSOR] - Send buffer");

        String content = R"({"origin":"SENSOR","type":"MEASUREMENT_LIST","message":[)" + measurements + "]}";
        confServerSocket.textAll(content);

        numberOfBuffer = 0;
        lastDispatch = numberMeasurement;

        // measurements.clear();
        measurements = "";
    }
}

[[noreturn]] void Task1code(void* parameter) {
    Logger::info("Task1code", "Running on core %d", xPortGetCoreID());
    for (;;) {
        const unsigned long current_millis = millis();
        Logger::info("Task1code", "Running on core %d", current_millis);

        vTaskDelay(50 / portTICK_PERIOD_MS);
    }
}

[[noreturn]] void Task2code(void* parameter) {
    Logger::info("Task2code", "Running on core %d", xPortGetCoreID());
    for (;;) {
        const unsigned long current_millis = millis();
        connectedWifi = WiFiClass::status() == WL_CONNECTED;

        if ((current_millis >= (last_wifi_try_connect + 1000)) && connectedWifi != true && setupExternalWifCompleted) {
            last_wifi_try_connect = current_millis;
            checkExternalWifi();
        }

        vTaskDelay(50 / portTICK_PERIOD_MS);
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

    sensor_instance.configure();
    sensor_instance.calibrate(true);

    setupInternalWifi();
    setupExternalWifi();

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
}
