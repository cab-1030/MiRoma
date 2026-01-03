-- Script para corregir la columna pareja_id en la tabla usuarios
-- Ejecuta este script en MySQL

-- Modificar la columna pareja_id para que:
-- 1. Permita NULL
-- 2. Tenga NULL como valor por defecto
ALTER TABLE usuarios 
MODIFY COLUMN pareja_id VARCHAR(255) NULL DEFAULT NULL;

-- Verificar que el cambio se aplicó correctamente
DESCRIBE usuarios;

