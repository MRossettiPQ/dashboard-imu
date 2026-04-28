-- Inserir Procedure Types
INSERT INTO procedure_types (id, type, description)
VALUES (gen_random_uuid(), 'SIMPLE', 'Simples'),
       (gen_random_uuid(), 'SHOULDER', 'Ombro'),
       (gen_random_uuid(), 'ELBOW', 'Cotovelo'),
       (gen_random_uuid(), 'RADIOULNAR', 'Radioulnar'),
       (gen_random_uuid(), 'WRIST', 'Punho'),
       (gen_random_uuid(), 'CARPOMETACARPAL_THUMB', 'Carpometacarpal do polegar'),
       (gen_random_uuid(), 'METACARPOPHALANGEAL', 'Metacarpofalangeana'),
       (gen_random_uuid(), 'PROXIMAL_INTERPHALANGEAL', 'Interfalângica proximal'),
       (gen_random_uuid(), 'DISTAL_INTERPHALANGEAL', 'Interfalângica distal');

-- Inserir Movement Types para cada Procedure Type
DO
$$
    DECLARE
        simple_id                   UUID;
        shoulder_id                 UUID;
        elbow_id                    UUID;
        radioulnar_id               UUID;
        wrist_id                    UUID;
        carpometacarpal_thumb_id    UUID;
        metacarpophalangeal_id      UUID;
        proximal_interphalangeal_id UUID;
        distal_interphalangeal_id   UUID;
    BEGIN
        -- Obter IDs dos procedure types
        SELECT id INTO simple_id FROM procedure_types WHERE type = 'SIMPLE';
        SELECT id INTO shoulder_id FROM procedure_types WHERE type = 'SHOULDER';
        SELECT id INTO elbow_id FROM procedure_types WHERE type = 'ELBOW';
        SELECT id INTO radioulnar_id FROM procedure_types WHERE type = 'RADIOULNAR';
        SELECT id INTO wrist_id FROM procedure_types WHERE type = 'WRIST';
        SELECT id INTO carpometacarpal_thumb_id FROM procedure_types WHERE type = 'CARPOMETACARPAL_THUMB';
        SELECT id INTO metacarpophalangeal_id FROM procedure_types WHERE type = 'METACARPOPHALANGEAL';
        SELECT id INTO proximal_interphalangeal_id FROM procedure_types WHERE type = 'PROXIMAL_INTERPHALANGEAL';
        SELECT id INTO distal_interphalangeal_id FROM procedure_types WHERE type = 'DISTAL_INTERPHALANGEAL';

        -- SIMPLE movement
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'SIMPLE', 'Simples', simple_id, '{
          "min": 0,
          "max": 180
        }'::jsonb, 'shoulder_-_flexion.jpg');

        -- SHOULDER movements
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'FLEXION', 'Flexão do Ombro', shoulder_id, '{
          "min": 0,
          "max": 180
        }'::jsonb, 'shoulder_-_flexion.jpg'),
               (gen_random_uuid(), 'EXTENSION', 'Extensão do Ombro', shoulder_id, '{
                 "min": 0,
                 "max": 45
               }'::jsonb, 'shoulder_-_extension.jpg'),
               (gen_random_uuid(), 'ABDUCTION', 'Abdução do Ombro', shoulder_id, '{
                 "min": 0,
                 "max": 40
               }'::jsonb, 'shoulder_-_abduction.jpg'),
               (gen_random_uuid(), 'ADDUCTION', 'Adução do Ombro', shoulder_id, '{
                 "min": 0,
                 "max": 180
               }'::jsonb, 'shoulder_-_adduction.jpg'),
               (gen_random_uuid(), 'INTERNAL_ROTATION', 'Rotação Interna do Ombro', shoulder_id, '{
                 "min": 0,
                 "max": 90
               }'::jsonb, 'shoulder_-_internal_rotation.jpg'),
               (gen_random_uuid(), 'EXTERNAL_ROTATION', 'Rotação Externa do Ombro', shoulder_id, '{
                 "min": 0,
                 "max": 90
               }'::jsonb, 'shoulder_-_external_rotation.jpg');

        -- ELBOW movements
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'FLEXION', 'Flexão do Cotovelo', elbow_id, '{
          "min": 0,
          "max": 145
        }'::jsonb, 'elbow_-_extension-flexion.jpg'),
               (gen_random_uuid(), 'EXTENSION', 'Extensão do Cotovelo', elbow_id, '{
                 "min": 145,
                 "max": 0
               }'::jsonb, 'elbow_-_extension-flexion.jpg');

        -- RADIOULNAR movements
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'PRONATION', 'Pronação Radioulnar', radioulnar_id, '{
          "min": 0,
          "max": 90
        }'::jsonb, 'radioulnar_-_pronation.jpg'),
               (gen_random_uuid(), 'SUPINATION', 'Supinação Radioulnar', radioulnar_id, '{
                 "min": 0,
                 "max": 90
               }'::jsonb, 'radioulnar_-_supnation.jpg');

        -- WRIST movements
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'FLEXION', 'Flexão do Punho', wrist_id, '{
          "min": 0,
          "max": 90
        }'::jsonb, 'wrist_-_flexion.jpg'),
               (gen_random_uuid(), 'EXTENSION', 'Extensão do Punho', wrist_id, '{
                 "min": 0,
                 "max": 70
               }'::jsonb, 'wrist_-_extension.jpg'),
               (gen_random_uuid(), 'ULNAR_ADDUCTION', 'Adução Ulnar do Punho', wrist_id, '{
                 "min": 0,
                 "max": 45
               }'::jsonb, 'wrist_-_ulnar_adduction.jpg'),
               (gen_random_uuid(), 'RADIAL_ADDUCTION', 'Adução Radial do Punho', wrist_id, '{
                 "min": 0,
                 "max": 20
               }'::jsonb, 'wrist.radial_adduction.jpg');

        -- CARPOMETACARPAL_THUMB movements
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'FLEXION', 'Flexão Carpometacarpal do Polegar', carpometacarpal_thumb_id, '{
          "min": 0,
          "max": 15
        }'::jsonb, 'carpometacarpal_thumb_-_flexion.jpg'),
               (gen_random_uuid(), 'ADDUCTION', 'Adução Carpometacarpal do Polegar', carpometacarpal_thumb_id, '{
                 "min": 0,
                 "max": 70
               }'::jsonb, 'carpometacarpal_thumb_-_abduction.jpg'),
               (gen_random_uuid(), 'EXTENSION', 'Extensão Carpometacarpal do Polegar', carpometacarpal_thumb_id, '{
                 "min": 0,
                 "max": 70
               }'::jsonb, 'carpometacarpal_thumb_-_extension.jpg');

        -- METACARPOPHALANGEAL movements
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'FLEXION', 'Flexão Metacarpofalângica', metacarpophalangeal_id, '{
          "min": 0,
          "max": 90
        }'::jsonb, 'metacarpophalangeal_-_flexion.jpg'),
               (gen_random_uuid(), 'EXTENSION', 'Extensão Metacarpofalângica', metacarpophalangeal_id, '{
                 "min": 0,
                 "max": 30
               }'::jsonb, 'metacarpophalangeal_-_extension.jpg'),
               (gen_random_uuid(), 'ABDUCTION', 'Abdução Metacarpofalângica', metacarpophalangeal_id, '{
                 "min": 0,
                 "max": 20
               }'::jsonb, 'metacarpophalangeal_-_abduction-adduction.jpg'),
               (gen_random_uuid(), 'ADDUCTION', 'Adução Metacarpofalângica', metacarpophalangeal_id, '{
                 "min": 0,
                 "max": 20
               }'::jsonb, 'metacarpophalangeal_-_abduction-adduction.jpg');

        -- PROXIMAL_INTERPHALANGEAL movements
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'FLEXION', 'Flexão Interfalângica Proximal', proximal_interphalangeal_id, '{
          "min": 0,
          "max": 110
        }'::jsonb, 'proximal_interphalangeal_-_flexion.jpg'),
               (gen_random_uuid(), 'EXTENSION', 'Extensão Interfalângica Proximal', proximal_interphalangeal_id, '{
                 "min": 0,
                 "max": 10
               }'::jsonb, 'proximal_interphalangeal_-_extension.jpg');

        -- DISTAL_INTERPHALANGEAL movements
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'FLEXION', 'Flexão Interfalângica Distal', distal_interphalangeal_id, '{
          "min": 0,
          "max": 110
        }'::jsonb, 'proximal_interphalangeal_-_flexion.jpg'),
               (gen_random_uuid(), 'THUMB_INTERNAL_FLEXION', 'Flexão Interna do Polegar Interfalângico Distal', distal_interphalangeal_id, '{
                 "min": 0,
                 "max": 80
               }'::jsonb, 'proximal_interphalangeal_-_flexion.jpg'),
               (gen_random_uuid(), 'THUMB_INTERNAL_EXTENSION', 'Extensão Interna do Polegar Interfalângico Distal', distal_interphalangeal_id, '{
                 "min": 0,
                 "max": 20
               }'::jsonb, 'proximal_interphalangeal_-_flexion.jpg'),
               (gen_random_uuid(), 'INTERNAL_EXTENSION_FINGERS', 'Extensão Interna dos Dedos Interfalângicos Distais', distal_interphalangeal_id, '{
                 "min": 0,
                 "max": 20
               }'::jsonb, 'proximal_interphalangeal_-_flexion.jpg');

    END
$$;