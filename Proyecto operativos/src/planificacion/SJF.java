package planificacion;

import estructuras.Cola;
import estructuras.Lista;
import modelo.Proceso;

public class SJF implements Planificador {
    /**
     * SJF - Shortest Job First (o SRTF si es preemptive)
     * 
     * Selecciona siempre el proceso con menos tiempo restante.
     * - Modo no-preemptive: una vez entra, corre hasta terminar.
     * - Modo preemptive (SRTF): puede ser interrumpido si llega uno más corto.
     * 
     * Nota: Esta implementación selecciona al más corto cuando la CPU queda libre.
     */
    private boolean esPreemptive;
    
    public SJF(boolean esPreemptive) {
        this.esPreemptive = esPreemptive;
    }
    
    @Override
    public Proceso seleccionarSiguienteProceso(Cola<Proceso> colaListos) {
        if (colaListos.estaVacia()) {
            return null;
        }
        Lista<Proceso> procesos = colaListos.obtenerTodos();
        
        if (procesos.tamaño() == 0) {
            return null;
        }
        
        Proceso masCortoProceso = procesos.obtener(0);
        int menorTiempo = masCortoProceso.getTiempoRestante();
        
        for (int i = 1; i < procesos.tamaño(); i++) {
            Proceso p = procesos.obtener(i);
            if (p.getTiempoRestante() < menorTiempo) {
                menorTiempo = p.getTiempoRestante();
                masCortoProceso = p;
            }
        }
        
        Cola<Proceso> nuevaCola = new Cola<>();
        for (int i = 0; i < procesos.tamaño(); i++) {
            Proceso p = procesos.obtener(i);
            if (p != masCortoProceso) {
                nuevaCola.encolar(p);
            }
        }
        
        colaListos.limpiar();
        Lista<Proceso> temp = nuevaCola.obtenerTodos();
        for (int i = 0; i < temp.tamaño(); i++) {
            colaListos.encolar(temp.obtener(i));
        }
        
        return masCortoProceso;
    }
    
    @Override
    public void agregarProceso(Proceso proceso, Cola<Proceso> colaListos) {
        colaListos.encolar(proceso);
    }
    
    @Override
    public String getNombre() {
        return esPreemptive ? "SJF Preemptive (SRTF)" : "SJF Non-Preemptive";
    }
    
    @Override
    public void setQuantum(int quantum) {
        // SJF no usa quantum
    }
    
    @Override
    public int getQuantum() {
        return 0;
    }
    
    @Override
    public void reiniciar() {
        // Sin estado interno que reiniciar
    }
    
    public boolean esPreemptive() {
        return esPreemptive;
    }
    
    @Override
    public boolean debeDesalojar(Proceso procesoActual, Cola<Proceso> colaListos) {
        if (!esPreemptive || colaListos.estaVacia() || procesoActual == null) {
            return false;
        }
        
        // Buscar si hay un proceso más corto esperando
        Lista<Proceso> procesos = colaListos.obtenerTodos();
        for (int i = 0; i < procesos.tamaño(); i++) {
            Proceso p = procesos.obtener(i);
            if (p.getTiempoRestante() < procesoActual.getTiempoRestante()) {
                return true; // ¡Hay uno más corto!
            }
        }
        return false;
    }
}