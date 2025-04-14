#include "sensor.h"
#include "logger.h"




void SensorUtils::scannerI2C() {
    int nDevices = 0;
    do {
        byte error, address;
        Logger::info("MPU", "Scanning...");
        for (address = 1; address < 127; address++) {
            Wire.beginTransmission(address);
            error = Wire.endTransmission();
            if (error == 0) {
                Serial.print("I2C device found at address 0x");
                if (address < 16) {
                    Serial.print("0");
                }
                Logger::info("MPU", address, HEX);
                nDevices++;
            } else if (error == 4) {
                Serial.print("Unknown error at address 0x");
                if (address < 16) {
                    Serial.print("0");
                }
                Logger::info("MPU", address, HEX);
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

void InitIMU() {
    // IMU initialization
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
        // delay(500);
        vTaskDelay(500 / portTICK_PERIOD_MS);
    } while (!mpu.available());
}

void CalibrateIMU() {
#if defined(ESP_PLATFORM) || defined(ESP32)
    EEPROM.begin(0x80);
#endif
    calibrating = true;
    setupEEPROM();

    Logger::info("MPU", "[SENSOR] - Accel Gyro calibration will start in 5 seconds");
    Logger::info("MPU", "[SENSOR] - Please leave the device still on the plan");
    mpu.verbose(true);

    // delay(5000);
    vTaskDelay(5000 / portTICK_PERIOD_MS);
    // digitalWrite(LED_SENSOR_CALIBRATION_PLAN, HIGH);
    mpu.calibrateAccelGyro();
    // digitalWrite(LED_SENSOR_CALIBRATION_PLAN, LOW);

    Logger::info("MPU", "[SENSOR] - Magnetic calibration will start in 5 seconds");
    Logger::info("MPU", 
            "[SENSOR] - Please wave the device in a figure eight until finished");
    // delay(5000);
    vTaskDelay(5000 / portTICK_PERIOD_MS);
    // digitalWrite(LED_SENSOR_CALIBRATION_EIGHT, HIGH);
    mpu.calibrateMag();

    // digitalWrite(LED_SENSOR_CALIBRATION_EIGHT, LOW);

    PrintIMUCalibration();
    mpu.verbose(false);

    SaveIMUCalibration();

    LoadIMUCalibration();
    calibrating = false;
}

void SaveIMUCalibration() {
    Logger::info("MPU", "[SENSOR] - Saving sensor operation in EEPROM");
    saveCalibration();
}

void LoadIMUCalibration() {
    Logger::info("MPU", "[SENSOR] - Loading sensor calibration into EEPROM");
    loadCalibration();
}

void PrintIMUCalibration() {
    Logger::info("MPU", "< calibration parameters >");
    Logger::info("MPU", "accel bias [g]: ");
    Serial.print(mpu.getAccBiasX() * 1000.f /
                 (float) MPU9250::CALIB_ACCEL_SENSITIVITY);
    Serial.print("\", ");
    Serial.print(mpu.getAccBiasY() * 1000.f /
                 (float) MPU9250::CALIB_ACCEL_SENSITIVITY);
    Serial.print("\", ");
    Serial.print(mpu.getAccBiasZ() * 1000.f /
                 (float) MPU9250::CALIB_ACCEL_SENSITIVITY);
    Logger::info("MPU", );
    Logger::info("MPU", "gyro bias [deg/s]: ");
    Serial.print(mpu.getGyroBiasX() / (float) MPU9250::CALIB_GYRO_SENSITIVITY);
    Serial.print("\", ");
    Serial.print(mpu.getGyroBiasY() / (float) MPU9250::CALIB_GYRO_SENSITIVITY);
    Serial.print("\", ");
    Serial.print(mpu.getGyroBiasZ() / (float) MPU9250::CALIB_GYRO_SENSITIVITY);
    Logger::info("MPU", );
    Logger::info("MPU", "mag bias [mG]: ");
    Serial.print(mpu.getMagBiasX());
    Serial.print("\", ");
    Serial.print(mpu.getMagBiasY());
    Serial.print("\", ");
    Serial.print(mpu.getMagBiasZ());
    Logger::info("MPU", );
    Logger::info("MPU", "mag scale []: ");
    Serial.print(mpu.getMagScaleX());
    Serial.print("\", ");
    Serial.print(mpu.getMagScaleY());
    Serial.print("\", ");
    Serial.print(mpu.getMagScaleZ());
    Logger::info("MPU", );
}

String CreateJsonFromMeasurement(int MeasurementNumber) {
    String date = timeClient.getFormattedTime();
    // Accelerometer
    double AccelX_mss =
            mpu.getAccBiasX() * 1000.f / (float) MPU9250::CALIB_ACCEL_SENSITIVITY;
    double AccelY_mss =
            mpu.getAccBiasY() * 1000.f / (float) MPU9250::CALIB_ACCEL_SENSITIVITY;
    double AccelZ_mss =
            mpu.getAccBiasZ() * 1000.f / (float) MPU9250::CALIB_ACCEL_SENSITIVITY;
    // Accelerometer Linear
    double AccelX_Lin = mpu.getLinearAccX();
    double AccelY_Lin = mpu.getLinearAccY();
    double AccelZ_Lin = mpu.getLinearAccZ();
    // Gyroscope
    double GyroX_rads =
            mpu.getGyroBiasX() / (float) MPU9250::CALIB_GYRO_SENSITIVITY;
    double GyroY_rads =
            mpu.getGyroBiasY() / (float) MPU9250::CALIB_GYRO_SENSITIVITY;
    double GyroZ_rads =
            mpu.getGyroBiasZ() / (float) MPU9250::CALIB_GYRO_SENSITIVITY;
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

void StopMeasurement() {
    measurements = "";
    cmdActual = 0;
    numberOfBuffer = 0;
}

void RestartMeasurement() {
    StopMeasurement();
    numberMeasurement = 0;
    lastDispatch = 0;
    numberOfBuffer = 0;
}

void MountBufferToSend() {
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