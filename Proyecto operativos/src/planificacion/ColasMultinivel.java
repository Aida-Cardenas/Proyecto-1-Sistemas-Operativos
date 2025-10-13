package planificacion;

import estructuras.Cola;
import estructuras.Lista;
import modelo.Proceso;

public class ColasMultinivel implements Planificador {
    private Cola<Proceso> colaPrioridad1; 
    private Cola<Proceso> colaPrioridad2; 
    private Cola<Proceso> colaPrioridad3; 
    private int quantum;
    
    public ColasMultinivel(int quantum) {
        this.colaPrioridad1 = new Cola<>();
        this.colaPrioridad2 = new Cola<>();
        this.colaPrioridad3 = new Cola<>();
        this.quantum = quantum;
    }
    

    public Proceso seleccionarSiguienteProceso(Cola<Proceso> colaListos) {
        //organizar todos los procesos en las colas por nivel
        while (!colaListos.estaVacia()) {
            Proceso p = colaListos.desencolar();
            int prioridad = p.getPcb().getPrioridad();
            
            if (prioridad <= 1) {
                colaPrioridad1.encolar(p);
            } else if (prioridad <= 3) {
                colaPrioridad2.encolar(p);
            } else {
                colaPrioridad3.encolar(p);
            }
        }
        
        //cola de mayor prioridad disponible
        if (!colaPrioridad1.estaVacia()) {
            return colaPrioridad1.desencolar();
        } else if (!colaPrioridad2.estaVacia()) {
            return colaPrioridad2.desencolar();
        } else if (!colaPrioridad3.estaVacia()) {
            return colaPrioridad3.desencolar();
        }
        
        return null;
    }
    
    public void agregarProceso(Proceso proceso, Cola<Proceso> colaListos) {
        int prioridad = proceso.getPcb().getPrioridad();
        
        if (prioridad <= 1) {
            colaPrioridad1.encolar(proceso);
        } else if (prioridad <= 3) {
            colaPrioridad2.encolar(proceso);
        } else {
            colaPrioridad3.encolar(proceso);
        }
    }
    
    public void devolverColasACola(Cola<Proceso> colaListos) {
        Lista<Proceso> p1 = colaPrioridad1.obtenerTodos();
        Lista<Proceso> p2 = colaPrioridad2.obtenerTodos();
        Lista<Proceso> p3 = colaPrioridad3.obtenerTodos();
        
        for (int i = 0; i < p1.tamaño(); i++) {
            colaListos.encolar(p1.obtener(i));
        }
        for (int i = 0; i < p2.tamaño(); i++) {
            colaListos.encolar(p2.obtener(i));
        }
        for (int i = 0; i < p3.tamaño(); i++) {
            colaListos.encolar(p3.obtener(i));
        }
    }

    public String getNombre() {
        return "Colas Multinivel";
    }
    
    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }
    
    public int getQuantum() {
        return quantum;
    }
    
    public void reiniciar() {
        colaPrioridad1.limpiar();
        colaPrioridad2.limpiar();
        colaPrioridad3.limpiar();
    }
}