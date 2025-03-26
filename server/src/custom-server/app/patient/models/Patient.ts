import { Column, DataType, HasMany, Model, NotNull, Table } from "sequelize-typescript";
import { Optional } from "sequelize";
import Session from "../../session/models/Session";

export interface PatientAttributes {
  id: number;
  name: string;
  cpf: string;
  email: string;
  phone: string;
  birthday: string;
  stature: number;
  sessions: Session[];
}

export type PatientCreationAttributes = Optional<PatientAttributes, "id">;

@Table({ tableName: "patients", underscored: true })
export default class Patient extends Model<PatientAttributes, PatientCreationAttributes> {
  @NotNull
  @Column({ type: DataType.UUIDV4, allowNull: false, defaultValue: DataType.UUIDV4 })
  public declare uuid: string;

  @NotNull
  @Column({ type: DataType.STRING, allowNull: false })
  public declare name: string;

  @NotNull
  @Column({ type: DataType.STRING, allowNull: false })
  public declare cpf: string;

  @NotNull
  @Column({ type: DataType.STRING, allowNull: false })
  public declare email: string;

  @Column(DataType.STRING)
  public declare phone: string;

  @Column(DataType.DATE)
  public declare birthday: Date;

  @Column(DataType.DECIMAL)
  public declare stature: number;

  @HasMany(() => Session, {
    onDelete: "CASCADE",
  })
  public declare sessions: Session[];
}
