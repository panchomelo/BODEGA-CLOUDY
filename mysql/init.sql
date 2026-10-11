-- ═══════════════════════════════════════════════════
-- bodega-cloudy · init.sql (MySQL 8)
-- Se ejecuta SOLO la primera vez (volumen vacío) vía
-- /docker-entrypoint-initdb.d. Crea 3 schemas por dominio
-- + 3 usuarios con GRANT solo sobre su schema (aislamiento).
-- ═══════════════════════════════════════════════════

CREATE DATABASE IF NOT EXISTS catalogo_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS operacion_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS identidad_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usuarios de aplicación (DDL+DML solo en su schema para ddl-auto).
CREATE USER IF NOT EXISTS 'catalogo_user'@'%' IDENTIFIED BY 'catalogo123';
CREATE USER IF NOT EXISTS 'operacion_user'@'%' IDENTIFIED BY 'operacion123';
CREATE USER IF NOT EXISTS 'identidad_user'@'%' IDENTIFIED BY 'identidad123';

GRANT ALL PRIVILEGES ON catalogo_db.* TO 'catalogo_user'@'%';
GRANT ALL PRIVILEGES ON operacion_db.* TO 'operacion_user'@'%';
GRANT ALL PRIVILEGES ON identidad_db.* TO 'identidad_user'@'%';

FLUSH PRIVILEGES;
