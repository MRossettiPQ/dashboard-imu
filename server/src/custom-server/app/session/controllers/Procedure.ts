import { ProcedureType } from "../models/Session";
import { PositionEnum } from "../models/Sensor";
import { MovementTypeEnum } from "../models/Movement";

interface Angle {
  min: number;
  max: number;
}

interface MovementType {
  movement_name: string;
  value: MovementTypeEnum;
  description: string;
  image: string;
  angle: Angle;
}

interface SensorPosition {
  label: string;
  value: PositionEnum;
}

interface ProcedureVo {
  articulation_name: string;
  value: ProcedureType;
  min_sensor: number;
  sensor_positions: SensorPosition[];
  rules: MovementType[];
}

export default {
  list(): ProcedureVo[] {
    return [
      {
        articulation_name: "Ombro",
        value: ProcedureType.SHOULDER,
        min_sensor: 2,
        sensor_positions: [
          {
            label: "ONE",
            value: PositionEnum.ONE,
          },
          {
            label: "TWO",
            value: PositionEnum.TWO,
          },
        ],
        rules: [
          {
            movement_name: "Flexão",
            value: MovementTypeEnum.FLEXION,
            description: "Movimento de flexão do braço (0 a 180°)",
            image: "shoulder_-_flexion.jpg",
            angle: {
              min: 0,
              max: 180,
            },
          },
          {
            movement_name: "Extensão",
            value: MovementTypeEnum.EXTENSION,
            description: "Movimento de extensão do braço (0 a 180°)",
            image: "shoulder_-_extension.jpg",
            angle: {
              min: 0,
              max: 45,
            },
          },
          {
            movement_name: "Abdução",
            value: MovementTypeEnum.ADDUCTION,
            description: "Movimento de abdução do braço (0 a 180°)",
            image: "shoulder_-_abduction.jpg",
            angle: {
              min: 0,
              max: 40,
            },
          },
          {
            movement_name: "Adução",
            value: MovementTypeEnum.ADDUCTION,
            description: "Movimento de abdução do braço (0 a 40°)",
            image: "shoulder_-_adduction.jpg",
            angle: {
              min: 0,
              max: 180,
            },
          },
          {
            movement_name: "Rotação interna",
            value: MovementTypeEnum.INTERNAL_ROTATION,
            description: "Movimento de rotação medial do braço (0 a 90º)",
            image: "shoulder_-_internal_rotation.jpg",
            angle: {
              min: 0,
              max: 90,
            },
          },
          {
            movement_name: "Rotação externa",
            value: MovementTypeEnum.EXTERNAL_ROTATION,
            description: "Movimento de rotação lateral do braço (0 a 90º)",
            image: "shoulder_-_external_rotation.jpg",
            angle: {
              min: 0,
              max: 90,
            },
          },
        ],
      },
      {
        articulation_name: "Cotovelo",
        value: ProcedureType.ELBOW,
        min_sensor: 2,
        sensor_positions: [
          {
            label: "ONE",
            value: PositionEnum.ONE,
          },
          {
            label: "TWO",
            value: PositionEnum.TWO,
          },
        ],
        rules: [
          {
            movement_name: "Flexão",
            value: MovementTypeEnum.FLEXION,
            description: "Movimento de flexão do antebraço (0 a 145º)",
            image: "elbow_-_extension-flexion.jpg",
            angle: {
              min: 0,
              max: 145,
            },
          },
          {
            movement_name: "Extensão",
            value: MovementTypeEnum.EXTENSION,
            description: "Movimento de extensão do antebraço (0 a 145º)",
            image: "elbow_-_extension-flexion.jpg",
            angle: {
              min: 145,
              max: 0,
            },
          },
        ],
      },
      {
        articulation_name: "Radioulnar",
        value: ProcedureType.RADIOULNAR,
        min_sensor: 2,
        sensor_positions: [
          {
            label: "ONE",
            value: PositionEnum.ONE,
          },
          {
            label: "TWO",
            value: PositionEnum.TWO,
          },
        ],
        rules: [
          {
            movement_name: "Pronação",
            value: MovementTypeEnum.PRONATION,
            description: "Movimento de pronação do antebraço (0 a 90º)",
            image: "radioulnar_-_pronation.jpg",
            angle: {
              min: 0,
              max: 90,
            },
          },
          {
            movement_name: "Supinação",
            value: MovementTypeEnum.SUPINATION,
            description: "Movimento de supinação do antebraço (0 a 90º)",
            image: "radioulnar_-_supnation.jpg",
            angle: {
              min: 0,
              max: 90,
            },
          },
        ],
      },
      {
        articulation_name: "Punho",
        value: ProcedureType.WRIST,
        min_sensor: 2,
        sensor_positions: [
          {
            label: "ONE",
            value: PositionEnum.ONE,
          },
          {
            label: "TWO",
            value: PositionEnum.TWO,
          },
        ],
        rules: [
          {
            movement_name: "Flexão",
            value: MovementTypeEnum.FLEXION,
            description: "Movimento de flexão do punho (0 a 90º)",
            image: "wrist_-_flexion.jpg",
            angle: {
              min: 0,
              max: 90,
            },
          },
          {
            movement_name: "Extensão",
            value: MovementTypeEnum.EXTENSION,
            description: "Movimento de extensão do punho (0 a 70º)",
            image: "wrist_-_extension.jpg",
            angle: {
              min: 0,
              max: 70,
            },
          },
          {
            movement_name: "Adução (desvio ulnar)",
            value: MovementTypeEnum.ULNAR_ADDUCTION,
            description: "Movimento de abdução da mão ou desvio radial (0 a 20º)",
            image: "wrist_-_ulnar_adduction.jpg",
            angle: {
              min: 0,
              max: 45,
            },
          },
          {
            movement_name: "Adução (desvio radial)",
            value: MovementTypeEnum.RADIAL_ADDUCTION,
            description: "Movimento de adução da mão ou desvio ulnar (0 a 45º)",
            image: "wrist.radial_adduction.jpg",
            angle: {
              min: 0,
              max: 20,
            },
          },
        ],
      },
      {
        articulation_name: "Carpometacárpica do polegar",
        value: ProcedureType.CARPOMETACARPAL_THUMB,
        min_sensor: 2,
        sensor_positions: [
          {
            label: "ONE",
            value: PositionEnum.ONE,
          },
          {
            label: "TWO",
            value: PositionEnum.TWO,
          },
        ],
        rules: [
          {
            movement_name: "Flexão",
            value: MovementTypeEnum.FLEXION,
            description: "Movimento de flexão (0 a 15º)",
            image: "carpometacarpal_thumb_-_flexion.jpg",
            angle: {
              min: 0,
              max: 15,
            },
          },
          {
            movement_name: "Abdução",
            value: MovementTypeEnum.ADDUCTION,
            description: "Movimento de abdução (0 a 70º)",
            image: "carpometacarpal_thumb_-_abduction.jpg",
            angle: {
              min: 0,
              max: 70,
            },
          },
          {
            movement_name: "Extensão",
            value: MovementTypeEnum.EXTENSION,
            description: "Movimento de extensão (0 a 70º)",
            image: "carpometacarpal_thumb_-_extension.jpg",
            angle: {
              min: 0,
              max: 70,
            },
          },
        ],
      },
      {
        articulation_name: "Metacarpofalangianas",
        value: ProcedureType.METACARPOPHALANGEAL,
        min_sensor: 2,
        sensor_positions: [
          {
            label: "ONE",
            value: PositionEnum.ONE,
          },
          {
            label: "TWO",
            value: PositionEnum.TWO,
          },
        ],
        rules: [
          {
            movement_name: "Flexão",
            value: MovementTypeEnum.FLEXION,
            description: "Movimento de flexão dos dedos (0 a 90º)",
            image: "metacarpophalangeal_-_flexion.jpg",
            angle: {
              min: 0,
              max: 90,
            },
          },
          {
            movement_name: "Extensão",
            value: MovementTypeEnum.EXTENSION,
            description: "Movimento de extensão dos dedos (0 a 30º)",
            image: "metacarpophalangeal_-_extension.jpg",
            angle: {
              min: 0,
              max: 30,
            },
          },
          {
            movement_name: "Abdução",
            value: MovementTypeEnum.ABDUCTION,
            description: "Movimento de abdução e adução dos dedos (0 a 20º)",
            image: "metacarpophalangeal_-_abduction-adduction.jpg",
            angle: {
              min: 0,
              max: 20,
            },
          },
          {
            movement_name: "Adução",
            value: MovementTypeEnum.ADDUCTION,
            description: "Movimento de abdução e adução dos dedos (0 a 20º)",
            image: "metacarpophalangeal_-_abduction-adduction.jpg",
            angle: {
              min: 0,
              max: 20,
            },
          },
        ],
      },
      {
        articulation_name: "Interfalângicas proximais",
        value: ProcedureType.PROXIMAL_INTERPHALANGEAL,
        min_sensor: 2,
        sensor_positions: [
          {
            label: "ONE",
            value: PositionEnum.ONE,
          },
          {
            label: "TWO",
            value: PositionEnum.TWO,
          },
        ],
        rules: [
          {
            movement_name: "Flexão",
            value: MovementTypeEnum.FLEXION,
            description: "Movimento de flexão (0 a 110º)",
            image: "proximal_interphalangeal_-_flexion.jpg",
            angle: {
              min: 0,
              max: 110,
            },
          },
          {
            movement_name: "Extensão",
            value: MovementTypeEnum.EXTENSION,
            description: "Movimento de extensão (0 a 10º)",
            image: "proximal_interphalangeal_-_extension.jpg",
            angle: {
              min: 0,
              max: 10,
            },
          },
        ],
      },
      {
        articulation_name: "Interfalângicas distais",
        value: ProcedureType.DISTAL_INTERPHALANGEAL,
        min_sensor: 2,
        sensor_positions: [
          {
            label: "ONE",
            value: PositionEnum.ONE,
          },
          {
            label: "TWO",
            value: PositionEnum.TWO,
          },
        ],
        rules: [
          {
            movement_name: "Flexão",
            value: MovementTypeEnum.FLEXION,
            description: "Movimento de flexão (0 a 90º)",
            image: "proximal_interphalangeal_-_flexion.jpg",
            angle: {
              min: 0,
              max: 110,
            },
          },
          {
            movement_name: "Flexão interna do polegar",
            value: MovementTypeEnum.THUMB_INTERNAL_FLEXION,
            description: "(0 a 80º)",
            image: "proximal_interphalangeal_-_flexion.jpg",
            angle: {
              min: 0,
              max: 80,
            },
          },
          {
            movement_name: "Extensão interna do polegar",
            value: MovementTypeEnum.THUMB_INTERNAL_EXTENSION,
            description: "(0 a 20º)",
            image: "proximal_interphalangeal_-_flexion.jpg",
            angle: {
              min: 0,
              max: 20,
            },
          },
          {
            movement_name: "Extensão interna do polegar",
            value: MovementTypeEnum.INTERNAL_EXTENSION_FINGERS,
            description: "(0 a 20º)",
            image: "proximal_interphalangeal_-_flexion.jpg",
            angle: {
              min: 0,
              max: 20,
            },
          },
        ],
      },
    ];
  },
};
