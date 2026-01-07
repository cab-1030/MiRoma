-- Script para aumentar el tamaño de la columna 'accion' en la tabla log_eventos
-- El tamaño actual (500) es insuficiente para algunos mensajes de log

ALTER TABLE log_eventos 
MODIFY COLUMN accion VARCHAR(1000) NOT NULL;


