package modelo;

/**
 * EstadoProceso - Los posibles estados de un proceso
 * 
 * Tal como en un SO real, un proceso puede moverse entre estos estados
 * durante su vida. Aquí los usamos para pintar la historia del proceso
 * dentro del simulador:
 * - NUEVO: recién creado, aún no entra a ejecución
 * - LISTO: está listo para usar CPU, esperando su turno
 * - EJECUCION: actualmente en CPU, corriendo instrucciones
 * - BLOQUEADO: detenido por I/O u otra espera
 * - LISTO_SUSPENDIDO: listo, pero enviado a almacenamiento secundario
 * - BLOQUEADO_SUSPENDIDO: bloqueado y además suspendido
 * - TERMINADO: completó sus instrucciones, fin del viaje
 */
public enum EstadoProceso {
    NUEVO,
    LISTO,
    EJECUCION,
    BLOQUEADO,
    LISTO_SUSPENDIDO,
    BLOQUEADO_SUSPENDIDO,
    TERMINADO
}