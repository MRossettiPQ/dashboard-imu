//
// Created by mathe on 12/04/2025.
//
#include <Arduino.h>
#include "main.h"

TaskHandle_t Task1;
TaskHandle_t Task2;

[[noreturn]] void Task1code(void* parameter) {
    Logger::info("Task1code", "Running on core %d\n", xPortGetCoreID());
    for (;;) {
        unsigned long current_millis = millis();
        Logger::info("Task1code", "Running on core %d\n", current_millis);

        vTaskDelay(100 / portTICK_PERIOD_MS);
    }
}

[[noreturn]] void Task2code(void* parameter) {
    Logger::info("Task2code", "Running on core %d\n", xPortGetCoreID());
    for (;;) {
        unsigned long current_millis = millis();
        Logger::info("Task2code", "Running on core %d\n", current_millis);

        vTaskDelay(100 / portTICK_PERIOD_MS);
    }
}

void setup() {
    Serial.begin(115200);
    Serial.setDebugOutput(true);
    Logger::info("SETUP", "Initialize sensor");

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
