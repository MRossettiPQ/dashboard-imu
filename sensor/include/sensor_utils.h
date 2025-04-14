#ifndef MPU_H
#define MPU_H

#include <Wire.h>
#include <logger.h>

class SensorUtils {
  public:
    static void scannerI2C() {
    int nDevices = 0;
    do {
        byte error, address;
        Logger::info("MPU", "Scanning...");
        for (address = 1; address < 127; address++) {
            Wire.beginTransmission(address);
            error = Wire.endTransmission();
            if (error == 0) {
                Logger::info("MPU", "I2C device found at address 0x");
                if (address < 16) {
                    Serial.print("0");
                }
                Logger::info("MPU", "%b", address, HEX);
                nDevices++;
            } else if (error == 4) {
                Serial.print("Unknown error at address 0x");
                if (address < 16) {
                    Serial.print("0");
                }
                Logger::info("MPU", "%b", address, HEX);
            }
        }
        if (nDevices == 0) {
            Logger::info("MPU", "No I2C devices found");
        } else {
            Logger::info("MPU", "done");
        }
        delay(5000);
    } while (nDevices == 0);
  }
void init() {
    do {
        Wire.begin(SDA_PIN, SCL_PIN);
        Wire.setClock(400000);
        // delay(50);
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
            Logger::info("MPU",
                    "[SENSOR] - It has not been initialized, Check the connection "
                    "between the IMU and the ESP32 and restart the device");
            Logger::info("MPU", &"[SENSOR] - Status: "[status]);
        } else {
            Logger::info("MPU", "[SENSOR] - IMU Initialized");
        }

        vTaskDelay(500 / portTICK_PERIOD_MS);
    } while (!mpu.available());
}

void CalibrateIMU() {
    #if defined(ESP_PLATFORM) || defined(ESP32)
        EEPROM.begin(0x80);
    #endif
    calibrating = true;
    setupEEPROM();

    Logger::info("MPU", "Accel Gyro calibration will start in 5 seconds");
    Logger::info("MPU", "Please leave the device still on the plan");
    mpu.verbose(true);

    // delay(5000);
    vTaskDelay(5000 / portTICK_PERIOD_MS);
    // digitalWrite(LED_SENSOR_CALIBRATION_PLAN, HIGH);
    mpu.calibrateAccelGyro();
    // digitalWrite(LED_SENSOR_CALIBRATION_PLAN, LOW);

    Logger::info("MPU", "Magnetic calibration will start in 5 seconds");
    Logger::info("MPU", "Please wave the device in a figure eight until finished");
    // delay(5000);
    vTaskDelay(5000 / portTICK_PERIOD_MS);
    // digitalWrite(LED_SENSOR_CALIBRATION_EIGHT, HIGH);
    mpu.calibrateMag();

    // digitalWrite(LED_SENSOR_CALIBRATION_EIGHT, LOW);

    printIMUCalibration();
    mpu.verbose(false);

    saveIMUCalibration();

    loadIMUCalibration();
    calibrating = false;
}

void saveIMUCalibration() {
    Logger::info("MPU", "[SENSOR] - Saving sensor operation in EEPROM");
    saveCalibration();
}

void loadIMUCalibration() {
    Logger::info("MPU", "[SENSOR] - Loading sensor calibration into EEPROM");
    loadCalibration();
}

void printIMUCalibration() {
  	float calibAccelSensitivity = MPU9250::CALIB_ACCEL_SENSITIVITY;
  	float calibGyroSensitivity = MPU9250::CALIB_GYRO_SENSITIVITY;
    Logger::info("MPU", "Calibration parameters");

    float accX = mpu.getAccBiasX() * 1000.f / calibAccelSensitivity;
    float accY = mpu.getAccBiasY() * 1000.f / calibAccelSensitivity;
    float accZ = mpu.getAccBiasZ() * 1000.f / calibAccelSensitivity;
    Logger::info("MPU", "Accel bias [g]: { x: %f,  y: %f,  z: %f }", accX, accY, accZ);

    float gyroX = mpu.getGyroBiasX() / calibGyroSensitivity;
    float gyroY = mpu.getGyroBiasY() / calibGyroSensitivity;
    float gyroZ = mpu.getGyroBiasZ() / calibGyroSensitivity;
    Logger::info("MPU", "Gyro bias [deg/s]: { x: %f,  y: %f,  z: %f }", gyroX, gyroY, gyroZ);

	float magFieldX = mpu.getMagneticFieldX();
	float magFieldY = mpu.getMagneticFieldY();
	float magFieldZ = mpu.getMagneticFieldZ();
    Logger::info("MPU", "Mag bias [mG]: { x: %f,  y: %f,  z: %f }", magFieldX, magFieldY, magFieldZ);

    float magScaleX = mpu.getMagScaleX();
    float magScaleY = mpu.getMagScaleY();
    float magScaleZ = mpu.getMagScaleZ();
    Logger::info("MPU", "Mag scale []: { x: %f,  y: %f,  z: %f }", magScaleX, magScaleY, magScaleZ);
}

String createJsonFromMeasurement(int MeasurementNumber) {
  	float calibAccelSensitivity = MPU9250::CALIB_ACCEL_SENSITIVITY;
  	float calibGyroSensitivity = MPU9250::CALIB_GYRO_SENSITIVITY;
    String date = timeClient.getFormattedTime();
    // Accelerometer
    double AccelX_mss = mpu.getAccBiasX() * 1000.f / calibAccelSensitivity;
    double AccelY_mss = mpu.getAccBiasY() * 1000.f / calibAccelSensitivity;
    double AccelZ_mss = mpu.getAccBiasZ() * 1000.f / calibAccelSensitivity;
    // Accelerometer Linear
    double AccelX_Lin = mpu.getLinearAccX();
    double AccelY_Lin = mpu.getLinearAccY();
    double AccelZ_Lin = mpu.getLinearAccZ();
    // Gyroscope
    double GyroX_rads = mpu.getGyroBiasX() / calibGyroSensitivity;
    double GyroY_rads = mpu.getGyroBiasY() / calibGyroSensitivity;
    double GyroZ_rads = mpu.getGyroBiasZ() / calibGyroSensitivity;
    // Magnetometer
    double MagX_uT = mpu.getMagBiasX();
    double MagY_uT = mpu.getMagBiasY();
    double MagZ_uT = mpu.getMagBiasZ();
    // Roll, Pitch e Yaw
    double Roll = mpu.getRoll();
    double Pitch = mpu.getPitch();
    double Yaw = mpu.getYaw();
    // Roll, Pitch e Yaw
    double Euler_X = mpu.getEulerX();
    double Euler_Y = mpu.getEulerY();
    double Euler_Z = mpu.getEulerZ();
    // Roll, Pitch e Yaw
    double Quaternion_X = mpu.getQuaternionX();
    double Quaternion_Y = mpu.getQuaternionY();
    double Quaternion_Z = mpu.getQuaternionZ();
    double Quaternion_W = mpu.getQuaternionW();

    //-----------------Sensor----------------
    String message = R"({"sensorName":")";
    message += sensorName;
    message += R"(","numberMensuration":")";
    message += MeasurementNumber;
    message += R"(","hourMensuration":")";
    message += date;
    //--------------Acelerometro--------------
    message += R"(","AccelX_mss":")";
    message += AccelX_mss;
    message += R"(","AccelY_mss":")";
    message += AccelY_mss;
    message += R"(","AccelZ_mss":")";
    message += AccelZ_mss;
    //--------------Acel. Liner---------------
    message += R"(","Acc_X":")";
    message += AccelX_Lin;
    message += R"(","Acc_Y":")";
    message += AccelY_Lin;
    message += R"(","Acc_Z":")";
    message += AccelZ_Lin;
    //---------------Giroscópio---------------
    message += R"(","Gyr_X":")";
    message += GyroX_rads;
    message += R"(","Gyr_Y":")";
    message += GyroY_rads;
    message += R"(","Gyr_Z":")";
    message += GyroZ_rads;
    //--------------Magnetometro--------------
    message += R"(","Mag_X":")";
    message += MagX_uT;
    message += R"(","Mag_Y":")";
    message += MagY_uT;
    message += R"(","Mag_Z":")";
    message += MagZ_uT;
    //----------Roll, Pitch e Yaw-------------
    message += R"(","Roll":")";
    message += Roll;
    message += R"(","Pitch":")";
    message += Pitch;
    message += R"(","Yaw":")";
    message += Yaw;
    //----------Euler_X, Euler_Y e Euler_Z-------------
    message += R"(","Euler_X":")";
    message += Euler_X;
    message += R"(","Euler_Y":")";
    message += Euler_Y;
    message += R"(","Euler_Z":")";
    message += Euler_Z;
    // QuaternionX, QuaternionY, QuaternionZ e QuaternionW
    message += R"(","Quaternion_X":")";
    message += Quaternion_X;
    message += R"(","Quaternion_Y":")";
    message += Quaternion_Y;
    message += R"(","Quaternion_Z":")";
    message += Quaternion_Z;
    message += R"(","Quaternion_W":")";
    message += Quaternion_W;
    message += "\"}";
    return message;
}

void stopMeasurement() {
    measurements = "";
    cmdActual = 0;
    numberOfBuffer = 0;
}

void restartMeasurement() {
    stopMeasurement();
    numberMeasurement = 0;
    lastDispatch = 0;
    numberOfBuffer = 0;
}

void mountBufferToSend() {
    if (numberOfBuffer != 0) {
        // Adiciona uma , no fim da posição do objeto quando não for o primeiro
        // elemento do array
        measurements += ",";
    }
    numberMeasurement = numberMeasurement + 1;
    numberOfBuffer = numberOfBuffer + 1;
    measurements += CreateJsonFromMeasurement(numberMeasurement);

    // Buffer de 40 Measurement = BUFFER_LENGTH /  = 120Hz, default BUFFER_LENGTH = 40
    if (numberMeasurement == (lastDispatch + BUFFER_LENGTH)) {
        Logger::info("MPU", "\n[SENSOR] - Send buffer");

        String content = R"({"origin":"SENSOR","type":"MEASUREMENT_LIST","message":[)" + measurements + "]}";
        confServerSocket.textAll(content);

        numberOfBuffer = 0;
        lastDispatch = numberMeasurement;

        // measurements.clear();
        measurements = "";
    }
}

};


#endif //MPU_H
