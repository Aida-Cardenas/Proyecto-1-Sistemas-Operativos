package planificacion;

import modelo.Proceso;
import estructuras.Cola;

public class RoundRobin implements Planificador {
    private int quantum;
    private int quantumRestante;
    
    public RoundRobin(int quantum) {
        this.quantum = quantum;
        this.quantumRestante = quantum;
    }
    
    @Override
    public Proceso seleccionarSiguienteProceso(Cola<Proceso> colaListos) {
        if (colaListos.estaVacia()) {
            return null;
        }
        quantumRestante = quantum;
        return colaListos.desencolar();
    }
    
    @Override
    public void agregarProceso(Proceso proceso, Cola<Proceso> colaListos) {
        colaListos.encolar(proceso);
    }
    
    public boolean seAcaboQuantum() {
        return quantumRestante <= 0;
    }
    
    public void consumirQuantum() {
        quantumRestante--;
    }
    
    public void reiniciarQuantum() {
        quantumRestante = quantum;
    }
    
    @Override
    public String getNombre() {
        return "Round Robin (Q=" + quantum + ")";
    }
    
    @Override
    public void setQuantum(int quantum) {
        this.quantum = quantum;
        this.quantumRestante = quantum;
    }
    
    @Override
    public int getQuantum() {
        return quantum;
    }
    
    public int getQuantumRestante() {
        return quantumRestante;
    }
    
    @Override
    public void reiniciar() {
        quantumRestante = quantum;
    }
}