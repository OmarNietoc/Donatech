-- Schemas por microservicio
CREATE SCHEMA IF NOT EXISTS catalog;
CREATE SCHEMA IF NOT EXISTS orders;
CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS supports;
CREATE SCHEMA IF NOT EXISTS shipping;

-- Usuarios dedicados (passwords inyectados via psql -v)
CREATE USER catalog_user  WITH PASSWORD :'catalog_pass';
CREATE USER order_user    WITH PASSWORD :'order_pass';
CREATE USER users_user    WITH PASSWORD :'users_pass';
CREATE USER supports_user WITH PASSWORD :'supports_pass';
CREATE USER shipping_user WITH PASSWORD :'shipping_pass';

-- Permisos: cada usuario solo sobre su schema
GRANT USAGE, CREATE ON SCHEMA catalog   TO catalog_user;
GRANT USAGE, CREATE ON SCHEMA orders    TO order_user;
GRANT USAGE, CREATE ON SCHEMA users     TO users_user;
GRANT USAGE, CREATE ON SCHEMA supports  TO supports_user;
GRANT USAGE, CREATE ON SCHEMA shipping  TO shipping_user;

-- Ownership de tablas futuras (requerido para ddl-auto: update)
ALTER DEFAULT PRIVILEGES IN SCHEMA catalog   GRANT ALL ON TABLES TO catalog_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA orders    GRANT ALL ON TABLES TO order_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA users     GRANT ALL ON TABLES TO users_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA supports  GRANT ALL ON TABLES TO supports_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA shipping  GRANT ALL ON TABLES TO shipping_user;
