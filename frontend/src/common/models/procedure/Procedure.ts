import { BaseModel } from 'src/common/models/models';
import { Movement, MovementType } from 'src/common/models/movement/Movement';
import { Type } from 'class-transformer';

export enum ProcedureEnum {
  SIMPLE = 'Simple',
  SHOULDER = 'Shoulder',
  ELBOW = 'Elbow',
  RADIOULNAR = 'Radioulnar',
  WRIST = 'Wrist',
  CARPOMETACARPAL_THUMB = 'Carpometacarpal thumb',
  METACARPOPHALANGEAL = 'Metacarpalangeal',
  PROXIMAL_INTERPHALANGEAL = 'Proximal Interpretalangeal',
  DISTAL_INTERPHALANGEAL = 'Distal Interpretalangeal',
}

export class Procedure extends BaseModel {
  type?: ProcedureEnum;
  description?: string;

  @Type(() => Movement)
  movements: Movement[] = [];
}

export class ProcedureType extends BaseModel {
  type?: ProcedureEnum;
  description?: string;

  @Type(() => Movement)
  movementsTypes: MovementType[] = [];
}
