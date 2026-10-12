INSERT INTO roles (name)
SELECT 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

INSERT INTO roles (name)
SELECT 'BARBER' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'BARBER');

INSERT INTO roles (name)
SELECT 'CLIENT' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'CLIENT');

INSERT INTO appointment_statuses (name)
SELECT 'PENDIENTE' WHERE NOT EXISTS (SELECT 1 FROM appointment_statuses WHERE name = 'PENDIENTE');

INSERT INTO appointment_statuses (name)
SELECT 'CONFIRMADA' WHERE NOT EXISTS (SELECT 1 FROM appointment_statuses WHERE name = 'CONFIRMADA');

INSERT INTO appointment_statuses (name)
SELECT 'EN_CURSO' WHERE NOT EXISTS (SELECT 1 FROM appointment_statuses WHERE name = 'EN_CURSO');

INSERT INTO appointment_statuses (name)
SELECT 'FINALIZADA' WHERE NOT EXISTS (SELECT 1 FROM appointment_statuses WHERE name = 'FINALIZADA');

INSERT INTO appointment_statuses (name)
SELECT 'CANCELADA' WHERE NOT EXISTS (SELECT 1 FROM appointment_statuses WHERE name = 'CANCELADA');

INSERT INTO `services` (name, description, price, duration_minutes, is_active, created_at)
SELECT 'Fade clásico', 'Degradado a navaja, precisión en cada detalle.', 25000, 45, TRUE, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM `services` WHERE name = 'Fade clásico');

INSERT INTO `services` (name, description, price, duration_minutes, is_active, created_at)
SELECT 'Corte tradicional', 'El corte de siempre: tijera, peine y mucho estilo.', 20000, 30, TRUE, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM `services` WHERE name = 'Corte tradicional');

INSERT INTO `services` (name, description, price, duration_minutes, is_active, created_at)
SELECT 'Mullet moderno', 'Un estilo retro reinventado y a tu medida.', 30000, 60, TRUE, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM `services` WHERE name = 'Mullet moderno');

INSERT INTO `services` (name, description, price, duration_minutes, is_active, created_at)
SELECT 'Degradado + diseño', 'Degradado con diseño personalizado a navaja.', 35000, 75, TRUE, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM `services` WHERE name = 'Degradado + diseño');

INSERT INTO `services` (name, description, price, duration_minutes, is_active, created_at)
SELECT 'Afeitado con navaja', 'Toalla caliente y un afeitado clásico y relajante.', 18000, 35, TRUE, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM `services` WHERE name = 'Afeitado con navaja');

INSERT INTO `services` (name, description, price, duration_minutes, is_active, created_at)
SELECT 'Corte + perfilado', 'Corte completo con perfilado de barba y contornos.', 28000, 50, TRUE, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM `services` WHERE name = 'Corte + perfilado');
