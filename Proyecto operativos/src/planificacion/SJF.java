package planificacion;

import modelo.Proceso;
import estructuras.Cola;
import estructuras.Lista;

public class SJF implements Planificador {
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
    }
    
    @Override
    public int getQuantum() {
        return 0;
    }
    
    @Override
    public void reiniciar() {
    }
    
    public boolean esPreemptive() {
        return esPreemptive;
    }
}