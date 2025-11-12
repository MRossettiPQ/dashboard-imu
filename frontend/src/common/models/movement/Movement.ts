import { BaseModel } from 'src/common/models/models';
import { Sensor } from 'src/common/models/sensor/Sensor';
import { Type } from 'class-transformer';

export enum MovementEnum {
  SIMPLE = 'Simple',
  FLEXION = 'Flexion',
  EXTENSION = 'Extension',
  ADDUCTION = 'Adduction',
  ABDUCTION = 'Abduction',
  INTERNAL_ROTATION = 'Internal Rotation',
  EXTERNAL_ROTATION = 'External Rotation',
  PRONATION = 'Pronation',
  SUPINATION = 'Supination',
  THUMB_INTERNAL_FLEXION = 'Thumb internal flexion',
  THUMB_INTERNAL_EXTENSION = 'Thumb internal extension',
  INTERNAL_EXTENSION_FINGERS = 'Internal extensions fingers',
  ULNAR_ADDUCTION = 'Ulnar adduction',
  RADIAL_ADDUCTION = 'Radial adduction',
}

export class Movement extends BaseModel {
  observation?: string;
  type?: MovementEnum;
  @Type(() => Sensor)
  sensors: Sensor[] = [];
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
