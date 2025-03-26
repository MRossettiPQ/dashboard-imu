import { Column, DataType, HasMany, Model, NotNull, Table } from "sequelize-typescript";
import { Optional } from "sequelize";
import Session from "../../session/models/Session";

export enum UserRole {
  ADMINISTRATOR = "ADMINISTRATOR",
  PHYSIOTHERAPIST = "PHYSIOTHERAPIST",
  PATIENT = "PATIENT",
}

export interface UserAttributes {
  id: number;
  uuid: string;
  username: string;
  name: string;
  email: string;
  password: string;
  role: UserRole;
  sessions: Session[];
}

export type UserCreationAttributes = Optional<UserAttributes, "id">;

@Table({ tableName: "users", underscored: true })
export default class User extends Model<UserAttributes, UserCreationAttributes> {
  @NotNull
  @Column({ type: DataType.UUIDV4, allowNull: false, defaultValue: DataType.UUIDV4 })
  public declare uuid: string;

  @NotNull
  @Column({ type: DataType.STRING, allowNull: false })
  public declare username: string;

  @Column(DataType.STRING)
  public declare name: string;

  @NotNull
  @Column({ type: DataType.STRING, allowNull: false })
  public declare email: string;

  @Column(DataType.STRING)
  public declare password: string;

  @Column({
    type: DataType.ENUM(
      UserRole.ADMINISTRATOR,
      UserRole.PHYSIOTHERAPIST,
      UserRole.PATIENT,
    ),
    defaultValue: UserRole.PHYSIOTHERAPIST,
  })
  public declare role: UserRole;

  @HasMany(() => Session, {
    onDelete: "CASCADE",
  })
  public declare sessions: Session[];
}
