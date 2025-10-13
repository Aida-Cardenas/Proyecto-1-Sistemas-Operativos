package modelo;

public class Proceso implements Runnable {
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

    }
    
    public boolean debeGenerarExcepcion() {
        if (!esIOBound || ciclosParaExcepcion == 0) return false;
        return ciclosEjecutados > 0 && ciclosEjecutados % ciclosParaExcepcion == 0;
    }
    
    public void ejecutarCiclo() {
        if (pcb.getProgramCounter() < numeroInstrucciones) {
            pcb.incrementarPC();
            ciclosEjecutados++;
        }
    }
    
    public boolean haTerminado() {
        return pcb.getProgramCounter() >= numeroInstrucciones;
    }
    
    public void iniciarExcepcion() {
        enExcepcion = true;
        ciclosEnExcepcion = 0;
    }
    
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
    
    public int getTiempoRestante() {
        return numeroInstrucciones - pcb.getProgramCounter();
    }
}