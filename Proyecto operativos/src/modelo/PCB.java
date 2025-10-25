package modelo;

public class PCB {
    /**
     * PCB - Bloque de Control de Proceso
     * 
     * El expediente del proceso. Aquí guardamos toda su identidad y estado:
     * - Identificación (id, nombre)
     * - Estado actual (Nuevo/Listo/Ejecución/...)
     * - Registros simulados (PC y MAR)
     * - Atributos de planificación (prioridad, quantum)
     * - Tiempos (llegada, inicio, fin, espera, respuesta)
     * 
     * El simulador consulta y actualiza esto constantemente para tomar decisiones.
     */
    private static int contadorID = 1; // contador de id

    private int id; 
    private String nombre; // nombre  proceso
    private EstadoProceso estado; // estado actual
    private int programCounter; // contador de programa
    private int memoryAddressRegister; // direccion de memoria
    private int prioridad; // prioridad 
    private int quantum; // quantum 
    private long tiempoLlegada; // tiempo de llegada
    private long tiempoInicioEjecucion; // tiempo de inicio
    private long tiempoFinalizacion; // tiempo para que termine 
    private long tiempoEspera; // tiempo esperando
    private long tiempoRespuesta; // tiempo de respuesta
    private boolean primerEjecucion; // si es la primera vez que ejecuta

    // Crear un nuevo PCB con valores por defecto coherentes
    public PCB(String nombre) {
        this.id = contadorID++;
        this.nombre = nombre;
        this.estado = EstadoProceso.NUEVO;
        this.programCounter = 0;
        this.memoryAddressRegister = 0;
        this.prioridad = 0;
        this.quantum = 0;
        this.tiempoLlegada = System.currentTimeMillis();
        this.tiempoInicioEjecucion = -1;
        this.tiempoFinalizacion = -1;
        this.tiempoEspera = 0;
        this.tiempoRespuesta = -1;
        this.primerEjecucion = true;
    }


    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public EstadoProceso getEstado() { return estado; }
    public void setEstado(EstadoProceso estado) { this.estado = estado; }
    public int getProgramCounter() { return programCounter; }
    public void setProgramCounter(int programCounter) { this.programCounter = programCounter; }
    public int getMemoryAddressRegister() { return memoryAddressRegister; }
    public void setMemoryAddressRegister(int memoryAddressRegister) { this.memoryAddressRegister = memoryAddressRegister; }
    public int getPrioridad() { return prioridad; }
    public void setPrioridad(int prioridad) { this.prioridad = prioridad; }
    public int getQuantum() { return quantum; }
    public void setQuantum(int quantum) { this.quantum = quantum; }
    public long getTiempoLlegada() { return tiempoLlegada; }
    public long getTiempoInicioEjecucion() { return tiempoInicioEjecucion; }
    public void setTiempoInicioEjecucion(long tiempo) { this.tiempoInicioEjecucion = tiempo; }
    public long getTiempoFinalizacion() { return tiempoFinalizacion; }
    public void setTiempoFinalizacion(long tiempo) { this.tiempoFinalizacion = tiempo; }
    public long getTiempoEspera() { return tiempoEspera; }
    public void setTiempoEspera(long tiempo) { this.tiempoEspera = tiempo; }
    public long getTiempoRespuesta() { return tiempoRespuesta; }
    public void setTiempoRespuesta(long tiempo) { this.tiempoRespuesta = tiempo; }
    public boolean esPrimerEjecucion() { return primerEjecucion; }
    public void setPrimerEjecucion(boolean primerEjecucion) { this.primerEjecucion = primerEjecucion; }

    // Simula la ejecución de una instrucción: avanza PC y MAR
    public void incrementarPC() {
        programCounter++;
        memoryAddressRegister++;
    }

    // Representación compacta para las tablas/paneles de la interfaz
    @Override
    public String toString() {
        return "ID: " + id + " | " + nombre + " | PC: " + programCounter + " | MAR: " + memoryAddressRegister + " | Estado: " + estado;
    }

    // Reinicia el contador global de IDs (reiniciar simulacion)
    public static void reiniciarContador() {
        contadorID = 1;
    }
}