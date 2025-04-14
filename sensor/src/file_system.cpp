#include "file_system.h"
#include "logger.h"
#include "global_variables.h"

void FileSystem::initFileSystem() {
    if (!SPIFFS.begin(true)) {
        Logger::info("FileSystem", "An error has occurred while mounting SPIFFS");
    }
    Logger::info("FileSystem", "SPIFFS mounted successfully");

    ssid = readFile(SPIFFS, SSID_PATH);
    password = readFile(SPIFFS, PASSWORD_PATH);
    backend = readFile(SPIFFS, BACKEND_PATH);
    sensorName = readFile(SPIFFS, SENSOR_NAME_PATH);

    printFileSystem();
}

void FileSystem::printFileSystem() {
    Logger::warn("FileSystem", "SSID: %s", ssid);
    Logger::warn("FileSystem", "Password: %s", password);
    Logger::warn("FileSystem", "Backend url: %s", backend);
    Logger::warn("FileSystem", "Sensor name: %s", sensorName);
}

String FileSystem::readFile(FS& fs, const char* path) {
    Logger::info("FileSystem", "Reading file: %s\r\n", path);

    File file = fs.open(path);
    if (!file || file.isDirectory()) {
        Logger::error("FileSystem", "Failed to open file for reading - %s", path);
        return {};
    }

    String fileContent;
    if (file.available()) {
        fileContent = file.readStringUntil('\n');
    }
    return fileContent;
}

void FileSystem::writeFile(FS& fs, const char* path, const char* message) {
    Logger::info("FileSystem", "Writing file: %s\r\n", path);

    File file = fs.open(path, FILE_WRITE);
    if (!file) {
        Logger::info("FileSystem", "Failed to open file for writing - %s", path);
        return;
    }

    if (file.print(message)) {
        Logger::info("FileSystem", "File written - %s", path);
    }
    else {
        Logger::error("FileSystem", "File write failed - %s", path);
    }
}
