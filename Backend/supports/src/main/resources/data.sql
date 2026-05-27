-- ============================================================
-- SEED DATA: supports-service  (schema: supports)
-- Idempotente: usa INSERT ... WHERE NOT EXISTS para evitar
-- duplicados ya que soportes no tiene columna UNIQUE natural.
-- Se ejecuta cada startup (ddl-auto: update no borra datos).
-- ============================================================

-- ─────────────────────────────────────────
-- TICKETS DE SOPORTE (5 registros)
-- Solo se insertan si la tabla está completamente vacía.
-- ─────────────────────────────────────────
INSERT INTO soportes (descripcion, estado, usuario_id, prioridad, tipo, titulo, fecha_creacion)
SELECT 'No puedo completar mi donación. El sistema muestra un error al procesar el pago en línea.',
       'PENDIENTE', 1, 'ALTO', 'TECNICO',
       'Error al procesar donación', NOW()
WHERE NOT EXISTS (SELECT 1 FROM soportes LIMIT 1);

INSERT INTO soportes (descripcion, estado, usuario_id, prioridad, tipo, titulo, fecha_creacion)
SELECT 'El producto "Frazada polar" aparece como agotado pero fue donado hace dos días y no se ha actualizado el stock.',
       'EN_PROGRESO', 2, 'MEDIO', 'PRODUCTO',
       'Stock no actualizado en catálogo', NOW()
WHERE NOT EXISTS (SELECT 1 FROM soportes OFFSET 1 LIMIT 1);

INSERT INTO soportes (descripcion, estado, usuario_id, prioridad, tipo, titulo, fecha_creacion)
SELECT 'Necesito cambiar la dirección de entrega de mi donación. La donación está en estado INGRESADA.',
       'PENDIENTE', 3, 'BAJO', 'DONACION',
       'Cambio de dirección de entrega', NOW()
WHERE NOT EXISTS (SELECT 1 FROM soportes OFFSET 2 LIMIT 1);

INSERT INTO soportes (descripcion, estado, usuario_id, prioridad, tipo, titulo, fecha_creacion)
SELECT 'No puedo iniciar sesión con mis credenciales. He intentado restablecer la contraseña pero no llega el email.',
       'PENDIENTE', 4, 'ALTO', 'USUARIO',
       'Problema de acceso a la cuenta', NOW()
WHERE NOT EXISTS (SELECT 1 FROM soportes OFFSET 3 LIMIT 1);

INSERT INTO soportes (descripcion, estado, usuario_id, prioridad, tipo, titulo, fecha_creacion)
SELECT 'La transferencia bancaria fue rechazada pero el sistema no muestra el motivo del rechazo. Necesito orientación para reintentar.',
       'PENDIENTE', 5, 'CRITICO', 'VALIDACION_TRANSFERENCIA',
       'Transferencia rechazada sin explicación', NOW()
WHERE NOT EXISTS (SELECT 1 FROM soportes OFFSET 4 LIMIT 1);
