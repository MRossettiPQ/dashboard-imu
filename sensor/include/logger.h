#ifndef LOGGER_H
#define LOGGER_H

#include <Arduino.h>

// Variável para controlar se o "START" já foi enviado
bool alreadySent = false;

class Logger {
    public:
        static void setup() {
            Serial.begin(115200);
        }
        static void debug(const char* tag, const char* fmt, ...) {
            va_list args;
            va_start(args, fmt);
            log("DEBUG", tag, fmt, args);
            va_end(args);
        }

        static void info(const char* tag, const char* fmt, ...) {
            va_list args;
            va_start(args, fmt);
            log("INFO", tag, fmt, args);
            va_end(args);
        }

        static void warn(const char* tag, const char* fmt, ...) {
            va_list args;
            va_start(args, fmt);
            log("WARN", tag, fmt, args);
            va_end(args);
        }

        static void error(const char* tag, const char* fmt, ...) {
            va_list args;
            va_start(args, fmt);
            log("ERROR", tag, fmt, args);
            va_end(args);
        }

    private:
        static void log(const char* level, const char* tag, const char* fmt, const va_list args) {
            char message[256];
            vsnprintf(message, sizeof(message), fmt, args);
            Serial.printf("[%s][%s][Core_%d] %s\n", level, tag, xPortGetCoreID(), message);
        }
};

#endif //LOGGER_H
