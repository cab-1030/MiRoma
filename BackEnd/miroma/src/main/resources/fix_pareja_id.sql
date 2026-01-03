-- Script para verificar y corregir la columna pareja_id
-- Ejecuta estos comandos en MySQL para verificar y corregir si es necesario

-- 1. Verificar la estructura actual de la tabla
DESCRIBE usuarios;

-- 2. Si pareja_id tiene NOT NULL, ejecuta este comando para permitir NULL:
ALTER TABLE usuarios MODIFY COLUMN pareja_id VARCHAR(255) NULL;

-- 3. Verificar que el cambio se aplicó correctamente
DESCRIBE usuarios;

