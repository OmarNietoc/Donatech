-- ============================================
-- REGIONES DE CHILE (Idempotente)
-- ============================================
INSERT INTO regions (id, name) VALUES (1, 'Región de Arica y Parinacota') ON CONFLICT (id) DO NOTHING;
INSERT INTO regions (id, name) VALUES (2, 'Región de Tarapacá') ON CONFLICT (id) DO NOTHING;
INSERT INTO regions (id, name) VALUES (3, 'Región de Antofagasta') ON CONFLICT (id) DO NOTHING;
INSERT INTO regions (id, name) VALUES (4, 'Región de Atacama') ON CONFLICT (id) DO NOTHING;
INSERT INTO regions (id, name) VALUES (5, 'Región de Coquimbo') ON CONFLICT (id) DO NOTHING;
INSERT INTO regions (id, name) VALUES (6, 'Región de Valparaíso') ON CONFLICT (id) DO NOTHING;
INSERT INTO regions (id, name) VALUES (7, 'Región Metropolitana de Santiago') ON CONFLICT (id) DO NOTHING;
INSERT INTO regions (id, name) VALUES (8, 'Región del Libertador General Bernardo O''Higgins') ON CONFLICT (id) DO NOTHING;
INSERT INTO regions (id, name) VALUES (9, 'Región del Maule') ON CONFLICT (id) DO NOTHING;
INSERT INTO regions (id, name) VALUES (10, 'Región de Ñuble') ON CONFLICT (id) DO NOTHING;
INSERT INTO regions (id, name) VALUES (11, 'Región del Biobío') ON CONFLICT (id) DO NOTHING;
INSERT INTO regions (id, name) VALUES (12, 'Región de La Araucanía') ON CONFLICT (id) DO NOTHING;
INSERT INTO regions (id, name) VALUES (13, 'Región de Los Ríos') ON CONFLICT (id) DO NOTHING;
INSERT INTO regions (id, name) VALUES (14, 'Región de Los Lagos') ON CONFLICT (id) DO NOTHING;
INSERT INTO regions (id, name) VALUES (15, 'Región de Aysén del General Carlos Ibáñez del Campo') ON CONFLICT (id) DO NOTHING;
INSERT INTO regions (id, name) VALUES (16, 'Región de Magallanes y de la Antártica Chilena') ON CONFLICT (id) DO NOTHING;

-- ============================================
-- COMUNAS - REGIÓN METROPOLITANA (Idempotente)
-- ============================================
INSERT INTO comunas (id, name, id_region) VALUES (1, 'Santiago', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (2, 'Cerrillos', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (3, 'Cerro Navia', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (4, 'Conchalí', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (5, 'El Bosque', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (6, 'Estación Central', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (7, 'Huechuraba', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (8, 'Independencia', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (9, 'La Cisterna', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (10, 'La Florida', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (11, 'La Granja', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (12, 'La Pintana', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (13, 'La Reina', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (14, 'Las Condes', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (15, 'Lo Barnechea', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (16, 'Lo Espejo', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (17, 'Lo Prado', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (18, 'Macul', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (19, 'Maipú', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (20, 'Ñuñoa', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (21, 'Pedro Aguirre Cerda', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (22, 'Peñalolén', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (23, 'Providencia', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (24, 'Pudahuel', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (25, 'Quilicura', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (26, 'Quinta Normal', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (27, 'Recoleta', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (28, 'Renca', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (29, 'San Joaquín', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (30, 'San Miguel', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (31, 'San Ramón', 7) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (32, 'Vitacura', 7) ON CONFLICT (id) DO NOTHING;

-- ============================================
-- COMUNAS - OTRAS REGIONES (Idempotente)
-- ============================================
INSERT INTO comunas (id, name, id_region) VALUES (33, 'Arica', 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (34, 'Camarones', 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (35, 'Iquique', 2) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (36, 'Alto Hospicio', 2) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (37, 'Antofagasta', 3) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (38, 'Calama', 3) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (39, 'Copiapó', 4) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (40, 'Vallenar', 4) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (41, 'La Serena', 5) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (42, 'Coquimbo', 5) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (43, 'Valparaíso', 6) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (44, 'Viña del Mar', 6) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (45, 'Rancagua', 8) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (46, 'San Fernando', 8) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (47, 'Talca', 9) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (48, 'Curicó', 9) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (49, 'Chillán', 10) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (50, 'San Carlos', 10) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (51, 'Concepción', 11) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (52, 'Talcahuano', 11) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (53, 'Temuco', 12) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (54, 'Padre Las Casas', 12) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (55, 'Valdivia', 13) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (56, 'La Unión', 13) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (57, 'Puerto Montt', 14) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (58, 'Osorno', 14) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (59, 'Coyhaique', 15) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (60, 'Puerto Aysén', 15) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (61, 'Punta Arenas', 16) ON CONFLICT (id) DO NOTHING;
INSERT INTO comunas (id, name, id_region) VALUES (62, 'Puerto Natales', 16) ON CONFLICT (id) DO NOTHING;

-- ============================================
-- ROLES DONATECH (Idempotente)
-- ============================================
INSERT INTO role (id, name) VALUES (1, 'ROLE_ADMIN') ON CONFLICT (id) DO NOTHING;
INSERT INTO role (id, name) VALUES (2, 'ROLE_DONANTE') ON CONFLICT (id) DO NOTHING;
INSERT INTO role (id, name) VALUES (3, 'ROLE_VOLUNTARIO') ON CONFLICT (id) DO NOTHING;
INSERT INTO role (id, name) VALUES (4, 'ROLE_BENEFICIARIO') ON CONFLICT (id) DO NOTHING;
INSERT INTO role (id, name) VALUES (5, 'ROLE_ORGANIZACION') ON CONFLICT (id) DO NOTHING;
