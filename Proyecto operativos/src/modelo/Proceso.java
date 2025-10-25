package modelo;

public class Proceso implements Runnable {
    /**
     * Proceso - Modelo de un proceso en el simulador
     * 
     * Representa un programa en ejecución con su PCB y su "carga de trabajo":
     * - numeroInstrucciones: cuántas instrucciones debe completar
     * - esCPUBound / esIOBound: si consume puro CPU o genera I/O
     * - ciclosParaExcepcion: cada cuántos ciclos genera I/O (si aplica)
     * - ciclosParaCompletarExcepcion: cuántos ciclos dura esa I/O
     * 
     * El simulador le va dando CPU ciclo a ciclo y este objeto va
     * avanzando su PC, generando excepciones de I/O y avisando cuando termina.
     */
    private PCB pcb;
    private int numeroInstrucciones;
    private boolean esCPUBound;
    private boolean esIOBound;
    private int ciclosParaExcepcion;
    private int ciclosParaCompletarExcepcion;
    private int ciclosEjecutados;
    private boolean enExcepcion;
    private int ciclosEnExcepcion;
    private volatile boolean ejecutando;
    private volatile boolean pausado;
    private Thread thread;
    
    public Proceso(String nombre, int numeroInstrucciones, boolean esCPUBound, 
                   int ciclosParaExcepcion, int ciclosParaCompletarExcepcion) {
        this.pcb = new PCB(nombre);
        this.numeroInstrucciones = numeroInstrucciones;
        this.esCPUBound = esCPUBound;
        this.esIOBound = !esCPUBound;
        this.ciclosParaExcepcion = ciclosParaExcepcion;
        this.ciclosParaCompletarExcepcion = ciclosParaCompletarExcepcion;
        this.ciclosEjecutados = 0;
        this.enExcepcion = false;
        this.ciclosEnExcepcion = 0;
        this.ejecutando = false;
        this.pausado = false;
    }
    
    @Override
    public void run() {
        // No usamos un hilo por proceso en este simulador.
        // La ejecución se simula "a pulsos" desde Simulador.ejecutarCiclo().
    }
    
    // ¿Debe generar I/O justo ahora? 
    public boolean debeGenerarExcepcion() {
        if (!esIOBound || ciclosParaExcepcion == 0) return false;
        return ciclosEjecutados > 0 && ciclosEjecutados % ciclosParaExcepcion == 0;
    }
    
    // Un ciclo de CPU: avanza PC y cuenta el progreso
    public void ejecutarCiclo() {
        if (pcb.getProgramCounter() < numeroInstrucciones) {
            pcb.incrementarPC();
            ciclosEjecutados++;
        }
    }
    
    // ¿Ya terminó todas sus instrucciones?
    public boolean haTerminado() {
        return pcb.getProgramCounter() >= numeroInstrucciones;
    }
    
    // Entra a estado de I/O (bloqueado en el simulador)
    public void iniciarExcepcion() {
        enExcepcion = true;
        ciclosEnExcepcion = 0;
    }
    
    // Procesa un ciclo de I/O; devuelve true cuando se completa
    public boolean procesarExcepcion() {
        if (!enExcepcion) return false;
        ciclosEnExcepcion++;
        if (ciclosEnExcepcion >= ciclosParaCompletarExcepcion) {
            enExcepcion = false;
            return true; 
        }
        return false;
    }

    public PCB getPcb() { return pcb; }
    public int getNumeroInstrucciones() { return numeroInstrucciones; }
    public boolean esCPUBound() { return esCPUBound; }
    public boolean esIOBound() { return esIOBound; }
    public int getCiclosParaExcepcion() { return ciclosParaExcepcion; }
    public int getCiclosParaCompletarExcepcion() { return ciclosParaCompletarExcepcion; }
    public int getCiclosEjecutados() { return ciclosEjecutados; }
    public boolean isEnExcepcion() { return enExcepcion; }
    public int getCiclosEnExcepcion() { return ciclosEnExcepcion; }
    
    // Instrucciones restantes por completar (para SJF/SRTF)
    public int getTiempoRestante() {
        return numeroInstrucciones - pcb.getProgramCounter();
    }
}