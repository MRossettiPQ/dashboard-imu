import {
  Column,
  DataType,
  Model,
  NotNull,
  BelongsTo,
  Table,
  ForeignKey,
  HasMany,
} from "sequelize-typescript";
import { Optional } from "sequelize";
import Sensor from "./Sensor";
import Session from "../../session/models/Session";

export enum MovementTypeEnum {
  FLEXION = "FLEXION",
  EXTENSION = "EXTENSION",
  ADDUCTION = "ADDUCTION",
  ABDUCTION = "ABDUCTION",
  INTERNAL_ROTATION = "INTERNAL_ROTATION",
  EXTERNAL_ROTATION = "EXTERNAL_ROTATION",
  PRONATION = "PRONATION",
  SUPINATION = "SUPINATION",
  THUMB_INTERNAL_FLEXION = "THUMB_INTERNAL_FLEXION",
  THUMB_INTERNAL_EXTENSION = "THUMB_INTERNAL_EXTENSION",
  INTERNAL_EXTENSION_FINGERS = "INTERNAL_EXTENSION_FINGERS",
  ULNAR_ADDUCTION = "ULNAR_ADDUCTION",
  RADIAL_ADDUCTION = "RADIAL_ADDUCTION",
}

export interface MovementAttributes {
  id: number;
  type: MovementTypeEnum;
  observation: string;
  sessionId: number; // FK
}

export type MovementCreationAttributes = Optional<MovementAttributes, "id">;

@Table({ tableName: "movements", underscored: true })
export default class Movement extends Model<
  MovementAttributes,
  MovementCreationAttributes
> {
  @NotNull
  @Column({ type: DataType.UUIDV4, allowNull: false, defaultValue: DataType.UUIDV4 })
  public declare uuid: string;

  @NotNull
  @Column({
    type: DataType.ENUM(
      MovementTypeEnum.FLEXION,
      MovementTypeEnum.EXTENSION,
      MovementTypeEnum.ADDUCTION,
      MovementTypeEnum.ABDUCTION,
      MovementTypeEnum.INTERNAL_ROTATION,
      MovementTypeEnum.EXTERNAL_ROTATION,
      MovementTypeEnum.PRONATION,
      MovementTypeEnum.SUPINATION,
      MovementTypeEnum.THUMB_INTERNAL_FLEXION,
      MovementTypeEnum.THUMB_INTERNAL_EXTENSION,
      MovementTypeEnum.INTERNAL_EXTENSION_FINGERS,
      MovementTypeEnum.ULNAR_ADDUCTION,
      MovementTypeEnum.RADIAL_ADDUCTION,
    ),
    allowNull: false,
  })
  public declare type: MovementTypeEnum;

  @Column(DataType.STRING)
  public declare observation: string;

  @ForeignKey(() => Session)
  @Column(DataType.INTEGER)
  public declare sessionId: number;

  @BelongsTo(() => Session, { foreignKey: "sessionId" })
  public declare session: Session;

  @HasMany(() => Sensor, {
    onDelete: "CASCADE",
  })
  public declare sensors: Sensor[];
}
