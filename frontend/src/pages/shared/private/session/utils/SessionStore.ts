import type { ProcedureType } from 'src/common/models/procedure/Procedure';
import { Procedure } from 'src/common/models/procedure/Procedure';
import { SessionNavigation } from 'pages/shared/private/session/utils/SessionNavigation';
import type { MovementType } from 'src/common/models/movement/Movement';
import { Movement } from 'src/common/models/movement/Movement';
import { PositionEnum, Sensor, SensorType } from 'src/common/models/sensor/Sensor';
import { Measurement } from 'src/common/models/measurement/Measurement';
import dayjs from 'dayjs';

class SessionStore extends SessionNavigation {
  procedureTypes: ProcedureType[] = [];
  viewType: 'grid' | 'unified' | 'table' | 'summary' = 'unified';

  selectedProcedure: Procedure | undefined = undefined;
  selectedMovement: Movement | undefined = undefined;
  inProgress: boolean = false;

  constructor() {
    super();
  }

  get actualMovementLabel(): string | undefined {
    return this.selectedMovement?.type;
  }

  addSample(sensorN: number = 2): void {
    const getRandomArbitrary = ({ min, max }: { min: number; max: number }): number =>
      Math.random() * (max - min) + min;

    this.selectedProcedure ??= new Procedure();

    if (!this.selectedMovement) {
      const movement = new Movement();
      const addIterator = 0;
      while (addIterator < sensorN) {
        const sensor = new Sensor();
        sensor.ip = `192.168.16.10${addIterator}`;
        sensor.sensorName = `Sensor ${addIterator}`;
        sensor.observation = `Sensor ${addIterator} de exemplo`;
        sensor.type = SensorType.GYROSCOPE;
        sensor.position = addIterator === 0 ? PositionEnum.ONE : PositionEnum.TWO;
        movement.sensors.push(sensor);
      }
      this.selectedMovement = movement;
    }

    const now = dayjs();
    let iterator: number = 0;
    while (iterator < 365) {
      iterator++;
      for (const sensor of this.selectedMovement.sensors) {
        const index: number = this.selectedMovement.sensors.indexOf(sensor);
        const number = sensor.measurements.length ? sensor.measurements.length : 0;
        const measurement = new Measurement();
        measurement.sensorName = sensor.sensorName ?? `undefined_sensor ${index}`;
        measurement.readOrder = number;

        measurement.roll = getRandomArbitrary({ min: 90, max: 80 });
        measurement.pitch = getRandomArbitrary({ min: 90, max: 80 });
        measurement.yaw = getRandomArbitrary({ min: 90, max: 80 });

        measurement.eulerX = getRandomArbitrary({ min: 90, max: 80 });
        measurement.eulerY = getRandomArbitrary({ min: 90, max: 80 });
        measurement.eulerZ = getRandomArbitrary({ min: 90, max: 80 });

        measurement.gyroRadsX = getRandomArbitrary({ min: 90, max: 80 });
        measurement.gyroRadsY = getRandomArbitrary({ min: 90, max: 80 });
        measurement.gyroRadsZ = getRandomArbitrary({ min: 90, max: 80 });

        measurement.accelLinX = getRandomArbitrary({ min: 90, max: 80 });
        measurement.accelLinY = getRandomArbitrary({ min: 90, max: 80 });
        measurement.accelLinZ = getRandomArbitrary({ min: 90, max: 80 });

        measurement.accelMssX = getRandomArbitrary({ min: 90, max: 80 });
        measurement.accelMssY = getRandomArbitrary({ min: 90, max: 80 });
        measurement.accelMssZ = getRandomArbitrary({ min: 90, max: 80 });

        measurement.magBiasX = getRandomArbitrary({ min: 90, max: 80 });
        measurement.magBiasY = getRandomArbitrary({ min: 90, max: 80 });
        measurement.magBiasZ = getRandomArbitrary({ min: 90, max: 80 });

        measurement.quaternionX = getRandomArbitrary({ min: 90, max: 80 });
        measurement.quaternionY = getRandomArbitrary({ min: 90, max: 80 });
        measurement.quaternionZ = getRandomArbitrary({ min: 90, max: 80 });
        measurement.quaternionW = getRandomArbitrary({ min: 90, max: 80 });

        measurement.capturedAt = now.add(number, 'ms');

        sensor.measurements.push(measurement);
      }
    }
  }
  addProcedure(procedureType: ProcedureType | undefined, movementTypes: MovementType[]): void {
    if (!procedureType?.type) {
      return;
    }

    console.log('Add procedure');
    if (this.procedures.some((p) => p.type === procedureType.type)) {
      return;
    }

    const procedure = new Procedure();
    procedure.type = procedureType.type;

    for (const movementType of movementTypes) {
      if (!movementType.type) {
        continue;
      }

      const movement = new Movement();
      movement.type = movementType.type;
      procedure.movements.push(movement);
    }

    this.procedures.push(procedure);
  }

  selectProcedure(index: number): void {
    this.selectedProcedure = this.procedures[index];
    if (this.selectedProcedure) {
      this.selectedMovement = this.selectedProcedure.movements[0];
    }
  }

  selectMovement(movement: Movement): void {
    console.log(movement);
  }

  printProcedure() {
    console.log(this.procedures);
  }

  start() {
    console.log('normal');
  }
}

export { SessionStore };
