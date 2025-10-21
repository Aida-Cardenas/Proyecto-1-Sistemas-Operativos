package sistema;

import estructuras.Cola;
import estructuras.Lista;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import modelo.EstadoProceso;
import modelo.PCB;
import modelo.Proceso;
import planificacion.ColasMultinivelRetroalimentacion;
import planificacion.FCFS;
import planificacion.Planificador;
import planificacion.RoundRobin;

/**
 * Simulador - Thread principal del sistema operativo simulado
 * 
 * Este es el corazón del simulador donde todo funciona junto.
 * Maneja todas las colas de procesos, ejecuta los ciclos de simulación
 * y coordina entre el planificador y la interfaz gráfica.
 * 
 * Básicamente simula lo que hace un sistema operativo real:
 * seleccionar procesos, ejecutarlos, manejar interrupciones
 * y cambiar entre modo SO y modo usuario.
 */
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

   // Método principal del thread donde corre toda la simulación
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

   // Ciclo de ejecución principal donde ocurre toda la magia
   private void ejecutarCiclo() {
      this.lock.lock();

      try {
         ++this.cicloGlobal;
         this.procesarExcepciones();
         
         if (this.procesoActual == null) {
            // Modo SO activado, buscando proceso para ejecutar
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
            // Ya hay un proceso ejecutándose, modo usuario
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
               this.registrarEvento("Proceso " + this.procesoActual.getPcb().getNombre() + " genera excepción I/O, BLOQUEADO");
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
                  this.registrarEvento("Proceso " + this.procesoActual.getPcb().getNombre() + " agotó quantum, vuelve a LISTO");
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

   // Procesamiento de excepciones I/O, revisar procesos bloqueados
   private void procesarExcepciones() {
      if (!this.colaBloqueados.estaVacia()) {
         Lista<Proceso> procesosADesbloquear = new Lista();
         Lista<Proceso> procesosBloqueados = this.colaBloqueados.obtenerTodos();

         int i;
         Proceso p;
         // Revisar cada proceso bloqueado para ver si completó su I/O
         for(i = 0; i < procesosBloqueados.tamaño(); ++i) {
            p = (Proceso)procesosBloqueados.obtener(i);
            if (p.procesarExcepcion()) {
               procesosADesbloquear.agregar(p);
               this.registrarEvento("Proceso " + p.getPcb().getNombre() + " completó excepción I/O");
            }
         }

         // Mover los procesos desbloqueados de vuelta a la cola de listos
         for(i = 0; i < procesosADesbloquear.tamaño(); ++i) {
            p = (Proceso)procesosADesbloquear.obtener(i);
            Lista<Proceso> temp = this.colaBloqueados.obtenerTodos();
            this.colaBloqueados.limpiar();

            for(int j = 0; j < temp.tamaño(); ++j) {
               if (temp.obtener(j) != p) {
                  this.colaBloqueados.encolar((Proceso)temp.obtener(j));
               }
            }

            p.getPcb().setEstado(EstadoProceso.LISTO);
            this.colaListos.encolar(p);
            this.registrarEvento("Proceso " + p.getPcb().getNombre() + " vuelve a LISTO");
         }
      }
   }

   // Agregar proceso nuevo al sistema
   public void agregarProceso(Proceso proceso) {
      this.lock.lock();

      try {
         proceso.getPcb().setEstado(EstadoProceso.LISTO);
         this.todosLosProcesos.agregar(proceso);
         this.planificador.agregarProceso(proceso, this.colaListos);
         String var10001 = proceso.getPcb().getNombre();
         this.registrarEvento("Proceso creado: " + var10001 + " con " + proceso.getNumeroInstrucciones() + " instrucciones");
      } finally {
         this.lock.unlock();
      }
   }

   // Suspender proceso, mover de cualquier cola a suspendidos
   public void suspenderProceso(Proceso proceso) {
      this.lock.lock();

      try {
         Lista temp;
         int i;
         if (proceso.getPcb().getEstado() == EstadoProceso.LISTO) {
            temp = this.colaListos.obtenerTodos();
            this.colaListos.limpiar();

            for(i = 0; i < temp.tamaño(); ++i) {
               if (temp.obtener(i) != proceso) {
                  this.colaListos.encolar((Proceso)temp.obtener(i));
               }
            }

            proceso.getPcb().setEstado(EstadoProceso.LISTO_SUSPENDIDO);
            this.colaListosSuspendidos.encolar(proceso);
            this.registrarEvento("Proceso " + proceso.getPcb().getNombre() + " suspendido desde LISTO");
         } else if (proceso.getPcb().getEstado() == EstadoProceso.BLOQUEADO) {
            temp = this.colaBloqueados.obtenerTodos();
            this.colaBloqueados.limpiar();

            for(i = 0; i < temp.tamaño(); ++i) {
               if (temp.obtener(i) != proceso) {
                  this.colaBloqueados.encolar((Proceso)temp.obtener(i));
               }
            }

            proceso.getPcb().setEstado(EstadoProceso.BLOQUEADO_SUSPENDIDO);
            this.colaBloqueadosSuspendidos.encolar(proceso);
            this.registrarEvento("Proceso " + proceso.getPcb().getNombre() + " suspendido desde BLOQUEADO");
         }
      } finally {
         this.lock.unlock();
      }
   }

   // Reanudar proceso, mover de suspendidos a su cola correspondiente
   public void reanudarProceso(Proceso proceso) {
      this.lock.lock();

      try {
         Lista temp;
         int i;
         if (proceso.getPcb().getEstado() == EstadoProceso.LISTO_SUSPENDIDO) {
            temp = this.colaListosSuspendidos.obtenerTodos();
            this.colaListosSuspendidos.limpiar();

            for(i = 0; i < temp.tamaño(); ++i) {
               if (temp.obtener(i) != proceso) {
                  this.colaListosSuspendidos.encolar((Proceso)temp.obtener(i));
               }
            }

            proceso.getPcb().setEstado(EstadoProceso.LISTO);
            this.colaListos.encolar(proceso);
            this.registrarEvento("Proceso " + proceso.getPcb().getNombre() + " reanudado a LISTO");
         } else if (proceso.getPcb().getEstado() == EstadoProceso.BLOQUEADO_SUSPENDIDO) {
            temp = this.colaBloqueadosSuspendidos.obtenerTodos();
            this.colaBloqueadosSuspendidos.limpiar();

            for(i = 0; i < temp.tamaño(); ++i) {
               if (temp.obtener(i) != proceso) {
                  this.colaBloqueadosSuspendidos.encolar((Proceso)temp.obtener(i));
               }
            }

            proceso.getPcb().setEstado(EstadoProceso.BLOQUEADO);
            this.colaBloqueados.encolar(proceso);
            this.registrarEvento("Proceso " + proceso.getPcb().getNombre() + " reanudado a BLOQUEADO");
         }
      } finally {
         this.lock.unlock();
      }
   }

   // Cambiar planificador dinámicamente
   public void cambiarPlanificador(Planificador nuevoPlanificador) {
      this.lock.lock();

      try {
         Lista<Proceso> procesosActuales = this.colaListos.obtenerTodos();
         this.planificador = nuevoPlanificador;
         this.colaListos.limpiar();
         int i = 0;

         while(true) {
            if (i >= procesosActuales.tamaño()) {
               this.registrarEvento("Planificador cambiado a: " + this.planificador.getNombre());
               if (this.listener != null) {
                  this.listener.onCambioPlanificador(this.planificador.getNombre());
               }
               break;
            }

            this.planificador.agregarProceso((Proceso)procesosActuales.obtener(i), this.colaListos);
            ++i;
         }
      } finally {
         this.lock.unlock();
      }
   }

   // Registrar eventos en el log
   private void registrarEvento(String evento) {
      String eventoCompleto = "[Ciclo " + this.cicloGlobal + "] " + evento;
      this.log.append(eventoCompleto).append("\n");
      if (this.listener != null) {
         this.listener.onNuevoEvento(eventoCompleto);
      }
   }

   // Getters para acceso a las colas y datos
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

   // Setters para configuración
   public void setDuracionCiclo(int milisegundos) {
      this.duracionCiclo = milisegundos;
   }

   public void setListener(SimuladorListener listener) {
      this.listener = listener;
   }

   // Métodos de control del thread para manejar play, pause, stop
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

   // Reiniciar simulación completa
   public void reiniciarSimulacion() {
      this.lock.lock();

      try {
         // Limpiar todas las colas
         this.colaListos.limpiar();
         this.colaBloqueados.limpiar();
         this.colaListosSuspendidos.limpiar();
         this.colaBloqueadosSuspendidos.limpiar();
         this.procesosTerminados = new Lista();
         this.todosLosProcesos = new Lista();
         
         // Reiniciar variables de control
         this.procesoActual = null;
         this.cicloGlobal = 0;
         
         // Reiniciar métricas y log
         this.metricas.reiniciar();
         this.log = new StringBuilder();
         
         // Reiniciar contador de PCB y planificador
         PCB.reiniciarContador();
         this.planificador.reiniciar();
         
         this.registrarEvento("=== SIMULACIÓN REINICIADA ===");
      } finally {
         this.lock.unlock();
      }
   }
}