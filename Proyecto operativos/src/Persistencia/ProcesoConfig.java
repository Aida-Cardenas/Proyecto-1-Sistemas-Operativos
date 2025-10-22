package persistencia;

import modelo.Proceso;

public class ProcesoConfig {
   public String nombre;
   public int numInstrucciones;
   public boolean esCPUBound;
   public int ciclosExcepcion;
   public int ciclosCompletarExcepcion;
   public int prioridad;

   public ProcesoConfig() {
   }

   public ProcesoConfig(String nombre, int numInstrucciones, boolean esCPUBound, int ciclosExcepcion, int ciclosCompletarExcepcion, int prioridad) {
      this.nombre = nombre;
      this.numInstrucciones = numInstrucciones;
      this.esCPUBound = esCPUBound;
      this.ciclosExcepcion = ciclosExcepcion;
      this.ciclosCompletarExcepcion = ciclosCompletarExcepcion;
      this.prioridad = prioridad;
   }

   public Proceso crearProceso() {
      Proceso p = new Proceso(this.nombre, this.numInstrucciones, this.esCPUBound, this.ciclosExcepcion, this.ciclosCompletarExcepcion);
      p.getPcb().setPrioridad(this.prioridad);
      return p;
   }
}