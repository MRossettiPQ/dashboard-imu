const { io } = require("socket.io-client");

// Captura a quantidade de sensores passada por parâmetro (ex: node simulator.js 5)
const args = process.argv.slice(2);
const quantity = parseInt(args[0], 10) || 1;

const SERVER_URL = "http://dashboard.local:8001";
const BUFFER_LENGTH = 30;
const DELAY_INTERVAL_MS = 8;

class MpuSensorSimulator {
    constructor(id) {
        this.id = id;
        this.macAddress = `00:11:22:33:44:${String(id).padStart(2, '0')}`;
        this.ipAddress = `192.168.1.10${id}`;
        this.sensorName = `MPU-MANAGER-${this.macAddress}`;

        this.actualCommand = "NONE";
        this.measurementCountTotal = 0;
        this.buffer = [];
        this.loopInterval = null;

        // O offset garante que os movimentos gerados no mock sejam defasados entre os sensores
        this.timeOffset = id * 0.5;

        console.log(`[INIT] Preparando Sensor #${this.id} - MAC: ${this.macAddress}`);

        this.socket = io(SERVER_URL, {
            transports: ["websocket"]
        });

        this.setupSocketEvents();
    }

    getFormattedTime() {
        const now = new Date();
        return now.toISOString().substring(11, 19);
    }

    readSensor() {
        this.measurementCountTotal++;
        const t = (Date.now() / 1000) + this.timeOffset;

        // Rotação suave baseada no tempo e no offset do sensor
        const simulatedAngle = Math.sin(t) * 45;

        return {
            accelMssX: 0.1,
            accelMssY: 0.1,
            accelMssZ: 9.81 + (Math.random() * 0.05),
            accelLinX: 0,
            accelLinY: 0,
            accelLinZ: 0,
            gyroRadsX: Math.cos(t) * 0.5,
            gyroRadsY: 0.0,
            gyroRadsZ: 0.0,
            magBiasX: 0,
            magBiasY: 0,
            magBiasZ: 0,
            roll: simulatedAngle,
            pitch: simulatedAngle * 0.5,
            yaw: simulatedAngle * 0.25,
            eulerX: simulatedAngle,
            eulerY: simulatedAngle * 0.5,
            eulerZ: simulatedAngle * 0.25,
            quaternionW: Math.cos(simulatedAngle / 2),
            quaternionX: Math.sin(simulatedAngle / 2),
            quaternionY: 0,
            quaternionZ: 0,
            readOrder: this.measurementCountTotal,
            capturedAt: this.getFormattedTime()
        };
    }

    sendBuffer() {
        if (this.buffer.length === 0) return;

        const payload = {
            origin: "SENSOR",
            type: "SENSOR_SERVER_MEASUREMENT",
            originIdentifier: this.macAddress,
            content: this.buffer
        };

        this.socket.emit("measurement", payload);
        this.buffer = [];
    }

    startReadingLoop() {
        if (this.loopInterval) clearInterval(this.loopInterval);

        this.loopInterval = setInterval(() => {
            if (this.actualCommand === "START") {
                this.buffer.push(this.readSensor());

                if (this.buffer.length >= BUFFER_LENGTH) {
                    this.sendBuffer();
                }
            }
        }, DELAY_INTERVAL_MS);
    }

    stopReadingLoop() {
        this.actualCommand = "NONE";
        this.measurementCountTotal = 0;
        this.sendBuffer();
        if (this.loopInterval) {
            clearInterval(this.loopInterval);
            this.loopInterval = null;
        }
    }

    setupSocketEvents() {
        this.socket.on("connect", () => {
            console.log(`[Sensor #${this.id}] Conectado ao servidor!`);

            const registerPayload = {
                origin: "SENSOR",
                type: "SENSOR_SERVER_REGISTER_SENSOR",
                originIdentifier: this.macAddress,
                content: {
                    ip: this.ipAddress,
                    name: this.sensorName,
                    mac: this.macAddress,
                    type: "GYROSCOPE"
                }
            };

            this.socket.emit("SENSOR_SERVER_REGISTER_SENSOR", registerPayload);
        });

        this.socket.on("disconnect", () => {
            console.log(`[Sensor #${this.id}] Desconectado do servidor!`);
            this.stopReadingLoop();
        });

        this.socket.on("command", (data) => {
            const commandType = data.content;
            console.log(`[Sensor #${this.id}] Comando recebido: ${commandType}`);

            if (commandType === "START") {
                this.actualCommand = "START";
                this.startReadingLoop();
            }
            else if (commandType === "STOP") {
                this.stopReadingLoop();
            }
            else if (commandType === "RESTART") {
                this.stopReadingLoop();
                this.measurementCountTotal = 0;
                this.actualCommand = "START";
                this.startReadingLoop();
            }
            else if (commandType === "CALIBRATE") {
                console.log(`[Sensor #${this.id}] Calibrando... (pausa 5s)`);
                this.stopReadingLoop();
                setTimeout(() => {
                    console.log(`[Sensor #${this.id}] Calibração concluída. Reiniciando...`);
                    this.measurementCountTotal = 0;
                    this.actualCommand = "START";
                    this.startReadingLoop();
                }, 5000);
            }
        });

        this.socket.on("ping", () => {
            this.socket.emit("pong", {
                content: `Pong do ESP32 (Simulado #${this.id})`,
                type: "PONG",
                origin: "SENSOR"
            });
        });
    }
}

// Inicializa a quantidade solicitada de sensores
console.log(`Iniciando simulação de ${quantity} sensor(es)...`);
const sensors = [];
for (let i = 1; i <= quantity; i++) {
    sensors.push(new MpuSensorSimulator(i));
}