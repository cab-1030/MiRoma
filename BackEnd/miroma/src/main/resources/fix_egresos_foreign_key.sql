-- Script para corregir la restricción de clave foránea en la tabla egresos
-- El problema: egresos_ibfk_3 está configurada incorrectamente
-- Debe ser: periodo_id -> presupuestos.id (no id -> presupuestos.id)

-- Paso 1: Eliminar la restricción incorrecta
ALTER TABLE egresos DROP FOREIGN KEY IF EXISTS egresos_ibfk_3;

-- Paso 2: Verificar que la columna periodo_id existe (si no existe, crearla primero)
-- Si la columna no existe, descomenta la siguiente línea:
-- ALTER TABLE egresos ADD COLUMN periodo_id INT NOT NULL;

-- Paso 3: Crear la restricción correcta para periodo_id
ALTER TABLE egresos 
ADD CONSTRAINT fk_egresos_presupuesto 
FOREIGN KEY (periodo_id) 
REFERENCES presupuestos(id) 
ON DELETE RESTRICT 
ON UPDATE CASCADE;

