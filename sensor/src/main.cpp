#include "config.h"
#include "fs_utils.h"
#include "led_utils.h"
#include "logger.h"
#include "mpu_utils.h"
#include "mqtt_utils.h"
#include "wifi_utils.h"

// ─── Buffer de medições ─────────────────────────────────────────────────

void sendBuffer() {
    if (buffer["content"].is<JsonArray>() && buffer["content"].as<JsonArray>().size() > 0) {
        MqttUtils::publishMeasurements(buffer);
    }

    last_dispatch = measurement_count_total;
    buffer.clear();
}

void stop() {
    actual_command = CommandType::NONE;

    // Envia buffer remanescente antes de parar
    sendBuffer();

    last_dispatch = 0;
    measurement_count = 0;
    measurement_count_total = 0;
}

void restart() {
    actual_command = CommandType::START;
    last_dispatch = 0;
    measurement_count = 0;
    measurement_count_total = 0;
    buffer.clear();
}

// ─── Acumula medição no buffer e envia quando cheio ─────────────────────

void stack() {
    if (mpu.update()) {
        if (!buffer["content"].is<JsonArray>()) {
            buffer["content"].to<JsonArray>();
        }

        buffer["originIdentifier"] = WiFi.macAddress();

        const JsonObject measurement = SensorUtils::read();
        measurement_count_total++;
        measurement_count++;

        buffer["content"].as<JsonArray>().add(measurement);

        // Buffer de BUFFER_LENGTH medições (ex: 40 = ~333ms a 120Hz)
        if (measurement_count_total == (last_dispatch + BUFFER_LENGTH)) {
            Logger::info("SENSOR", "Buffer cheio, enviando %d medições", BUFFER_LENGTH);
            sendBuffer();
        }
    }

    // Proteção contra overflow do contador
    if (measurement_count_total == INT_MAX) {
        measurement_count_total = 0;
        sendBuffer();
    }
}

// ─── Tasks FreeRTOS ─────────────────────────────────────────────────────

/**
 * Task 1 (Core 0): Leitura do sensor e acumulação no buffer.
 * Roda no loop mais rápido possível quando em modo START.
 */
[[noreturn]] void Task1code(void* parameter) {
    Logger::info("Task1", "Iniciada no core %d", xPortGetCoreID());

    for (;;) {
        const unsigned long current_millis = millis();

        if ((current_millis >= (previous_millis + delay_interval)) && connectedWifi) {
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
                    // Calibração é tratada no callback MQTT
                    break;
                default:
                    break;
            }
        }

        vTaskDelay(1 / portTICK_PERIOD_MS);
    }
}

/**
 * Task 2 (Core 1): Manutenção de WiFi e MQTT.
 * Reconecta WiFi se necessário e mantém o client MQTT ativo.
 */
[[noreturn]] void Task2code(void* parameter) {
    Logger::info("Task2", "Iniciada no core %d", xPortGetCoreID());

    for (;;) {
        const unsigned long current_millis = millis();
        connectedWifi = WiFiClass::status() == WL_CONNECTED;

        // Reconexão WiFi
        if ((current_millis >= (last_wifi_try_connect + 1000)) && !connectedWifi && setupExternalWifiCompleted) {
            last_wifi_try_connect = current_millis;
            checkExternalWifi();
        }

        // Loop MQTT (mantém conexão + processa callbacks)
        if (connectedWifi) {
            MqttUtils::loop();
        }

        vTaskDelay(1 / portTICK_PERIOD_MS);
    }
}

// ─── Setup ──────────────────────────────────────────────────────────────

void setup() {
    initFS();
    setupLed();
    Logger::setup();

    Logger::info("SETUP", "Inicializando...");
    Logger::info("SETUP", "Chip: %s rev %d, %d cores", ESP.getChipModel(), ESP.getChipRevision(), ESP.getChipCores());

    // Configura e calibra o sensor IMU
    SensorUtils::configure();
    sensor_instance.calibrate(true);

    // Configura WiFi (AP interno + STA externo)
    setupInternalWifi();
    setupExternalWifi();

    // Configura MQTT (usa o server_url e MQTT_PORT do config.h)
    // Ajuste server_url e MQTT_PORT no seu config.h para apontar ao broker Moquette
    MqttUtils::configure(server_url, MQTT_PORT);

    // Cria tasks em cores separados
    TaskHandle_t Task1;
    TaskHandle_t Task2;

    xTaskCreatePinnedToCore(Task1code, "SensorTask", 10000, nullptr, 1, &Task1, 0);
    xTaskCreatePinnedToCore(Task2code, "NetworkTask", 10000, nullptr, 1, &Task2, 1);
}

void loop() {
    // Lógica controlada por FreeRTOS tasks
}