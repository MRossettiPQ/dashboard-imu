import {
  BelongsTo,
  Column,
  DataType,
  ForeignKey,
  Model,
  NotNull,
  Table,
} from "sequelize-typescript";
import { Optional } from "sequelize";
import Sensor from "./Sensor";

export interface GyroMeasurementAttributes {
  id: number;
  sensorName: string;
  numberMensuration: number;
  hourMensuration: Date;
  accX: number;
  accY: number;
  accZ: number;
  accelXMss: number;
  accelYMss: number;
  accelZMss: number;
  gyrX: number;
  gyrY: number;
  gyrZ: number;
  magX: number;
  magY: number;
  magZ: number;
  roll: number;
  pitch: number;
  yaw: number;
  eulerX: number;
  eulerY: number;
  eulerZ: number;
  quaternionX: number;
  quaternionY: number;
  quaternionZ: number;
  quaternionW: number;
  sensorId: number;
  sensor: Sensor;
}

export type GyroMeasurementCreationAttributes = Optional<GyroMeasurementAttributes, "id">;

@Table({ tableName: "gyro_measurements", underscored: true })
export default class GyroMeasurement extends Model<
  GyroMeasurementAttributes,
  GyroMeasurementCreationAttributes
> {
  @NotNull
  @Column({ type: DataType.UUIDV4, allowNull: false, defaultValue: DataType.UUIDV4 })
  public declare uuid: string;

  @Column(DataType.STRING)
  public declare sensorName: string;

  @NotNull
  @Column({
    type: DataType.INTEGER,
    allowNull: false,
    field: "number_mensuration", // especificando o nome do campo
  })
  public declare numberMensuration: number;

  @Column(DataType.DATE)
  public declare hourMensuration: Date;

  @Column(DataType.DECIMAL)
  public declare accX: number;

  @Column(DataType.DECIMAL)
  public declare accY: number;

  @Column(DataType.DECIMAL)
  public declare accZ: number;

  @Column(DataType.DECIMAL)
  public declare accelXMss: number;

  @Column(DataType.DECIMAL)
  public declare accelYMss: number;

  @Column(DataType.DECIMAL)
  public declare accelZMss: number;

  @Column(DataType.DECIMAL)
  public declare gyrX: number;

  @Column(DataType.DECIMAL)
  public declare gyrY: number;

  @Column(DataType.DECIMAL)
  public declare gyrZ: number;

  @Column(DataType.DECIMAL)
  public declare magX: number;

  @Column(DataType.DECIMAL)
  public declare magY: number;

  @Column(DataType.DECIMAL)
  public declare magZ: number;

  @Column(DataType.DECIMAL)
  public declare roll: number;

  @Column(DataType.DECIMAL)
  public declare pitch: number;

  @Column(DataType.DECIMAL)
  public declare yaw: number;

  @Column(DataType.DECIMAL)
  public declare eulerX: number;

  @Column(DataType.DECIMAL)
  public declare eulerY: number;

  @Column(DataType.DECIMAL)
  public declare eulerZ: number;

  @Column(DataType.DECIMAL)
  public declare quaternionX: number;

  @Column(DataType.DECIMAL)
  public declare quaternionY: number;

  @Column(DataType.DECIMAL)
  public declare quaternionZ: number;

  @Column(DataType.DECIMAL)
  public declare quaternionW: number;

  // Sensor
  @ForeignKey(() => Sensor)
  @Column(DataType.INTEGER)
  public declare sensorId: number;

  @BelongsTo(() => Sensor, { foreignKey: "sensorId" })
  public declare sensor: Sensor;
}
