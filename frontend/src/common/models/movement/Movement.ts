import { BaseModel } from 'src/common/models/models';
import { Sensor } from 'src/common/models/sensor/Sensor';
import { Type } from 'class-transformer';

export enum MovementEnum {
  SIMPLE = 'Simples',
  FLEXION = 'Flexão',
  EXTENSION = 'Extensão',
  ADDUCTION = 'Adução',
  ABDUCTION = 'Abdução',
  INTERNAL_ROTATION = 'Rotação interna',
  EXTERNAL_ROTATION = 'Rotação externa',
  PRONATION = 'Pronação',
  SUPINATION = 'Supinação',
  THUMB_INTERNAL_FLEXION = 'Flexão interna do polegar',
  THUMB_INTERNAL_EXTENSION = 'Extensão interna do polegar',
  INTERNAL_EXTENSION_FINGERS = 'Extensões internas dos dedos',
  ULNAR_ADDUCTION = 'Adução ulnar',
  RADIAL_ADDUCTION = 'Adução radial',
}

export function findMovementEnum(strEn: string | MovementEnum): string | undefined {
  return MovementEnum[strEn as keyof typeof MovementEnum];
}

function generateInternalId(): string {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return `movement_${crypto.randomUUID()}`;
  }
  return `movement_${Date.now()}_${Math.random().toString(36).slice(2, 11)}`;
}
export class Movement extends BaseModel {
  observation?: string;
  sessionIdentifier?: string;
  type?: MovementEnum;
  @Type(() => Sensor)
  sensors: Sensor[] = [];

  constructor() {
    super();
    this.sessionIdentifier = generateInternalId();
  }
}

export class AngleRule {
  min?: number;
  max?: number;
}

export class MovementType extends BaseModel {
  angleRule?: AngleRule;
  imageName?: string;
  type?: MovementEnum;
  description?: string;
}
