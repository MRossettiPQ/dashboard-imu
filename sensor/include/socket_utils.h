#ifndef SOCKET_UTILS_H
#define SOCKET_UTILS_H
#include "logger.h"
#include "config.h"

void socketIOEvent(socketIOmessageType_t type, uint8_t * payload, size_t length) {
    switch(type) {
        case sIOtype_DISCONNECT:
            USE_SERIAL.printf("[IOc] Disconnected!\n");
        break;
        case sIOtype_CONNECT:
            USE_SERIAL.printf("[IOc] Connected to url: %s\n", payload);

        // join default namespace (no auto join in Socket.IO V3)
        socketIO.send(sIOtype_CONNECT, "/");
        break;
        case sIOtype_EVENT:
            USE_SERIAL.printf("[IOc] get event: %s\n", payload);
        break;
        case sIOtype_ACK:
            USE_SERIAL.printf("[IOc] get ack: %u\n", length);
        hexdump(payload, length);
        break;
        case sIOtype_ERROR:
            USE_SERIAL.printf("[IOc] get error: %u\n", length);
        hexdump(payload, length);
        break;
        case sIOtype_BINARY_EVENT:
            USE_SERIAL.printf("[IOc] get binary: %u\n", length);
        hexdump(payload, length);
        break;
        case sIOtype_BINARY_ACK:
            USE_SERIAL.printf("[IOc] get binary ack: %u\n", length);
        hexdump(payload, length);
        break;
    }
}

class WebSocketClient {
    public:
        void configure() {
            webSocket.beginSocketIO(server_url, SOCKET_PORT, "/socket.io/?EIO=4&transport=websocket");
            webSocket.onEvent(socketIOEvent);
            webSocket.setReconnectInterval(5000);
        }

        void loop() {
          webSocket.loop();
        }

        void send(JsonDocument json) {
            String payload;
            serializeJson(json, payload);
            webSocket.sendTXT(payload);
        }
};

#endif //SOCKET_UTILS_H
