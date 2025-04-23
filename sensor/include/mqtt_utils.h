#ifndef MQTT_UTILS_H
#define MQTT_UTILS_H
#include "logger.h"
#include "config.h"

class MqttClient {
    public:
    void configure() {
    }

    void loop() {
    }

    void send(JsonDocument json) {
        String payload;
        serializeJson(json, payload);
    }

};

#endif //MQTT_UTILS_H
