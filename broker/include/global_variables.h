#ifndef GLOBAL_VARIABLES_H
#define GLOBAL_VARIABLES_H

#include <Arduino.h>

String ssid;
String password;
String backend;
String sensorName;

const char *SSID_PATH = "/config/ssid.txt";
const char *PASSWORD_PATH = "/config/password.txt";
const char *BACKEND_PATH = "/config/backend.txt";
const char *SENSOR_NAME_PATH = "/config/sensorName.txt";

#endif //GLOBAL_VARIABLES_H
