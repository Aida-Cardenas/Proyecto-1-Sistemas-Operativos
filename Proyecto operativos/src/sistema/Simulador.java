package sistema;

import estructuras.Cola;
import estructuras.Lista;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import modelo.Proceso;
import planificacion.FCFS;
import planificacion.Planificador;

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

   private void ejecutarCiclo() {
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
