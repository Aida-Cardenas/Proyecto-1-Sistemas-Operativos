package planificacion;

import modelo.Proceso;
import estructuras.Cola;

public class FCFS implements Planificador {
    
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
    }
    
    @Override
    public int getQuantum() {
        return 0;
    }
    
    @Override
    public void reiniciar() {
    }
}