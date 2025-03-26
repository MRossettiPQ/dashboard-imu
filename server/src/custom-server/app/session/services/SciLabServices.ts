import Session from "../models/Session";
import {
  getArrayDivision,
  getArrayPow,
  getArraySqrt,
  getArraySubtract,
  getColumn,
  getMax,
  getMean,
  getMin,
  getSqrt,
  getStDeviation,
} from "../../../core/utils/SciLabUtil";
import Movement from "../models/Movement";
import Sensor, { PositionEnum } from "../models/Sensor";
import GyroMeasurement from "../models/GyroMeasurement";

export interface ResultVariabilityCenter {
  minPitch: number;
  maxPitch: number;
  varPitch: number;
  minAtorn: number;
  maxAtorn: number;
  varAtorn: number;
  meanRmsRAtorn: unknown;
  sdRmsRAtorn: unknown;
  meanRmsRPitch1p: unknown;
  sdRmsRPitch1p: unknown;
}

export interface MovementResult {
  movement: Movement;
  values?: ResultVariabilityCenter;
  chartOption?: unknown;
  atorn?: unknown;
}

export interface SessionResultVo {
  session: Session;
  result: MovementResult[];
}

export const { getAllCalc, calculationVariabilityCenter } = new (class SciLabServices {
  async getAllCalc(movements: Movement[]): Promise<MovementResult[]> {
    const promises: Promise<MovementResult>[] = [];

    for (const movement of movements) {
      promises.push(this.calculationVariabilityCenter(movement));
    }

    return Promise.all(promises);
  }

  async calculationVariabilityCenter(movement: Movement): Promise<MovementResult> {
    const sensors = movement.sensors;
    const result: MovementResult = {
      movement,
    };

    try {
      if (sensors.length) {
        // Sensor 1
        const sensor1: Sensor = sensors.find((sensor: Sensor): boolean => {
          return sensor.position === PositionEnum.ONE;
        });
        const gyroMeasurements1: GyroMeasurement[] = sensor1.gyro_measurements;

        // Sensor 2
        const sensor2: Sensor = sensors.find((sensor: Sensor): boolean => {
          return sensor.position === PositionEnum.TWO;
        });
        const gyroMeasurements2: GyroMeasurement[] = sensor2.gyro_measurements;

        //
        if (gyroMeasurements1 !== null && gyroMeasurements2 !== null) {
          const length: number = gyroMeasurements1?.length;

          const pitch1: number[] = getColumn(gyroMeasurements1, "pitch");
          let yaw1: number[] = getColumn(gyroMeasurements1, "yaw");

          yaw1 = yaw1.map((yaw) => yaw * -1);

          const y1: number[] = getColumn(gyroMeasurements1, "accY");

          const pitch2: number[] = getColumn(gyroMeasurements2, "pitch");

          const erasePitch1: number = getMean(pitch1.slice(9, 99));
          const pitch1p: number[] = getArraySubtract(pitch1, erasePitch1);

          const eraseY1: number = getMean(y1.slice(9, 99));
          const y1p: number[] = getArraySubtract(y1, eraseY1);

          const atorn: number[] = [];
          for (let index = 0; index < length; index++) {
            atorn.push(90 - yaw1[index] - pitch2[index]);
          }

          const limit9: number = getMax(y1p);
          let yY1p: number[] = getArrayDivision(y1p, 2);
          yY1p = getArrayPow(yY1p as number[], 4) as number[];

          let start = 0;
          let startI = 0;
          let startF = 0;
          for (const [index, temp] of yY1p.entries()) {
            if (temp > limit9) {
              startI = index;
            }
          }

          start = startI - 735;
          startF = startI - 135;

          const rPitch1p: number[] = pitch1p.slice(start, startF);
          const rAtorn: number[] = atorn.slice(start, startF);

          const minPitch: number = getMin(rPitch1p);
          const maxPitch: number = getMax(rPitch1p);
          const varPitch: number = maxPitch - minPitch;

          const minAtorn: number = getMin(rAtorn);
          const maxAtorn: number = getMax(rAtorn);

          const varAtorn: number = maxAtorn - minAtorn;

          const zAtorn: number[] = getArraySubtract(rAtorn, 90);
          const rmsRAtorn: number[] = getArraySqrt(getArrayPow(zAtorn, 2));
          const rPitch1pSqrt = getArraySqrt(getArrayPow(rPitch1p, 2));
          // const rmsRPitch1p: number = getSqrt(rPitch1pSqrt);

          const meanRmsRAtorn: number = getMean(rmsRAtorn);
          const sdRmsRAtorn = getStDeviation(rmsRAtorn);

          // const meanRmsRPitch1p: number = getMean(rmsRPitch1p);
          // const sdRmsRPitch1p: number = getStDeviation(rmsRPitch1p);

          result.atorn = atorn;
          result.values = {
            minPitch,
            maxPitch,
            varPitch,
            minAtorn,
            maxAtorn,
            varAtorn,
            meanRmsRAtorn,
            sdRmsRAtorn,
            meanRmsRPitch1p: 0,
            sdRmsRPitch1p: 0,
          };
          result.chartOption = this.chartOptions(maxAtorn);
        }
      }
    } catch (e) {
      console.log(e);
    }

    return result;
  }

  chartOptions(maxAtorn: number) {
    // reference -> https://echarts.apache.org/
    return {
      yAxis: {
        boundaryGap: [0, "100%"],
        type: "value",
        max: maxAtorn + 0.1 * maxAtorn,
      },
      xAxis: {
        type: "category",
        boundaryGap: false,
        axisLine: { onZero: true },
      },
    };
  }
})();
