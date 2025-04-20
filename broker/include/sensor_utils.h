#ifndef SENSOR_UTILS_H
#define SENSOR_UTILS_H

#include <Arduino.h>
#include <EEPROM.h>
#include <logger.h>
#include <Wire.h>
#include "MPU9250.h"

constexpr uint8_t EEPROM_SIZE = 1 + sizeof(float) * 3 * 4;
extern MPU9250 mpu;

#define ADDRESS_SENSOR 0x68
#define SDA_PIN 21
#define SCL_PIN 22

class SensorUtils;
extern SensorUtils sensor_instance;
MPU9250 mpu;

enum EEP_ADDR {
    EEP_CALIB_FLAG = 0x00,
    EEP_ACC_BIAS   = 0x01,
    EEP_GYRO_BIAS  = 0x0D,
    EEP_MAG_BIAS   = 0x19,
    EEP_MAG_SCALE  = 0x25
};

class SensorUtils {
    int nDevices = 0;
    bool calibrating = false;

    class Calibration {
        public:
            static void save() {
                Serial.println("Write calibrated parameters to EEPROM");
                writeByte(EEP_CALIB_FLAG, 1);
                writeFloat(EEP_ACC_BIAS + 0, mpu.getAccBias(0));
                writeFloat(EEP_ACC_BIAS + 4, mpu.getAccBias(1));
                writeFloat(EEP_ACC_BIAS + 8, mpu.getAccBias(2));
                writeFloat(EEP_GYRO_BIAS + 0, mpu.getGyroBias(0));
                writeFloat(EEP_GYRO_BIAS + 4, mpu.getGyroBias(1));
                writeFloat(EEP_GYRO_BIAS + 8, mpu.getGyroBias(2));
                writeFloat(EEP_MAG_BIAS + 0, mpu.getMagBias(0));
                writeFloat(EEP_MAG_BIAS + 4, mpu.getMagBias(1));
                writeFloat(EEP_MAG_BIAS + 8, mpu.getMagBias(2));
                writeFloat(EEP_MAG_SCALE + 0, mpu.getMagScale(0));
                writeFloat(EEP_MAG_SCALE + 4, mpu.getMagScale(1));
                writeFloat(EEP_MAG_SCALE + 8, mpu.getMagScale(2));
                #if defined(ESP_PLATFORM) || defined(ESP8266)
                EEPROM.commit();
                #endif
            }

            static void print() {
                constexpr float gyroSensitivity = MPU9250::CALIB_GYRO_SENSITIVITY;
                constexpr float accelSensitivity = MPU9250::CALIB_GYRO_SENSITIVITY;

                Logger::info("MPU", "Calibration parameters");

                Logger::info("MPU", "Calibrated? : %s", readByte(EEP_CALIB_FLAG, 0) ? "YES" : "NO");

                Logger::info("MPU", "Acc bias x  : %f", readFloat(EEP_MAG_BIAS + 0) * 1000.f / accelSensitivity);
                Logger::info("MPU", "Acc bias y  : %f", readFloat(EEP_MAG_BIAS + 4) * 1000.f / accelSensitivity);
                Logger::info("MPU", "Acc bias z  : %f", readFloat(EEP_MAG_BIAS + 8) * 1000.f / accelSensitivity);

                Logger::info("MPU", "Gyro bias x  : %f", readFloat(EEP_MAG_BIAS + 0) / gyroSensitivity);
                Logger::info("MPU", "Gyro bias y  : %f", readFloat(EEP_MAG_BIAS + 4) / gyroSensitivity);
                Logger::info("MPU", "Gyro bias z  : %f", readFloat(EEP_MAG_BIAS + 8) / gyroSensitivity);

                Logger::info("MPU", "Mag bias x  : %f", readFloat(EEP_MAG_BIAS + 0));
                Logger::info("MPU", "Mag bias y  : %f", readFloat(EEP_MAG_BIAS + 4));
                Logger::info("MPU", "Mag bias z  : %f", readFloat(EEP_MAG_BIAS + 8));

                Logger::info("MPU", "Mag scale x : %f", readFloat(EEP_MAG_SCALE + 0));
                Logger::info("MPU", "Mag scale y : %f", readFloat(EEP_MAG_SCALE + 4));
                Logger::info("MPU", "Mag scale z : %f", readFloat(EEP_MAG_SCALE + 8));
            }

            static void load() {
                Logger::info("MPU", "Load calibrated parameters from EEPROM");

                if (isCalibrated()) {
                    Logger::info("MPU", "calibrated? : YES");
                    Logger::info("MPU", "load calibrated values");
                    mpu.setAccBias(
                        readFloat(EEP_ACC_BIAS + 0),
                        readFloat(EEP_ACC_BIAS + 4),
                        readFloat(EEP_ACC_BIAS + 8)
                    );
                    mpu.setGyroBias(
                        readFloat(EEP_GYRO_BIAS + 0),
                        readFloat(EEP_GYRO_BIAS + 4),
                        readFloat(EEP_GYRO_BIAS + 8)
                    );
                    mpu.setMagBias(
                        readFloat(EEP_MAG_BIAS + 0),
                        readFloat(EEP_MAG_BIAS + 4),
                        readFloat(EEP_MAG_BIAS + 8)
                    );
                    mpu.setMagScale(
                        readFloat(EEP_MAG_SCALE + 0),
                        readFloat(EEP_MAG_SCALE + 4),
                        readFloat(EEP_MAG_SCALE + 8)
                    );
                }

                if (!isCalibrated()) {
                    Logger::info("MPU", "Calibrated? : NO");
                    Logger::info("MPU", "Load default values");
                    mpu.setAccBias(0., 0., 0.);
                    mpu.setGyroBias(0., 0., 0.);
                    mpu.setMagBias(0., 0., 0.);
                    mpu.setMagScale(1., 1., 1.);
                }
            }
    };

    const Calibration calibration = Calibration();


    static void writeByte(const int address, const byte value) {
        EEPROM.put(address, value);
    }

    static void writeFloat(const int address, const float value) {
        EEPROM.put(address, value);
    }

    static byte readByte(const int address, int i) {
        byte valueIn = 0;
        EEPROM.get(address, valueIn);
        return valueIn;
    }

    static float readFloat(const int address) {
        float valueIn = 0;
        EEPROM.get(address, valueIn);
        return valueIn;
    }

    static void clearCalibration() {
        writeByte(EEP_CALIB_FLAG, 0);
    }

    static bool isCalibrated() {
        return (readByte(EEP_CALIB_FLAG, 0) == 0x01);
    }

    public:
        void scanner() {
            while (nDevices == 0) {
                Logger::info("MPU", "Scanning...");

                for (byte address = 1; address < 127; address++) {
                    Wire.beginTransmission(address);
                    const byte error = Wire.endTransmission();
                    int info = address;

                    if (address < 16) {
                        info = 0;
                    }

                    if (error == 0) {
                        Logger::info("MPU", "I2C device found at address 0x%i", info);
                        nDevices++;
                    }

                    if (error == 4) {
                        Logger::error("Sensor", "Unknown error at address 0x%i", info);
                    }
                }

                if (nDevices == 0) {
                    Logger::warn("MPU", "No I2C devices found");
                }

                vTaskDelay(500 / portTICK_PERIOD_MS);
            }
        }

        static void configure() {
            // IMU initialization
            Wire.begin(SDA_PIN, SCL_PIN);
            Wire.setClock(400000);

            vTaskDelay(50 / portTICK_PERIOD_MS);

            MPU9250Setting setting;
            setting.accel_fs_sel = ACCEL_FS_SEL::A16G;
            setting.gyro_fs_sel = GYRO_FS_SEL::G2000DPS;
            setting.mag_output_bits = MAG_OUTPUT_BITS::M16BITS;
            setting.fifo_sample_rate = FIFO_SAMPLE_RATE::SMPL_1000HZ;
            setting.gyro_fchoice = 0x03;
            setting.gyro_dlpf_cfg = GYRO_DLPF_CFG::DLPF_184HZ;
            setting.accel_fchoice = 0x01;
            setting.accel_dlpf_cfg = ACCEL_DLPF_CFG::DLPF_218HZ_0;

            mpu.selectFilter(QuatFilterSel::MADGWICK);
            mpu.setFilterIterations(10);


            if (!mpu.setup(ADDRESS_SENSOR, setting)) {
                Logger::info("MPU", "It has not been initialized, Check the connection between the IMU and the ESP32 and restart the device");
            }

            if (mpu.available()) {
                Logger::info("MPU", "IMU Initialized");
            }
        }

        void blink() {
        }

        void calibrate() {
            #if defined(ESP_PLATFORM) || defined(ESP32)
            EEPROM.begin(0x80);
            #endif
            calibrating = true;

            Logger::info("MPU", "EEPROM start");
            if (!isCalibrated()) {
                Logger::info("MPU", "Need Calibration!!");
            }

            Logger::info("MPU", "EEPROM calibration value is : ");
            calibration.print();

            Logger::info("MPU", "Loaded calibration value is : ");
            calibration.load();

            mpu.verbose(true);
            Logger::info("MPU", "Accel Gyro calibration will start in 5 seconds");
            Logger::info("MPU", "Please leave the device still on the plan");

            // delay(5000);
            vTaskDelay(5000 / portTICK_PERIOD_MS);
            // digitalWrite(LED_SENSOR_CALIBRATION_PLAN, HIGH);

            mpu.calibrateAccelGyro();
            blink();
            // digitalWrite(LED_SENSOR_CALIBRATION_PLAN, LOW);

            Logger::info("MPU", "Magnetic calibration will start in 5 seconds");
            Logger::info("MPU", "Please wave the device in a figure eight until finished");
            // delay(5000);
            vTaskDelay(5000 / portTICK_PERIOD_MS);
            // digitalWrite(LED_SENSOR_CALIBRATION_EIGHT, HIGH);
            mpu.calibrateMag();

            // digitalWrite(LED_SENSOR_CALIBRATION_EIGHT, LOW);

            calibration.print();
            mpu.verbose(false);

            calibration.save();

            calibrating = false;
        }
};

#endif //SENSOR_UTILS_H
