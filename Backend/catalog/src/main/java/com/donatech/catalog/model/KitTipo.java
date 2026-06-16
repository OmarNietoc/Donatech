package com.donatech.catalog.model;

/**
 * Tipo de kit.
 * <ul>
 *   <li>{@code STANDARD}: kit reutilizable creado por administración, asignable a múltiples campañas.</li>
 *   <li>{@code USO_UNICO}: kit personalizado (p. ej. generado por el asistente IA) vinculado a una sola campaña.</li>
 * </ul>
 */
public enum KitTipo {
    STANDARD,
    USO_UNICO
}
