import { BaseModel } from 'src/common/models/models';
import { Movement, MovementType } from 'src/common/models/movement/Movement';
import { Type } from 'class-transformer';

export enum ProcedureEnum {
  SIMPLE = 'Simples',
  SHOULDER = 'Ombro',
  ELBOW = 'Cotovelo',
  RADIOULNAR = 'Radioulnar',
  WRIST = 'Punho',
  CARPOMETACARPAL_THUMB = 'Carpometacarpal do polegar',
  METACARPOPHALANGEAL = 'Metacarpofalangeana',
  PROXIMAL_INTERPHALANGEAL = 'Interfalângica proximal',
  DISTAL_INTERPHALANGEAL = 'Interfalângica distal',
}

export function findProcedureEnum(strEn: string | ProcedureEnum): string | undefined {
  return ProcedureEnum[strEn as keyof typeof ProcedureEnum];
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
