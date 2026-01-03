-- Tabla de presupuestos para Mi Roma
CREATE TABLE IF NOT EXISTS presupuestos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pareja_id INT NOT NULL,
    periodo VARCHAR(50) NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pareja_id (pareja_id),
    FOREIGN KEY (pareja_id) REFERENCES parejas(id) ON DELETE CASCADE,
    UNIQUE KEY unique_pareja_periodo (pareja_id, periodo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

