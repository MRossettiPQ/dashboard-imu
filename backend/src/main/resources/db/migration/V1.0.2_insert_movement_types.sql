-- Inserir Procedure Types
INSERT INTO procedure_types (id, type, description)
VALUES (gen_random_uuid(), 'SIMPLE', 'Simple'),
       (gen_random_uuid(), 'SHOULDER', 'Shoulder'),
       (gen_random_uuid(), 'ELBOW', 'Elbow'),
       (gen_random_uuid(), 'RADIOULNAR', 'Radioulnar'),
       (gen_random_uuid(), 'WRIST', 'Wrist'),
       (gen_random_uuid(), 'CARPOMETACARPAL_THUMB', 'Carpometacarpal thumb'),
       (gen_random_uuid(), 'METACARPOPHALANGEAL', 'Metacarpalangeal'),
       (gen_random_uuid(), 'PROXIMAL_INTERPHALANGEAL', 'Proximal Interpretalangeal'),
       (gen_random_uuid(), 'DISTAL_INTERPHALANGEAL', 'Distal Interpretalangeal');

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
        VALUES (gen_random_uuid(), 'SIMPLE', 'Sample', simple_id, '{
          "min": 0,
          "max": 180
        }'::jsonb, 'shoulder_-_flexion.jpg');

        -- SHOULDER movements
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'FLEXION', 'Shoulder Flexion movement', shoulder_id, '{
          "min": 0,
          "max": 180
        }'::jsonb, 'shoulder_-_flexion.jpg'),
               (gen_random_uuid(), 'EXTENSION', 'Shoulder Extension movement', shoulder_id, '{
                 "min": 0,
                 "max": 45
               }'::jsonb, 'shoulder_-_extension.jpg'),
               (gen_random_uuid(), 'ABDUCTION', 'Shoulder Abduction movement', shoulder_id, '{
                 "min": 0,
                 "max": 40
               }'::jsonb, 'shoulder_-_abduction.jpg'),
               (gen_random_uuid(), 'ADDUCTION', 'Shoulder Adduction movement', shoulder_id, '{
                 "min": 0,
                 "max": 180
               }'::jsonb, 'shoulder_-_adduction.jpg'),
               (gen_random_uuid(), 'INTERNAL_ROTATION', 'Shoulder Internal Rotation movement', shoulder_id, '{
                 "min": 0,
                 "max": 90
               }'::jsonb, 'shoulder_-_internal_rotation.jpg'),
               (gen_random_uuid(), 'EXTERNAL_ROTATION', 'Shoulder External Rotation movement', shoulder_id, '{
                 "min": 0,
                 "max": 90
               }'::jsonb, 'shoulder_-_external_rotation.jpg');

        -- ELBOW movements
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'FLEXION', 'Elbow Flexion movement', elbow_id, '{
          "min": 0,
          "max": 145
        }'::jsonb, 'elbow_-_extension-flexion.jpg'),
               (gen_random_uuid(), 'EXTENSION', 'Elbow Extension movement', elbow_id, '{
                 "min": 145,
                 "max": 0
               }'::jsonb, 'elbow_-_extension-flexion.jpg');

        -- RADIOULNAR movements
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'PRONATION', 'Radioulnar Pronation movement', radioulnar_id, '{
          "min": 0,
          "max": 90
        }'::jsonb, 'radioulnar_-_pronation.jpg'),
               (gen_random_uuid(), 'SUPINATION', 'Radioulnar Supination movement', radioulnar_id, '{
                 "min": 0,
                 "max": 90
               }'::jsonb, 'radioulnar_-_supnation.jpg');

        -- WRIST movements
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'FLEXION', 'Wrist Flexion movement', wrist_id, '{
          "min": 0,
          "max": 90
        }'::jsonb, 'wrist_-_flexion.jpg'),
               (gen_random_uuid(), 'EXTENSION', 'Wrist Extension movement', wrist_id, '{
                 "min": 0,
                 "max": 70
               }'::jsonb, 'wrist_-_extension.jpg'),
               (gen_random_uuid(), 'ULNAR_ADDUCTION', 'Wrist Ulnar Adduction movement', wrist_id, '{
                 "min": 0,
                 "max": 45
               }'::jsonb, 'wrist_-_ulnar_adduction.jpg'),
               (gen_random_uuid(), 'RADIAL_ADDUCTION', 'Wrist Radial Adduction movement', wrist_id, '{
                 "min": 0,
                 "max": 20
               }'::jsonb, 'wrist.radial_adduction.jpg');

        -- CARPOMETACARPAL_THUMB movements
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'FLEXION', 'Carpometacarpal thumb Flexion movement', carpometacarpal_thumb_id, '{
          "min": 0,
          "max": 15
        }'::jsonb, 'carpometacarpal_thumb_-_flexion.jpg'),
               (gen_random_uuid(), 'ADDUCTION', 'Carpometacarpal thumb Adduction movement', carpometacarpal_thumb_id, '{
                 "min": 0,
                 "max": 70
               }'::jsonb, 'carpometacarpal_thumb_-_abduction.jpg'),
               (gen_random_uuid(), 'EXTENSION', 'Carpometacarpal thumb Extension movement', carpometacarpal_thumb_id, '{
                 "min": 0,
                 "max": 70
               }'::jsonb, 'carpometacarpal_thumb_-_extension.jpg');

        -- METACARPOPHALANGEAL movements
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'FLEXION', 'Metacarpophalangeal Flexion movement', metacarpophalangeal_id, '{
          "min": 0,
          "max": 90
        }'::jsonb, 'metacarpophalangeal_-_flexion.jpg'),
               (gen_random_uuid(), 'EXTENSION', 'Metacarpophalangeal Extension movement', metacarpophalangeal_id, '{
                 "min": 0,
                 "max": 30
               }'::jsonb, 'metacarpophalangeal_-_extension.jpg'),
               (gen_random_uuid(), 'ABDUCTION', 'Metacarpophalangeal Abduction movement', metacarpophalangeal_id, '{
                 "min": 0,
                 "max": 20
               }'::jsonb, 'metacarpophalangeal_-_abduction-adduction.jpg'),
               (gen_random_uuid(), 'ADDUCTION', 'Metacarpophalangeal Adduction movement', metacarpophalangeal_id, '{
                 "min": 0,
                 "max": 20
               }'::jsonb, 'metacarpophalangeal_-_abduction-adduction.jpg');

        -- PROXIMAL_INTERPHALANGEAL movements
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'FLEXION', 'Proximal interphalangeal Flexion movement', proximal_interphalangeal_id, '{
          "min": 0,
          "max": 110
        }'::jsonb, 'proximal_interphalangeal_-_flexion.jpg'),
               (gen_random_uuid(), 'EXTENSION', 'Proximal interphalangeal Extension movement', proximal_interphalangeal_id, '{
                 "min": 0,
                 "max": 10
               }'::jsonb, 'proximal_interphalangeal_-_extension.jpg');

        -- DISTAL_INTERPHALANGEAL movements
        INSERT INTO movement_types (id, type, description, procedure_type_id, angle_rule, image_name)
        VALUES (gen_random_uuid(), 'FLEXION', 'Distal interphalangeal Flexion movement', distal_interphalangeal_id, '{
          "min": 0,
          "max": 110
        }'::jsonb, 'proximal_interphalangeal_-_flexion.jpg'),
               (gen_random_uuid(), 'THUMB_INTERNAL_FLEXION', 'Distal interphalangeal Thumb internal flexion movement', distal_interphalangeal_id, '{
                 "min": 0,
                 "max": 80
               }'::jsonb, 'proximal_interphalangeal_-_flexion.jpg'),
               (gen_random_uuid(), 'THUMB_INTERNAL_EXTENSION', 'Distal interphalangeal Thumb internal extension movement', distal_interphalangeal_id, '{
                 "min": 0,
                 "max": 20
               }'::jsonb, 'proximal_interphalangeal_-_flexion.jpg'),
               (gen_random_uuid(), 'INTERNAL_EXTENSION_FINGERS', 'Distal interphalangeal Internal extensions fingers movement', distal_interphalangeal_id, '{
                 "min": 0,
                 "max": 20
               }'::jsonb, 'proximal_interphalangeal_-_flexion.jpg');

    END
$$;