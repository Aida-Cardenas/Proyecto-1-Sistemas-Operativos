package planificacion;

import estructuras.Cola;
import modelo.Proceso;

public class RoundRobin implements Planificador {
    /**
     * Round Robin - Justicia por turnos
     * 
     * Cada proceso recibe un "slice" de CPU (quantum). Si no termina en su turno,
     * se regresa al final de la cola para dar chance al siguiente.
     */
    private int quantum;
    private int quantumRestante;
    
    public RoundRobin(int quantum) {
        this.quantum = quantum;
        this.quantumRestante = quantum;
    }
    

    public Proceso seleccionarSiguienteProceso(Cola<Proceso> colaListos) {
        if (colaListos.estaVacia()) {
            return null;
        }
        quantumRestante = quantum;
        return colaListos.desencolar();
    }
    

    public void agregarProceso(Proceso proceso, Cola<Proceso> colaListos) {
        colaListos.encolar(proceso);
    }
    
    // ¿Se agotó el turno del proceso actual?
    public boolean seAcaboQuantum() {
        return quantumRestante <= 0;
    }
    
    // Consumir un ciclo del quantum (lo invoca el simulador cada ciclo)
    public void consumirQuantum() {
        quantumRestante--;
    }
    
    // Reiniciar el quantum cuando entra un nuevo proceso a CPU
    public void reiniciarQuantum() {
        quantumRestante = quantum;
    }
    

    public String getNombre() {
        return "Round Robin (Q=" + quantum + ")";
    }
    

    public void setQuantum(int quantum) {
        this.quantum = quantum;
        this.quantumRestante = quantum;
    }
    

    public int getQuantum() {
        return quantum;
    }
    
    public int getQuantumRestante() {
        return quantumRestante;
    }
    

    public void reiniciar() {
        quantumRestante = quantum;
    }
    
    @Override
    public boolean debeDesalojar(Proceso procesoActual, Cola<Proceso> colaListos) {
        // Round Robin usa su propia lógica de quantum en el Simulador
        return false;
    }
}