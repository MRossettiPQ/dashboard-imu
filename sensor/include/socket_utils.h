#ifndef SOCKET_UTILS_H
#define SOCKET_UTILS_H
#include "logger.h"
#include "config.h"

inline void socketIOEvent(socketIOmessageType_t type, uint8_t* payload, size_t length) {
    switch(type) {
        case sIOtype_DISCONNECT:
            Logger::info("Socket", "Disconnected!");
        	break;

        case sIOtype_CONNECT:
            Logger::info("Socket", "Connected to url: %s", payload);
        	// join default namespace (no auto join in Socket.IO V3)
            socketIO.sendEVENT("[\"hello\",{\"msg\":\"ESP32 conectado!\"}]");
        	break;

        case sIOtype_ACK:
            Logger::info("Socket", "Get ack: %u", length);
        	break;

        case sIOtype_ERROR:
            Logger::info("Socket", "Get error: %u", length);
        	break;

        case sIOtype_BINARY_EVENT:
            Logger::info("Socket", "Get binary: %u", length);
        	break;

        case sIOtype_BINARY_ACK:
            Logger::info("Socket", "Get binary ack: %u", length);
        	break;

        case sIOtype_EVENT:
            Logger::info("Socket", "Get event: %s", payload);
            // Parse o evento e o payload
            JsonDocument content;
            const DeserializationError error = deserializeJson(content, payload, length);
            if (error) {
                Serial.print("deserializeJson() failed: ");
                Serial.println(error.f_str());
                return;
            }

            // doc[0] é o nome do evento
            // doc[1] é o objeto
            const char* event_name = content[0];
            const JsonObject data = content[1].as<JsonObject>();
            if (strcmp(event_name, "command") == 0) {
                actual_command = getCommandTypeFromJson(data["content"].as<String>().c_str());
                return;
            }
            if (strcmp(event_name, "calibrate") == 0) {
                actual_command = CommandType::STOP;
                sensor_instance.calibrate();
                actual_command = CommandType::RESTART;
                return;
            }
            if (strcmp(event_name, "ping") == 0) {
                socketIO.sendEVENT(R"(["pong",{"content":"Pong do ESP32","type":"PONG","origin":"SENSOR"}])");
            }
        	break;
    }
}

class WebSocketClient {
    public:
        static void configure() {
            socketIO.onEvent(socketIOEvent);
            socketIO.setReconnectInterval(5000);
            socketIO.begin(server_url, SOCKET_PORT, "/socket.io/?EIO=4&transport=websocket");
        }

        static void loop() {
          socketIO.loop();
        }

        static void send(const String& event, const JsonDocument& json) {
            String serialized;
            serializeJson(json, serialized);
    		socketIO.sendEVENT("[\"" + event + "\"," + serialized + "]");
        }
};

#endif //SOCKET_UTILS_H
