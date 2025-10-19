package sistema;

import estructuras.Cola;
import estructuras.Lista;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import modelo.EstadoProceso;
import modelo.Proceso;
import planificacion.ColasMultinivelRetroalimentacion;
import planificacion.FCFS;
import planificacion.Planificador;
import planificacion.RoundRobin;

public class Simulador extends Thread {
   // Variables de estado de las colas
   private Cola<Proceso> colaListos = new Cola();
   private Cola<Proceso> colaBloqueados = new Cola();
   private Cola<Proceso> colaListosSuspendidos = new Cola();
   private Cola<Proceso> colaBloqueadosSuspendidos = new Cola();
   private Lista<Proceso> procesosTerminados = new Lista();
   private Lista<Proceso> todosLosProcesos = new Lista();
   
   // Variables de control de ejecución
   private Proceso procesoActual = null;
   private Planificador planificador = new FCFS();
   private volatile boolean ejecutando = false;
   private volatile boolean pausado = false;
   private volatile boolean modoSO = false;
   
   // Variables de temporización
   private int duracionCiclo = 1000;
   private int cicloGlobal = 0;
   
   // Variables de métricas y sincronización
   private Metricas metricas = new Metricas();
   private Semaphore semaforoCPU = new Semaphore(1);
   private ReentrantLock lock = new ReentrantLock();
   private StringBuilder log = new StringBuilder();
   private SimuladorListener listener;

   // Constructor
   public Simulador() {
   }

   // Método principal del thread 
   public void run() {
      this.ejecutando = true;
      this.registrarEvento("=== SIMULADOR INICIADO ===");

      while(this.ejecutando) {
         if (this.pausado) {
            try {
               Thread.sleep(100L);
            } catch (InterruptedException var2) {
               var2.printStackTrace();
            }
         } else {
            try {
               this.ejecutarCiclo();
               Thread.sleep((long)this.duracionCiclo);
            } catch (InterruptedException var3) {
               var3.printStackTrace();
            }
         }
      }

      this.registrarEvento("=== SIMULADOR DETENIDO ===");
   }

   // Ciclo de ejecución principal
   private void ejecutarCiclo() {
      this.lock.lock();

      try {
         ++this.cicloGlobal;
         this.procesarExcepciones();
         
         if (this.procesoActual == null) {
            // Modo SO activado - buscando proceso para ejecutar
            this.modoSO = true;
            if (this.listener != null) {
               this.listener.onCambioModo(true);
            }

            this.procesoActual = this.planificador.seleccionarSiguienteProceso(this.colaListos);
            if (this.procesoActual != null) {
               this.procesoActual.getPcb().setEstado(EstadoProceso.EJECUCION);
               this.registrarEvento("CPU selecciona proceso: " + this.procesoActual.getPcb().getNombre());
               
               if (this.procesoActual.getPcb().esPrimerEjecucion()) {
                  long tiempoRespuesta = System.currentTimeMillis() - this.procesoActual.getPcb().getTiempoLlegada();
                  this.procesoActual.getPcb().setTiempoRespuesta(tiempoRespuesta);
                  this.procesoActual.getPcb().setPrimerEjecucion(false);
                  this.procesoActual.getPcb().setTiempoInicioEjecucion(System.currentTimeMillis());
               }

               this.modoSO = false;
               if (this.listener != null) {
                  this.listener.onCambioModo(false);
               }

               this.metricas.registrarCicloConCPU();
            } else {
               this.metricas.registrarCicloSinCPU();
            }
         } else {
            // Ya hay un proceso ejecutándose - modo usuario
            this.modoSO = false;
            if (this.listener != null) {
               this.listener.onCambioModo(false);
            }

            this.procesoActual.ejecutarCiclo();
            String var10001 = this.procesoActual.getPcb().getNombre();
            this.registrarEvento("Proceso " + var10001 + " ejecuta instrucción " + this.procesoActual.getPcb().getProgramCounter());
            this.metricas.registrarCicloConCPU();
            
            // Verificar si genera excepción de I/O
            if (this.procesoActual.debeGenerarExcepcion() && !this.procesoActual.isEnExcepcion()) {
               this.procesoActual.iniciarExcepcion();
               this.procesoActual.getPcb().setEstado(EstadoProceso.BLOQUEADO);
               this.colaBloqueados.encolar(this.procesoActual);
               this.registrarEvento("Proceso " + this.procesoActual.getPcb().getNombre() + " genera excepción I/O - BLOQUEADO");
               this.procesoActual = null;
               return;
            }

            // Verificar si el proceso terminó
            if (this.procesoActual.haTerminado()) {
               this.procesoActual.getPcb().setEstado(EstadoProceso.TERMINADO);
               this.procesoActual.getPcb().setTiempoFinalizacion(System.currentTimeMillis());
               this.procesosTerminados.agregar(this.procesoActual);
               this.metricas.registrarProcesoCompletado(this.procesoActual);
               this.registrarEvento("Proceso " + this.procesoActual.getPcb().getNombre() + " TERMINADO");
               this.procesoActual = null;
               return;
            }

            // Manejo especial según el planificador
            if (this.planificador instanceof RoundRobin) {
               RoundRobin rr = (RoundRobin)this.planificador;
               rr.consumirQuantum();
               if (rr.seAcaboQuantum()) {
                  this.procesoActual.getPcb().setEstado(EstadoProceso.LISTO);
                  this.colaListos.encolar(this.procesoActual);
                  this.registrarEvento("Proceso " + this.procesoActual.getPcb().getNombre() + " agotó quantum - vuelve a LISTO");
                  this.procesoActual = null;
               }
            } else if (this.planificador instanceof ColasMultinivelRetroalimentacion) {
               ColasMultinivelRetroalimentacion cmr = (ColasMultinivelRetroalimentacion)this.planificador;
               int nivel = cmr.getNivelProceso(this.procesoActual);
               int quantum = cmr.getQuantumParaNivel(nivel);
               if (this.procesoActual.getCiclosEjecutados() % quantum == 0 && this.procesoActual.getCiclosEjecutados() > 0) {
                  cmr.degradarProceso(this.procesoActual);
                  this.procesoActual.getPcb().setEstado(EstadoProceso.LISTO);
                  this.colaListos.encolar(this.procesoActual);
                  var10001 = this.procesoActual.getPcb().getNombre();
                  this.registrarEvento("Proceso " + var10001 + " degradado a nivel " + cmr.getNivelProceso(this.procesoActual));
                  this.procesoActual = null;
               }
            }
         }

         if (this.listener != null) {
            this.listener.onActualizacion();
         }

      } finally {
         this.lock.unlock();
      }
   }

   private void procesarExcepciones() {
   }

   public void agregarProceso(Proceso proceso) {
   }

   public void suspenderProceso(Proceso proceso) {
   }

   public void reanudarProceso(Proceso proceso) {
   }

   public void cambiarPlanificador(Planificador nuevoPlanificador) {
   }

   private void registrarEvento(String evento) {
   }

   // Getters
   public Cola<Proceso> getColaListos() {
      return this.colaListos;
   }

   public Cola<Proceso> getColaBloqueados() {
      return this.colaBloqueados;
   }

   public Cola<Proceso> getColaListosSuspendidos() {
      return this.colaListosSuspendidos;
   }

   public Cola<Proceso> getColaBloqueadosSuspendidos() {
      return this.colaBloqueadosSuspendidos;
   }

   public Lista<Proceso> getProcesosTerminados() {
      return this.procesosTerminados;
   }

   public Lista<Proceso> getTodosLosProcesos() {
      return this.todosLosProcesos;
   }

   public Proceso getProcesoActual() {
      return this.procesoActual;
   }

   public Planificador getPlanificador() {
      return this.planificador;
   }

   public int getCicloGlobal() {
      return this.cicloGlobal;
   }

   public Metricas getMetricas() {
      return this.metricas;
   }

   public boolean isModoSO() {
      return this.modoSO;
   }

   public String getLog() {
      return this.log.toString();
   }

   public int getDuracionCiclo() {
      return this.duracionCiclo;
   }

   // Setters
   public void setDuracionCiclo(int milisegundos) {
      this.duracionCiclo = milisegundos;
   }

   public void setListener(SimuladorListener listener) {
      this.listener = listener;
   }

   // Métodos de control del thread - Para manejar play,pause,stop
   public void pausar() {
      this.pausado = true;
   }

   public void reanudar() {
      this.pausado = false;
   }

   public void detener() {
      this.ejecutando = false;
   }

   public boolean isEjecutando() {
      return this.ejecutando && !this.pausado;
   }

   public void reiniciarSimulacion() {
   }
}
