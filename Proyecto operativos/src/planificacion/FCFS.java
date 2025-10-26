package planificacion;

import estructuras.Cola;
import modelo.Proceso;

public class FCFS implements Planificador {
    /**
     * FCFS - First Come, First Served
     * 
     * El más simple: el que llega primero es el que se atiende primero.
     * No hay prioridades, no hay preempción, no hay quantum.
     */
    
    @Override
    public Proceso seleccionarSiguienteProceso(Cola<Proceso> colaListos) {
        if (colaListos.estaVacia()) {
            return null;
        }
        return colaListos.desencolar();
    }
    
    @Override
    public void agregarProceso(Proceso proceso, Cola<Proceso> colaListos) {
        colaListos.encolar(proceso);
    }
    
    @Override
    public String getNombre() {
        return "FCFS";
    }
    
    @Override
    public void setQuantum(int quantum) {
        // FCFS no usa quantum
    }
    
    @Override
    public int getQuantum() {
        return 0;
    }
    
    @Override
    public void reiniciar() {
        // Sin estado interno que reiniciar
    }
    
    @Override
    public boolean debeDesalojar(Proceso procesoActual, Cola<Proceso> colaListos) {
        // FCFS nunca desaloja procesos (no es preemptive)
        return false;
    }
}