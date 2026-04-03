const mqtt = require("mqtt");

// Captura a quantidade de sensores passada por parâmetro (ex: node mqtt_simulator.js 5)
const args = process.argv.slice(2);
const quantity = parseInt(args[0], 10) || 1;

// Ajuste para o host e porta onde o Moquette está rodando
const MQTT_URL = "mqtt://localhost:1883";
const BUFFER_LENGTH = 30;
const DELAY_INTERVAL_MS = 8;

class MpuSensorSimulatorMqtt {
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

        // Conecta ao broker MQTT usando o MAC como Client ID
        this.client = mqtt.connect(MQTT_URL, {
            clientId: this.macAddress,
            clean: true,
            connectTimeout: 4000,
            reconnectPeriod: 1000,
        });

        this.setupMqttEvents();
    }

    getFormattedTime() {
        const now = new Date();
        return now.toISOString().substring(11, 19);
    }

    readSensor() {
        this.measurementCountTotal++;
        const t = (Date.now() / 1000) + this.timeOffset;

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

        // O Kotlin espera um MqttMessage<MutableList<MeasurementDto>> no tópico "measurement"
        const payload = {
            origin: "SENSOR",
            type: "SENSOR_SERVER_MEASUREMENT",
            originIdentifier: this.macAddress,
            content: this.buffer
        };

        this.client.publish("measurement", JSON.stringify(payload), { qos: 0 });
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

    setupMqttEvents() {
        this.client.on("connect", () => {
            console.log(`[Sensor #${this.id}] Conectado ao broker MQTT!`);

            // Assina o tópico de comandos para receber instruções do backend
            this.client.subscribe("command", (err) => {
                if (!err) {
                    console.log(`[Sensor #${this.id}] Inscrito no tópico 'command'`);
                } else {
                    console.error(`[Sensor #${this.id}] Erro ao assinar tópico 'command'`, err);
                }
            });

            // Envia uma mensagem inicial de registro no tópico 'message' (ou outro tópico de controle)
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
            this.client.publish("message", JSON.stringify(registerPayload));
        });

        this.client.on("message", (topic, message) => {
            // Lida com as mensagens recebidas (comandos do servidor)
            if (topic === "command") {
                try {
                    const data = JSON.parse(message.toString());

                    // Verifica se o comando é genérico (broadcast) ou direcionado a este sensor específico
                    // Se o seu backend envia para todos, não precisa dessa validação, mas é uma boa prática
                    if (data.originIdentifier && data.originIdentifier !== this.macAddress && data.originIdentifier !== "BROADCAST") {
                        return;
                    }

                    const commandType = data.content;
                    console.log(`[Sensor #${this.id}] Comando recebido no tópico '${topic}': ${commandType}`);

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
                } catch (error) {
                    console.error(`[Sensor #${this.id}] Erro ao parsear comando JSON`, error);
                }
            }
        });

        this.client.on("error", (err) => {
            console.error(`[Sensor #${this.id}] Erro de conexão MQTT:`, err);
            this.stopReadingLoop();
        });

        this.client.on("offline", () => {
            console.log(`[Sensor #${this.id}] Broker MQTT offline...`);
            this.stopReadingLoop();
        });
    }
}

// Inicializa a quantidade solicitada de sensores
console.log(`Iniciando simulação MQTT de ${quantity} sensor(es)...`);
for (let i = 1; i <= quantity; i++) {
    new MpuSensorSimulatorMqtt(i);
}