import {
  BelongsTo,
  Column,
  DataType,
  ForeignKey,
  HasMany,
  Model,
  NotNull,
  Table,
} from "sequelize-typescript";
import { Optional } from "sequelize";
import Movement from "./Movement";
import GyroMeasurement from "./GyroMeasurement";

export enum PositionEnum {
  ONE = "ONE",
  TWO = "TWO",
}

export enum SensorTypeEnum {
  GYROSCOPE = "GYROSCOPE",
  HUMIDITY = "HUMIDITY",
}

export interface SensorAttributes {
  id: number;
  sensorName: string;
  position: PositionEnum;
  type: SensorTypeEnum;
  observation: string;
  movementId: number; // FK
}

export type SensorCreationAttributes = Optional<SensorAttributes, "id">;

@Table({ tableName: "sensors", underscored: true })
export default class Sensor extends Model<SensorAttributes, SensorCreationAttributes> {
  @NotNull
  @Column({ type: DataType.UUIDV4, allowNull: false, defaultValue: DataType.UUIDV4 })
  public declare uuid: string;

  @NotNull
  @Column({ type: DataType.STRING, allowNull: false })
  public declare sensorName: string;

  @NotNull
  @Column({
    type: DataType.ENUM(PositionEnum.ONE, PositionEnum.TWO),
    allowNull: false,
  })
  public declare position: PositionEnum;

  @NotNull
  @Column({
    type: DataType.ENUM(SensorTypeEnum.GYROSCOPE, SensorTypeEnum.HUMIDITY),
    allowNull: false,
    defaultValue: SensorTypeEnum.GYROSCOPE,
  })
  public declare type: SensorTypeEnum;

  @Column(DataType.STRING)
  public declare observation: string;

  @HasMany(() => GyroMeasurement, {
    onDelete: "CASCADE",
  })
  public declare gyro_measurements: GyroMeasurement[];

  // Movimento
  @ForeignKey(() => Movement)
  @Column(DataType.INTEGER)
  public declare movementId: number;

  @BelongsTo(() => Movement, { foreignKey: "movementId" })
  public declare patient: Movement;
}
