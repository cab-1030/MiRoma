-- Agregar campo token_version a la tabla usuarios para invalidar tokens al cambiar contraseña
ALTER TABLE usuarios 
ADD COLUMN token_version INT NOT NULL DEFAULT 1;


