#ifndef LED_UTILS_H
#define LED_UTILS_H
#include "config.h"

inline void powerLed() {
    digitalWrite(LED_READY, HIGH);
    vTaskDelay(200 / portTICK_PERIOD_MS);
}

inline void resetLed() {
    digitalWrite(LED_READY, LOW);
    vTaskDelay(200 / portTICK_PERIOD_MS);
}

inline void setupLed() {
    pinMode(LED_READY, OUTPUT);
}

inline void blinkLed(const int seconds) {
    const unsigned long startMillis = millis();
    unsigned long elapsedTime = 0;

    while (elapsedTime < (seconds * 1000)) {
        powerLed();
        resetLed();

        elapsedTime = millis() - startMillis;
    }
}

#endif //LED_UTILS_H
