# Simulador de Planificación de Procesos — Proyecto 1 (Sistemas Operativos)

Este repositorio contiene un simulador didáctico de un sistema operativo a nivel de planificación de CPU. El enfoque es práctico y directo: representar las colas, avanzar ciclos, registrar eventos y mostrar el estado de los procesos en una interfaz gráfica simple.

[Ver informe en PDF (docs)](Proyecto%20operativos/docs/Informe%20Simulador%20-%20Ricardo%20Baeta-Aida-Cardenas-Rene%20Chamorro.pdf)

## ¿Qué hace el simulador?

- Mantiene y opera las colas de procesos: listos, bloqueados y sus variantes suspendidas.
- Selecciona el siguiente proceso a ejecutar según el planificador activo.
- Ejecuta ciclos de CPU, simula excepciones de I/O y cambios de modo (SO/usuario).
- Registra métricas básicas (uso de CPU, tiempos por proceso) y un log de eventos por ciclo.
- Expone todo en una GUI con paneles dedicados para colas, PCB, métricas, gráficas y log.

## Planificadores incluidos

- FCFS (First-Come, First-Served)
- SJF (Shortest Job First)
- Prioridad (estática)
- Round Robin (con quantum configurable)
- Colas Multinivel (FCFS por nivel de prioridad)
- Colas Multinivel con Retroalimentación (degradación por uso de CPU)

Cada planificador implementa la misma interfaz y puede cambiarse en caliente durante la simulación.

## Estructura del proyecto

Carpeta principal del proyecto NetBeans/Ant: `Proyecto operativos/`

- `src/` — Código fuente organizado por paquetes:
  - `estructuras/` — Estructuras de datos simples (colas y listas) usadas por el simulador.
  - `modelo/` — Entidades del dominio: `Proceso`, `PCB`, `EstadoProceso`.
  - `planificacion/` — Implementaciones de los algoritmos de planificación.
  - `sistema/` — Núcleo del simulador (hilo principal, métricas, listener de UI).
  - `gui/` — Interfaz gráfica: paneles para colas, PCB, métricas, gráficas, log y ventana principal.
- `docs/` — Documentación del proyecto (informe en PDF).
- `configuracion.csv` — Parámetros de simulación (por ejemplo, quantum, ciclos, etc.).
- `procesos.csv` — Definición de procesos de entrada (nombres, tiempos, prioridades, I/O, etc.).
- `build.xml` — Script de Ant para compilar/ejecutar (proyecto estilo NetBeans).
- `manifest.mf`, `nbproject/` — Metadatos del proyecto.

## Cómo ejecutar

Opción 1 — NetBeans

- Abrir la carpeta `Proyecto operativos/` como proyecto en NetBeans.
- Limpiar y construir; luego ejecutar la aplicación desde el IDE.

Opción 2 — Ant (línea de comando)

- Requiere Java y Ant instalados en el sistema.
- Desde la carpeta `Proyecto operativos/`, ejecutar las tareas de Ant definidas en `build.xml` (por ejemplo, `clean`, `jar`, `run`) según la configuración del proyecto.

## Configuración y datos de entrada

- `configuracion.csv`: controla parámetros globales de la simulación (p. ej. duración del ciclo, quantum por planificador, etc.).
- `procesos.csv`: lista de procesos iniciales con sus atributos básicos. El simulador también permite agregar procesos en tiempo de ejecución desde la GUI.

Ambos archivos se cargan al inicio para inicializar el entorno de ejecución.

## Interfaz gráfica (GUI)

- Panel de Colas — Muestra listos, bloqueados, suspendidos y el proceso en CPU.
- Panel PCB — Detalle del PCB del proceso seleccionado (estado, PC, tiempos).
- Panel Métricas — Uso de CPU, tiempos de respuesta/espera/retorno, procesos completados.
- Panel Gráficas — Visualización de la evolución de métricas a través de los ciclos.
- Panel Log — Trazas legibles por ciclo con eventos del simulador.

La GUI se actualiza en cada ciclo y al producirse cambios relevantes (selección de proceso, I/O, suspensión, cambio de planificador, etc.).

## Notas de diseño

- El simulador corre en un hilo propio y coordina acceso a las colas con bloqueos ligeros.
- Los planificadores encapsulan la política de selección; el simulador los orquesta sin conocer sus detalles internos.
- La prioridad, el quantum y las excepciones de I/O son parámetros clave para observar comportamientos.

## Documentación

- Informe PDF con detalles del diseño y resultados: 
  [Informe Simulador (PDF)](Proyecto%20operativos/docs/Informe%20Simulador%20-%20Ricardo%20Baeta-Aida-Cardenas-Rene%20Chamorro.pdf)

---

Si quieres extender el proyecto (nuevos planificadores, más métricas o mejoras de UI), la estructura por paquetes facilita añadir y probar componentes sin afectar el resto del sistema.