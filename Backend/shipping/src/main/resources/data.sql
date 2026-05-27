-- ============================================================
-- SEED DATA: shipping-service  (schema: shipping)
-- Idempotente: usa INSERT ... WHERE NOT EXISTS para evitar
-- duplicados (routes usa UUID como PK generado por Hibernate).
-- Se ejecuta cada startup (ddl-auto: update no borra datos).
-- ============================================================

-- ─────────────────────────────────────────
-- RUTAS DE ENVÍO (5 registros)
-- Solo se insertan si la tabla está completamente vacía.
-- ─────────────────────────────────────────

-- Ruta 1
INSERT INTO routes (id, company_id, route_date, origin_address, status, optimized_path_json)
SELECT gen_random_uuid(),
       'company-001',
       CURRENT_DATE,
       'Av. Providencia 1234, Providencia, Santiago',
       'PLANNED',
       '{"waypoints":["Providencia","Las Condes","Ñuñoa"]}'
WHERE NOT EXISTS (SELECT 1 FROM routes LIMIT 1);

-- Ruta 2
INSERT INTO routes (id, company_id, route_date, origin_address, status, optimized_path_json)
SELECT gen_random_uuid(),
       'company-002',
       CURRENT_DATE + 1,
       'Av. Gran Bretaña 551, Puente Alto, Santiago',
       'PLANNED',
       '{"waypoints":["Puente Alto","La Florida","San Bernardo"]}'
WHERE NOT EXISTS (SELECT 1 FROM routes OFFSET 1 LIMIT 1);

-- Ruta 3
INSERT INTO routes (id, company_id, route_date, origin_address, status, optimized_path_json)
SELECT gen_random_uuid(),
       'company-001',
       CURRENT_DATE - 1,
       'Av. Recoleta 505, Recoleta, Santiago',
       'COMPLETED',
       '{"waypoints":["Recoleta","Conchalí","Huechuraba"]}'
WHERE NOT EXISTS (SELECT 1 FROM routes OFFSET 2 LIMIT 1);

-- Ruta 4
INSERT INTO routes (id, company_id, route_date, origin_address, status, optimized_path_json)
SELECT gen_random_uuid(),
       'company-003',
       CURRENT_DATE,
       'Blanco Encalada 2120, Santiago Centro',
       'IN_PROGRESS',
       '{"waypoints":["Santiago","Cerro Navia","Pudahuel"]}'
WHERE NOT EXISTS (SELECT 1 FROM routes OFFSET 3 LIMIT 1);

-- Ruta 5
INSERT INTO routes (id, company_id, route_date, origin_address, status, optimized_path_json)
SELECT gen_random_uuid(),
       'company-002',
       CURRENT_DATE + 2,
       'Av. Grecia 3200, Ñuñoa, Santiago',
       'PLANNED',
       '{"waypoints":["Ñuñoa","Macul","La Granja"]}'
WHERE NOT EXISTS (SELECT 1 FROM routes OFFSET 4 LIMIT 1);
