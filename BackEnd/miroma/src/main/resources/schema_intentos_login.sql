-- Tabla para rastrear intentos fallidos de login
CREATE TABLE IF NOT EXISTS intentos_login (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    intentos_fallidos INT NOT NULL DEFAULT 0,
    ultimo_intento DATETIME NULL,
    bloqueado_hasta DATETIME NULL,
    nivel_bloqueo INT NOT NULL DEFAULT 0,
    INDEX idx_email (email),
    INDEX idx_bloqueado_hasta (bloqueado_hasta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

