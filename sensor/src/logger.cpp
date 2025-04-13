#include "Logger.h"

void Logger::debug(const char* tag, const char* fmt, ...) {
    va_list args;
    va_start(args, fmt);
    log("DEBUG", tag, fmt, args);
    va_end(args);
}

void Logger::info(const char* tag, const char* fmt, ...) {
    va_list args;
    va_start(args, fmt);
    log("INFO", tag, fmt, args);
    va_end(args);
}

void Logger::warn(const char* tag, const char* fmt, ...) {
    va_list args;
    va_start(args, fmt);
    log("WARN", tag, fmt, args);
    va_end(args);
}

void Logger::error(const char* tag, const char* fmt, ...) {
    va_list args;
    va_start(args, fmt);
    log("ERROR", tag, fmt, args);
    va_end(args);
}

void Logger::log(const char* level, const char* tag, const char* fmt, va_list args) {
    char message[256];
    vsnprintf(message, sizeof(message), fmt, args);
    Serial.printf("[%s][%s][Core_%d] %s\n", level, tag, xPortGetCoreID(), message);
}
