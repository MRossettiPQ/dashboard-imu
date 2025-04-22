#ifndef FS_UTILS_H
#define FS_UTILS_H
#include "config.h"
#include "logger.h"


inline void initFS() {
    if (!SPIFFS.begin(true)) {
        Logger::error("Fs", "An error has occurred while mounting SPIFFS");
    }
}

inline File loadFile(const char *path, const char* mode = FILE_READ) {
    Logger::info("Fs", "Load file: %s", path);

    File file = SPIFFS.open(path, mode);
    if (!file || file.isDirectory()) {
        Logger::error("Fs", "Failed to open file for reading");
        return {};
    }
    return file;
}

inline String readText(const char *path) {
    File file = loadFile(path, "r");

    String content;
    while (file.available()) {
        content = file.readStringUntil('\n');
        break;
    }
    file.close();
    return content;
}

inline JsonDocument readJson(const char *path) {
    File file = loadFile(path, "r");

    JsonDocument doc;
    const DeserializationError error = deserializeJson(doc, file);
    if (error) {
        Logger::error("Fs", "Failed to parse JSON file");
        return doc;
    }

    file.close();
    return doc;
}

inline void writeText(const char *path, const String &content) {
    File file = loadFile(path, "w");

    file.print(content);
    file.close();
    Logger::info("Fs", "Text written to file: %s", path);
}

inline void writeJson(const char *path, const JsonDocument &doc) {
    File file = loadFile(path, "w");

    if (serializeJson(doc, file) == 0) {
        Logger::error("Fs", "Failed to write JSON to file: %s", path);
    } else {
        Logger::info("Fs", "JSON written to file: %s", path);
    }

    file.close();
}


#endif //FS_UTILS_H
