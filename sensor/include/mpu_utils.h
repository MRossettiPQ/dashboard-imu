#ifndef MPU_SOCKET_SERVER_MPU_SENSOR_H
#define MPU_SOCKET_SERVER_MPU_SENSOR_H

#include <config.h>
#include <fs_utils.h>
#include <led_utils.h>
#include <logger.h>

constexpr uint8_t EEPROM_SIZE = 1 + sizeof(float) * 3 * 4;
MPU9250 mpu;

#define ADDRESS_SENSOR 0x68
#define SDA_PIN 21
#define SCL_PIN 22

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

    class Calibration {
        public:
            static void save() {
                Logger::info("MPU", "Write calibrated parameters to EEPROM");
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

                Logger::info("MPU", "EEPROM calibration parameters");
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
                    Logger::info("MPU", "Calibrated? : YES");
                    Logger::info("MPU", "Load calibrated values");

                    const float abx = readFloat(EEP_ACC_BIAS + 0);
                    const float aby = readFloat(EEP_ACC_BIAS + 4);
                    const float abz = readFloat(EEP_ACC_BIAS + 8);
                    mpu.setAccBias(abx, aby, abz);

                    const float gbx = readFloat(EEP_GYRO_BIAS + 0);
                    const float gby = readFloat(EEP_GYRO_BIAS + 4);
                    const float gbz = readFloat(EEP_GYRO_BIAS + 8);
                    mpu.setGyroBias(gbx, gby, gbz);

                    const float mbx = readFloat(EEP_MAG_BIAS + 0);
                    const float mby = readFloat(EEP_MAG_BIAS + 4);
                    const float mbz = readFloat(EEP_MAG_BIAS + 8);
                    mpu.setMagBias(mbx, mby, mbz);

                    const float msx = readFloat(EEP_MAG_SCALE + 0);
                    const float msy = readFloat(EEP_MAG_SCALE + 4);
                    const float msz = readFloat(EEP_MAG_SCALE + 8);
                    mpu.setMagScale(msx, msy, msz);
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
        static JsonObject read() {
            JsonDocument measurement;
            String read_at = timeClient.getFormattedTime();
            const double acc_bias_x = mpu.getAccBiasX();
            const double acc_bias_y = mpu.getAccBiasY();
            const double acc_bias_z = mpu.getAccBiasZ();
            const double acc_lin_x = mpu.getLinearAccX();
            const double acc_lin_y = mpu.getLinearAccY();
            const double acc_lin_z = mpu.getLinearAccZ();
            const double gyro_bias_x = mpu.getGyroBiasX();
            const double gyro_bias_y = mpu.getGyroBiasY();
            const double gyro_bias_z = mpu.getGyroBiasZ();
            const double mag_bias_x = mpu.getMagBiasX();
            const double mag_bias_y = mpu.getMagBiasY();
            const double mag_bias_z = mpu.getMagBiasZ();
            const double roll = mpu.getRoll();
            const double pitch = mpu.getPitch();
            const double yaw = mpu.getYaw();
            const double euler_x = mpu.getEulerX();
            const double euler_y = mpu.getEulerY();
            const double euler_z = mpu.getEulerZ();
            const double quaternion_w = mpu.getQuaternionW();
            const double quaternion_x = mpu.getQuaternionX();
            const double quaternion_y = mpu.getQuaternionY();
            const double quaternion_z = mpu.getQuaternionZ();

            // Accelerometer
            const double accel_x_mss = acc_bias_x * 1000.f / static_cast<float>(MPU9250::CALIB_ACCEL_SENSITIVITY);
            const double accel_y_mss = acc_bias_y * 1000.f / static_cast<float>(MPU9250::CALIB_ACCEL_SENSITIVITY);
            const double accel_z_mss = acc_bias_z * 1000.f / static_cast<float>(MPU9250::CALIB_ACCEL_SENSITIVITY);
            measurement["accel_x_mss"] = accel_x_mss;
            measurement["accel_y_mss"] = accel_y_mss;
            measurement["accel_z_mss"] = accel_z_mss;

            // Accelerometer Linear
            measurement["acc_lin_x"] = acc_lin_x;
            measurement["acc_lin_y"] = acc_lin_y;
            measurement["acc_lin_z"] = acc_lin_z;

            // Gyroscope
            const double gyro_rads_x = gyro_bias_x / static_cast<float>(MPU9250::CALIB_GYRO_SENSITIVITY);
            const double gyro_rads_y = gyro_bias_y / static_cast<float>(MPU9250::CALIB_GYRO_SENSITIVITY);
            const double gyro_rads_z = gyro_bias_z / static_cast<float>(MPU9250::CALIB_GYRO_SENSITIVITY);
            measurement["gyro_rads_x"] = gyro_rads_x;
            measurement["gyro_rads_y"] = gyro_rads_y;
            measurement["gyro_rads_z"] = gyro_rads_z;

            // Magnetometer
            measurement["mag_bias_x"] = mag_bias_x;
            measurement["mag_bias_y"] = mag_bias_y;
            measurement["mag_bias_z"] = mag_bias_z;

            // Roll, Pitch e Yaw
            measurement["roll"] = roll;
            measurement["pitch"] = pitch;
            measurement["yaw"] = yaw;

            // Euler
            measurement["euler_x"] = euler_x;
            measurement["euler_y"] = euler_y;
            measurement["euler_z"] = euler_z;

            // Quaternion
            measurement["quaternion_w"] = quaternion_w;
            measurement["quaternion_x"] = quaternion_x;
            measurement["quaternion_y"] = quaternion_y;
            measurement["quaternion_z"] = quaternion_z;

            String payload;
            serializeJsonPretty(measurement, payload);

            Logger::info("SETUP", "Reading sensor: %s", payload.c_str());

            return measurement.as<JsonObject>();
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

        void calibrate(const bool on_init = false) {
            calibrating = true;
            #if defined(ESP_PLATFORM) || defined(ESP32)
            EEPROM.begin(0x80);
            #endif

            Logger::info("MPU", "EEPROM start");
            if (!isCalibrated()) {
                Logger::info("MPU", "Need Calibration!!");
            }

            calibration.print();

            Logger::info("MPU", "Loaded calibration value is : ");
            calibration.load();

            JsonDocument memory = readJson("/memory.json");
            const bool calibrated = memory["calibrate"].as<bool>();
            if (!on_init || !calibrated) {
                mpu.verbose(true);

                Logger::info("MPU", "Accel Gyro calibration will start in 5 seconds");
                Logger::info("MPU", "Please leave the device still on the plan");
                blinkLed(5);
                mpu.calibrateAccelGyro();

                Logger::info("MPU", "Magnetic calibration will start in 5 seconds");
                Logger::info("MPU", "Please wave the device in a figure eight until finished");
                blinkLed(8);
                mpu.calibrateMag();

                memory["calibrate"] = true;
                writeJson("/memory.json", memory);

                resetLed();

                calibration.save();

                calibration.print();
                mpu.verbose(false);
            }

            calibrating = false;
        }
};

auto sensor_instance = SensorUtils();
#endif  // MPU_SOCKET_SERVER_MPU_SENSOR_H
