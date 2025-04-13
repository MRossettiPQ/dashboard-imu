#ifndef LOGGER_H
#define LOGGER_H

#include <Arduino.h>

class Logger {
public:
    static void debug(const char* tag, const char* fmt, ...);
    static void info(const char* tag, const char* fmt, ...);
    static void warn(const char* tag, const char* fmt, ...);
    static void error(const char* tag, const char* fmt, ...);

private:
    static void log(const char* level, const char* tag, const char* fmt, va_list args);
};

#endif //LOGGER_H
