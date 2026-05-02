-- ============================================
-- POBLAR CATEGORÍAS (Idempotente)
-- ============================================
INSERT INTO categories (name) VALUES ('alimentos') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name) VALUES ('agua_y_liquidos') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name) VALUES ('medicamentos') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name) VALUES ('higiene_personal') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name) VALUES ('ropa_y_abrigo') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name) VALUES ('herramientas') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name) VALUES ('comunicacion') ON CONFLICT (name) DO NOTHING;

-- ============================================
-- POBLAR UNIDADES (Idempotente)
-- ============================================
INSERT INTO units (name) VALUES ('unidad') ON CONFLICT (name) DO NOTHING;
INSERT INTO units (name) VALUES ('kg') ON CONFLICT (name) DO NOTHING;
INSERT INTO units (name) VALUES ('litro') ON CONFLICT (name) DO NOTHING;
INSERT INTO units (name) VALUES ('caja') ON CONFLICT (name) DO NOTHING;
INSERT INTO units (name) VALUES ('par') ON CONFLICT (name) DO NOTHING;
INSERT INTO units (name) VALUES ('fardo') ON CONFLICT (name) DO NOTHING;
INSERT INTO units (name) VALUES ('kit') ON CONFLICT (name) DO NOTHING;

-- ============================================
-- PRODUCTOS HUMANITARIOS SE CARGAN MEDIANTE
-- DatabaseInitializer.java
-- ============================================
