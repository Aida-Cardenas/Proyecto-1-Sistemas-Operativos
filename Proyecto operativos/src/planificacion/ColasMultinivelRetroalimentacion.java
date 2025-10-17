package planificacion;

import estructuras.Cola;
import java.util.HashMap;
import java.util.Map;
import modelo.Proceso;

public class ColasMultinivelRetroalimentacion implements Planificador {
    private Cola<Proceso> cola1; // quantum = 2
    private Cola<Proceso> cola2; // quantum = 4
    private Cola<Proceso> cola3; // FCFS
    private Map<Integer, Integer> procesoACola; // tracking cual proceso en cola
    private int quantum1 = 2;
    private int quantum2 = 4;
    
    public ColasMultinivelRetroalimentacion() {
        this.cola1 = new Cola<>();
        this.cola2 = new Cola<>();
        this.cola3 = new Cola<>();
        this.procesoACola = new HashMap<>();
    }
    
    @Override
    public Proceso seleccionarSiguienteProceso(Cola<Proceso> colaListos) {
        // Mover procesos de colaListos a las colas multinivel
        while (!colaListos.estaVacia()) {
            Proceso p = colaListos.desencolar();
            int idProceso = p.getPcb().getId();
            
            if (!procesoACola.containsKey(idProceso)) {
                cola1.encolar(p);
                procesoACola.put(idProceso, 1);
            } else {
                int nivel = procesoACola.get(idProceso);
                if (nivel == 1) {
                    cola1.encolar(p);
                } else if (nivel == 2) {
                    cola2.encolar(p);
                } else {
                    cola3.encolar(p);
                }
            }
        }
        
        if (!cola1.estaVacia()) {
            return cola1.desencolar();
        } else if (!cola2.estaVacia()) {
            return cola2.desencolar();
        } else if (!cola3.estaVacia()) {
            return cola3.desencolar();
        }
        
        return null;
    }
    
    @Override
    public void agregarProceso(Proceso proceso, Cola<Proceso> colaListos) {
        int idProceso = proceso.getPcb().getId();
        
        if (!procesoACola.containsKey(idProceso)) {
            cola1.encolar(proceso);
            procesoACola.put(idProceso, 1);
        } else {
            int nivel = procesoACola.get(idProceso);
            if (nivel == 1) {
                cola1.encolar(proceso);
            } else if (nivel == 2) {
                cola2.encolar(proceso);
            } else {
                cola3.encolar(proceso);
            }
        }
    }
    
    public void degradarProceso(Proceso proceso) {
        int idProceso = proceso.getPcb().getId();
        int nivelActual = procesoACola.getOrDefault(idProceso, 1);
        
        if (nivelActual < 3) {
            procesoACola.put(idProceso, nivelActual + 1);
        }
    }
    
    public int getNivelProceso(Proceso proceso) {
        return procesoACola.getOrDefault(proceso.getPcb().getId(), 1);
    }
    
    public int getQuantumParaNivel(int nivel) {
        if (nivel == 1) return quantum1;
        if (nivel == 2) return quantum2;
        return Integer.MAX_VALUE; 
    }
    
    @Override
    public String getNombre() {
        return "Colas Multinivel con Retroalimentación";
    }
    
    @Override
    public void setQuantum(int quantum) {
        this.quantum1 = quantum;
        this.quantum2 = quantum * 2;
    }
    
    @Override
    public int getQuantum() {
        return quantum1;
    }
    
    @Override
    public void reiniciar() {
        cola1.limpiar();
        cola2.limpiar();
        cola3.limpiar();
        procesoACola.clear();
    }
}