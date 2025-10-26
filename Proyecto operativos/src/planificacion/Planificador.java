package planificacion;

import modelo.Proceso;
import estructuras.Cola;

public interface Planificador {
    Proceso seleccionarSiguienteProceso(Cola<Proceso> colaListos);
    void agregarProceso(Proceso proceso, Cola<Proceso> colaListos);
    String getNombre();
    void setQuantum(int quantum);
    int getQuantum();
    void reiniciar();
    boolean debeDesalojar(Proceso procesoActual, Cola<Proceso> colaListos);
}