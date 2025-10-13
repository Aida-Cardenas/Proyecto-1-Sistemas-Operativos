
package planificacion;

import modelo.Proceso;
import estructuras.Cola;
import estructuras.Lista;

public class Prioridad implements Planificador {
    private boolean esPreemptive;
    
    public Prioridad(boolean esPreemptive) {
        this.esPreemptive = esPreemptive;
    }
    

    public Proceso seleccionarSiguienteProceso(Cola<Proceso> colaListos) {
        if (colaListos.estaVacia()) {
            return null;
        }
        
        Lista<Proceso> procesos = colaListos.obtenerTodos();
        
        if (procesos.tamaño() == 0) {
            return null;
        }
        
        // Encontrar el proceso con mayor prioridad (menor número = mayor prioridad)
        Proceso mayorPrioridad = procesos.obtener(0);
        int menorNumeroPrioridad = mayorPrioridad.getPcb().getPrioridad();
        
        for (int i = 1; i < procesos.tamaño(); i++) {
            Proceso p = procesos.obtener(i);
            if (p.getPcb().getPrioridad() < menorNumeroPrioridad) {
                menorNumeroPrioridad = p.getPcb().getPrioridad();
                mayorPrioridad = p;
            }
        }
        
        // Reconstruir la cola sin el proceso seleccionado
        Cola<Proceso> nuevaCola = new Cola<>();
        for (int i = 0; i < procesos.tamaño(); i++) {
            Proceso p = procesos.obtener(i);
            if (p != mayorPrioridad) {
                nuevaCola.encolar(p);
            }
        }
        
        colaListos.limpiar();
        Lista<Proceso> temp = nuevaCola.obtenerTodos();
        for (int i = 0; i < temp.tamaño(); i++) {
            colaListos.encolar(temp.obtener(i));
        }
        
        return mayorPrioridad;
    }
    

    public void agregarProceso(Proceso proceso, Cola<Proceso> colaListos) {
        colaListos.encolar(proceso);
    }
    

    public String getNombre() {
        return esPreemptive ? "Prioridad Preemptive" : "Prioridad Non-Preemptive";
    }
    

    public void setQuantum(int quantum) {
        // Prioridad no usa quantum
    }

    public int getQuantum() {
        return 0;
    }
    

    public void reiniciar() {
        // No hay estado que reiniciar
    }
    
    public boolean esPreemptive() {
        return esPreemptive;
    }
}