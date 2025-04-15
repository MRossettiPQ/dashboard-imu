#include "main.h"
#include <Arduino.h>

TaskHandle_t Task1;
TaskHandle_t Task2;


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
        Logger::info("Task2code", "Running on core %d", current_millis);

        sensor_instance.scanner();
        sensor_instance.configure();

        wifi_instance.check();

        vTaskDelay(50 / portTICK_PERIOD_MS);
    }
}

void setup() {
    Serial.begin(115200);
    Serial.setDebugOutput(true);
    Logger::info("SETUP", "Initialize sensor");

    wifi_instance.configure();

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
