#include "file_system.h"
#include "logger.h"
#include "global_variables.h"

void FileSystem::initFileSystem() {
    if (!SPIFFS.begin(true)) {
        Logger::info("FileSystem", "An error has occurred while mounting SPIFFS");
    }
    Logger::info("FileSystem", "SPIFFS mounted successfully");

    ssid = FileSystem::readFile(SPIFFS, SSID_PATH);
    password = FileSystem::readFile(SPIFFS, PASSWORD_PATH);
    backend = FileSystem::readFile(SPIFFS, BACKEND_PATH);
    sensorName = FileSystem::readFile(SPIFFS, SENSOR_NAME_PATH);

    FileSystem::printFileSystem();
}