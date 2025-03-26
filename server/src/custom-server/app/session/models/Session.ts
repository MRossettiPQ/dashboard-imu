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
import User from "../../user/models/User";
import Patient from "../../patient/models/Patient";
import Movement from "./Movement";

export enum SessionType {
  REAL = "REAL",
}

export enum ProcedureType {
  SHOULDER = "SHOULDER",
  ELBOW = "ELBOW",
  RADIOULNAR = "RADIOULNAR",
  WRIST = "WRIST",
  CARPOMETACARPAL_THUMB = "CARPOMETACARPAL_THUMB",
  METACARPOPHALANGEAL = "METACARPOPHALANGEAL",
  PROXIMAL_INTERPHALANGEAL = "PROXIMAL_INTERPHALANGEAL",
  DISTAL_INTERPHALANGEAL = "DISTAL_INTERPHALANGEAL",
}

export interface SessionAttributes {
  id: number;
  date: string;
  patientId: number; // FK
  userId: number; // FK
}

export type SessionCreationAttributes = Optional<SessionAttributes, "id">;

@Table({ tableName: "sessions", underscored: true })
export default class Session extends Model<SessionAttributes, SessionCreationAttributes> {
  @NotNull
  @Column({ type: DataType.UUIDV4, allowNull: false, defaultValue: DataType.UUIDV4 })
  public declare uuid: string;

  @Column(DataType.DATE)
  public declare date: Date;

  @Column(DataType.ENUM(SessionType.REAL))
  public declare type: SessionType;

  @Column(
    DataType.ENUM(
      ProcedureType.SHOULDER,
      ProcedureType.ELBOW,
      ProcedureType.RADIOULNAR,
      ProcedureType.WRIST,
      ProcedureType.CARPOMETACARPAL_THUMB,
      ProcedureType.METACARPOPHALANGEAL,
      ProcedureType.PROXIMAL_INTERPHALANGEAL,
      ProcedureType.DISTAL_INTERPHALANGEAL,
    ),
  )
  public declare procedure: ProcedureType;

  @Column(DataType.STRING)
  public declare observation: string;

  // Usuario
  @ForeignKey(() => User)
  @Column(DataType.INTEGER)
  public declare userId: number;

  @BelongsTo(() => User, { foreignKey: "userId" })
  public declare user: User;

  // Movimentos
  @HasMany(() => Movement, {
    onDelete: "CASCADE",
  })
  public declare movements: Movement[];

  // Paciente
  @ForeignKey(() => Patient)
  @Column(DataType.INTEGER)
  public declare patientId: number;

  @BelongsTo(() => Patient, { foreignKey: "patientId" })
  public declare patient: Patient;
}
