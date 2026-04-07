#ifndef MQTT_UTILS_H
#define MQTT_UTILS_H

#include <PubSubClient.h>
#include <WiFiClient.h>
#include "config.h"
#include "logger.h"

// ─── Estado MQTT ────────────────────────────────────────────────────────
WiFiClient espWifiClient;
PubSubClient mqttClient(espWifiClient);

String mqtt_session_id = "";       // UUID da sessão atribuída pelo servidor
bool mqtt_connected = false;
unsigned long last_mqtt_reconnect = 0;
constexpr unsigned long MQTT_RECONNECT_INTERVAL = 5000;
String mqtt_target_host = "";
IPAddress mqtt_resolved_ip(0, 0, 0, 0);

// ─── Tópicos ────────────────────────────────────────────────────────────
inline String topicSensorRegister() {
	// Enviar registro do sensor
    return "sensor/" + WiFi.macAddress() + "/register";
}

inline String topicSensorStatus() {
	// Enviar status do sensor
    return "sensor/" + WiFi.macAddress() + "/status";
}

inline String topicCalibrate() {
	// Receber comandos (CALIBRATE)
    return "sensor/" + WiFi.macAddress() + "/calibrate";
}

inline String topicSessionCommand() {
	if (mqtt_session_id.isEmpty()) return "";
	return "session/" + mqtt_session_id + "/command";
}

inline String topicSessionMeasurement() {
	// Enviar medições
    if (mqtt_session_id.isEmpty()) return "";
    return "session/" + mqtt_session_id + "/sensor/" + WiFi.macAddress() + "/measurements";
}

inline void mqttCallback(const char* topic, const byte* payload, const unsigned int length);

// ─── Classe principal ───────────────────────────────────────────────────

class MqttUtils {
public:
    /**
     * Configura o client MQTT com servidor e callback.
     * Chamado após conexão WiFi ser estabelecida.
     */
	static void configure(const char* host, const uint16_t port) {
		mqtt_target_host = String(host);
		mqtt_target_host.replace("http://", "");
		mqtt_target_host.replace(".local", ""); // Limpa para sobrar apenas "dashboard"

		mqttClient.setCallback(mqttCallback);
		mqttClient.setBufferSize(4096);
		mqttClient.setKeepAlive(30);
		Logger::info("MQTT", "Configurado para buscar o host mDNS: %s", mqtt_target_host.c_str());
	}

    /**
     * Tenta reconectar ao broker MQTT.
     * Chamado periodicamente pelo loop da Task2.
     */
    static bool reconnect() {
        if (mqttClient.connected()) {
            mqtt_connected = true;
            return true;
        }

        const unsigned long now = millis();
        if (now - last_mqtt_reconnect < MQTT_RECONNECT_INTERVAL) {
            return false;
        }
        last_mqtt_reconnect = now;

		const IPAddress emptyIP(0, 0, 0, 0);
		if (mqtt_resolved_ip == emptyIP) {
			Logger::info("MQTT", "Resolvendo %s.local via mDNS...", mqtt_target_host.c_str());

			// O MDNS "pergunta" na rede quem é o dashboard
			mqtt_resolved_ip = MDNS.queryHost(mqtt_target_host);

			if (mqtt_resolved_ip != emptyIP) {
				Logger::info("MQTT", "IP resolvido via mDNS: %s", mqtt_resolved_ip.toString().c_str());
				mqttClient.setServer(mqtt_resolved_ip, MQTT_PORT); // Injeta o IP descoberto
			} else {
				Logger::error("MQTT", "Falha ao resolver mDNS. Tentando novamente depois...");
				return false;
			}
		}
		// ──────────────────────

		const String clientId = "ESP32-" + WiFi.macAddress();

		// Define um username que o seu backend aceite (ex: começando com MPU)
		const String username = "MPU-" + WiFi.macAddress();
		const char* password = nullptr;

		Logger::info("MQTT", "Conectando como %s (User: %s)...", clientId.c_str(), username.c_str());

		if (mqttClient.connect(clientId.c_str(), username.c_str(), password)) {
    		mqtt_connected = true;

    		// Sempre escuta calibração
    		mqttClient.subscribe(topicCalibrate().c_str());
    		Logger::info("MQTT", "Inscrito em: %s", topicCalibrate().c_str());

    		registerSensor();
    		publishStatus("AVAILABLE");
    		return true;
    	} else {
            mqtt_connected = false;
            Logger::error("MQTT", "Falha na conexão, rc=%d", mqttClient.state());
    		mqtt_resolved_ip = IPAddress(0, 0, 0, 0);
            return false;
        }
    }

    /**
     * Loop do client MQTT. Deve ser chamado frequentemente.
     */
    static void loop() {
        if (!mqttClient.connected()) {
            mqtt_connected = false;
            reconnect();
        }
        mqttClient.loop();
    }

    /**
     * Publica o registro deste sensor no broker.
     * Tópico: sensor/{mac}/register
     */
    static void registerSensor() {
        JsonDocument doc;
        doc["mac"] = WiFi.macAddress();
        doc["name"] = ap_ssid; // Nome do AP configurado no wifi_utils
        doc["ip"] = sta_ip;

        String payload;
        serializeJson(doc, payload);

        const String topic = topicSensorRegister();
        const bool sent = mqttClient.publish(topic.c_str(), payload.c_str());
        Logger::info("MQTT", "Registro enviado [%s]: %s (ok=%d)", topic.c_str(), payload.c_str(), sent);
    }

    /**
     * Publica um batch de medições.
     * Tópico: session/{sessionId}/measurement
     *
     * O payload segue o formato:
     * {
     *   "originIdentifier": "AA:BB:CC:DD:EE:FF",
     *   "content": [ { medição1 }, { medição2 }, ... ]
     * }
     *
     * O readOrder em cada medição garante a ordem de reconstrução no servidor.
     */
    static bool publishMeasurements(const JsonDocument& measurementBuffer) {
        if (mqtt_session_id.isEmpty()) {
            Logger::warn("MQTT", "Sem sessão atribuída, medições descartadas");
            return false;
        }

        if (!mqttClient.connected()) {
            Logger::warn("MQTT", "Desconectado, medições descartadas");
            return false;
        }

        const String topic = topicSessionMeasurement();
        String payload;
        serializeJson(measurementBuffer, payload);

        // PubSubClient suporta mensagens até bufferSize (4096 por padrão)
        // Se o payload for maior, será necessário fragmentar
        if (payload.length() > static_cast<unsigned int>(mqttClient.getBufferSize() - 10)) {
            Logger::warn("MQTT", "Payload muito grande (%d bytes), considere reduzir BUFFER_LENGTH", payload.length());
            return false;
        }

        const bool sent = mqttClient.publish(topic.c_str(), payload.c_str());
        if (sent) {
            Logger::info("MQTT", "Batch enviado [%s]: %d bytes", topic.c_str(), payload.length());
        } else {
            Logger::error("MQTT", "Falha ao enviar batch [%s]", topic.c_str());
        }

        return sent;
    }

    /**
     * Publica o status atual do sensor.
     * Tópico: sensor/{mac}/status
     * Status possíveis: AVAILABLE, CALIBRATING, ERROR
     */
    static void publishStatus(const char* status, const char* message = nullptr) {
        JsonDocument doc;
        doc["status"] = status;
        if (message != nullptr) {
            doc["message"] = message;
        }

        String payload;
        serializeJson(doc, payload);

        const String topic = topicSensorStatus();
        mqttClient.publish(topic.c_str(), payload.c_str());
        Logger::info("MQTT", "Status publicado [%s]: %s", topic.c_str(), status);
    }

    /**
     * Verifica se está conectado ao broker.
     */
    static bool isConnected() {
        return mqtt_connected && mqttClient.connected();
    }
};


// ─── Callback de mensagens recebidas ────────────────────────────────────
inline void mqttCallback(const char* topic, const byte* payload, const unsigned int length) {
    // Converte payload para String
    String message;
    message.reserve(length);
    for (unsigned int i = 0; i < length; i++) {
        message += static_cast<char>(payload[i]);
    }

    const String topicStr(topic);
    Logger::info("MQTT", "Mensagem recebida [%s]: %s", topicStr.c_str(), message.c_str());

    // Parse do JSON
    JsonDocument doc;
    const DeserializationError error = deserializeJson(doc, message);
    if (error) {
        Logger::error("MQTT", "Erro ao parsear JSON: %s", error.c_str());
        return;
    }

    const char* message_command = doc["command"];
    if (message_command == nullptr) {
        Logger::warn("MQTT", "Mensagem sem campo 'command'");
        return;
    }

    const String cmd(message_command);
    if (cmd == "START" && actual_command != CommandType::START) {
        Logger::info("MQTT", "Comando START recebido");
        actual_command = CommandType::START;
        measurement_count = 0;
        measurement_count_total = 0;
        last_dispatch = 0;
		MqttUtils::publishStatus("MEASURING");
    }
    else if (cmd == "STOP" && actual_command != CommandType::STOP) {
        Logger::info("MQTT", "Comando STOP recebido");
        actual_command = CommandType::STOP;
    }
    else if (cmd == "RESTART") {
        Logger::info("MQTT", "Comando RESTART recebido");
        actual_command = CommandType::RESTART;
        measurement_count = 0;
        measurement_count_total = 0;
        last_dispatch = 0;
    }
    else if (cmd == "CALIBRATE") {
        Logger::info("MQTT", "Comando CALIBRATE recebido");
        actual_command = CommandType::STOP;

        // Publica status de calibração
        MqttUtils::publishStatus("CALIBRATING");

        // Executa calibração (bloqueia a task)
        sensor_instance.calibrate();

        // Publica status de disponível
        MqttUtils::publishStatus("AVAILABLE");

        actual_command = CommandType::NONE;
    }
    else if (cmd == "ASSIGN") {
    	const char* sessionId = doc["sessionId"];
    	if (sessionId != nullptr) {
    		mqtt_session_id = String(sessionId);
    		Logger::info("MQTT", "Atribuído à sessão: %s", mqtt_session_id.c_str());

    		// Agora subscreve nos comandos da sessão
    		const String cmdTopic = topicSessionCommand();
    		mqttClient.subscribe(cmdTopic.c_str());
    		Logger::info("MQTT", "Inscrito em: %s", cmdTopic.c_str());
    	}
    }
    else if (cmd == "RELEASE") {
    	Logger::info("MQTT", "Liberado da sessão: %s", mqtt_session_id.c_str());

    	// Cancela subscrição dos comandos da sessão
    	mqttClient.unsubscribe(topicSessionCommand().c_str());

    	mqtt_session_id = "";
    	actual_command = CommandType::NONE;
    	MqttUtils::publishStatus("AVAILABLE");  // ← faltava isso
    }
    else {
        Logger::warn("MQTT", "Comando desconhecido: %s", cmd.c_str());
    }
}

#endif // MQTT_UTILS_H