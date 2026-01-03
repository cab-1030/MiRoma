-- Script para modificar la columna pareja_id y permitir NULL o establecer un valor por defecto
-- Ejecuta estos comandos en MySQL

-- Opción 1: Permitir NULL (RECOMENDADO para campos opcionales)
ALTER TABLE usuarios MODIFY COLUMN pareja_id VARCHAR(255) NULL DEFAULT NULL;

-- Opción 2: Si prefieres un valor por defecto (cadena vacía)
-- ALTER TABLE usuarios MODIFY COLUMN pareja_id VARCHAR(255) NOT NULL DEFAULT '';

-- Verificar que el cambio se aplicó correctamente
DESCRIBE usuarios;

