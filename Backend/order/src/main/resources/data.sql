-- ============================================================
-- SEED DATA: order-service  (schema: orders)
-- Idempotente: ON CONFLICT DO NOTHING en tablas con UNIQUE constraint.
-- Se ejecuta cada startup de Spring Boot (ddl-auto: update no borra datos).
-- ============================================================

-- ─────────────────────────────────────────
-- CUPONES DE DESCUENTO (5 registros)
-- ─────────────────────────────────────────
INSERT INTO coupons (code, discount_amount, active)
VALUES ('DONATECH10', 1000, true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO coupons (code, discount_amount, active)
VALUES ('DONATECH20', 2000, true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO coupons (code, discount_amount, active)
VALUES ('BIENVENIDO', 500, true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO coupons (code, discount_amount, active)
VALUES ('EMERGENCIA', 3000, true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO coupons (code, discount_amount, active)
VALUES ('SOLIDARIO50', 1500, false)
ON CONFLICT (code) DO NOTHING;
