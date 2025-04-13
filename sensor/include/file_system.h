#ifndef FILE_SYSTEM_H
#define FILE_SYSTEM_H

#include <Arduino.h>
#include "SPIFFS.h"

class FileSystem {
public:
    static void initFileSystem();
    static void writeFile(fs::FS& fs, const char* path, const char* message);
    static void printFileSystem();
    static String readFile(fs::FS& fs, const char* path);
};

#endif //FILE_SYSTEM_H
