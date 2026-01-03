-- Tabla de parejas para Mi Roma
-- Ejecuta este script en MySQL para crear la tabla

CREATE TABLE IF NOT EXISTS parejas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(400) NOT NULL,
    esposo_id INT NOT NULL,
    esposa_id INT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_esposo_id (esposo_id),
    INDEX idx_esposa_id (esposa_id),
    FOREIGN KEY (esposo_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (esposa_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    UNIQUE KEY unique_esposo (esposo_id),
    UNIQUE KEY unique_esposa (esposa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Actualizar la tabla usuarios para cambiar pareja_id de VARCHAR a INT
-- Ejecuta este comando solo si la tabla usuarios ya existe con pareja_id como VARCHAR
-- ALTER TABLE usuarios MODIFY COLUMN pareja_id INT NULL;

