package sistema;

import estructuras.Cola;
import estructuras.Lista;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import modelo.EstadoProceso;
import modelo.PCB;
import modelo.Proceso;
import planificacion.ColasMultinivel;
import planificacion.ColasMultinivelRetroalimentacion;
import planificacion.FCFS;
import planificacion.Planificador;
import planificacion.Prioridad;
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
   private Cola<Proceso> colaListos = new Cola();
   private Cola<Proceso> colaBloqueados = new Cola();
   private Cola<Proceso> colaListosSuspendidos = new Cola();
   private Cola<Proceso> colaBloqueadosSuspendidos = new Cola();
   private Lista<Proceso> procesosTerminados = new Lista();
   private Lista<Proceso> todosLosProcesos = new Lista();
   
   private Proceso procesoActual = null;
   private Planificador planificador = new FCFS();
   private volatile boolean ejecutando = false;
   private volatile boolean pausado = false;
   private volatile boolean modoSO = true;  
   
   private int duracionCiclo = 1000;
   private int cicloGlobal = 0;
   
   // Gestión de recursos limitados
   private final int LIMITE_MEMORIA = 10; // Máximo 10 procesos en memoria activa
   
   private Metricas metricas = new Metricas();
   private Semaphore semaforoCPU = new Semaphore(1);
   private ReentrantLock lock = new ReentrantLock();
   private StringBuilder log = new StringBuilder();
   private SimuladorListener listener;

   public Simulador() {
   }

   public void run() {
      this.ejecutando = true;
      this.modoSO = true;  
      if (this.listener != null) {
         this.listener.onCambioModo(true);
      }
      this.registrarEvento("SIMULADOR INICIADO");

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

      // Mostrar estadísticas finales
      if (!this.ejecutando) {
         mostrarEstadisticasFinales();
      }
      
      this.registrarEvento("SIMULADOR DETENIDO");
   }

   private void ejecutarCiclo() {
      try {
         semaforoCPU.acquire(); // Adquirir acceso exclusivo a la CPU
         this.lock.lock();

         ++this.cicloGlobal;
         
         this.procesarExcepciones();
         
         // Verificar periódicamente gestión de recursos (cada 10 ciclos)
         if (this.cicloGlobal % 10 == 0) {
            this.gestionarRecursos();
         }
         
         if (this.procesoActual == null) {
            this.modoSO = true;
            if (this.listener != null) {
               this.listener.onCambioModo(true);
               this.listener.onActualizacion();  // ✅ Actualizar UI
            }

            try {
               Thread.sleep(50);
            } catch (InterruptedException e) {
               e.printStackTrace();
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
               // Verificar si realmente no hay más trabajo que hacer
               if (todoElTrabajoCompletado()) {
                  this.registrarEvento("🎉 SIMULACIÓN COMPLETADA AUTOMÁTICAMENTE - Todos los procesos terminados");
                  this.ejecutando = false;
                  
                  // Notificar a la interfaz que la simulación terminó
                  if (this.listener != null) {
                     this.listener.onActualizacion();
                  }
                  return;
               } else {
                  this.registrarEvento("CPU Inactivo - Cola de listos vacía");
                  this.metricas.registrarCicloSinCPU();
               }
            }
         } else {
            this.modoSO = false;
            if (this.listener != null) {
               this.listener.onCambioModo(false);
            }

            this.procesoActual.ejecutarCiclo();
            this.registrarEvento("Proceso " + this.procesoActual.getPcb().getNombre() + 
                                " ejecuta instrucción " + this.procesoActual.getPcb().getProgramCounter());
            this.metricas.registrarCicloConCPU();
            
            // Verificar si genera excepción I/O
            if (this.procesoActual.debeGenerarExcepcion() && !this.procesoActual.isEnExcepcion()) {
               this.modoSO = true;
               if (this.listener != null) {
                  this.listener.onCambioModo(true);
                  this.listener.onActualizacion();
               }
               
               this.procesoActual.iniciarExcepcion();
               this.procesoActual.getPcb().setEstado(EstadoProceso.BLOQUEADO);
               this.colaBloqueados.encolar(this.procesoActual);
               this.registrarEvento("Proceso " + this.procesoActual.getPcb().getNombre() + 
                                   " genera excepción I/O, BLOQUEADO");
               
               // Crear y iniciar thread independiente para manejar I/O
               ExceptionThread ioThread = new ExceptionThread(this.procesoActual, this);
               ioThread.start();
               this.registrarEvento("SISTEMA: Iniciado Thread I/O para " + this.procesoActual.getPcb().getNombre());
               
               this.procesoActual = null;
               return;
            }

            // Verificar si el proceso terminó
            if (this.procesoActual.haTerminado()) {
               this.modoSO = true;
               if (this.listener != null) {
                  this.listener.onCambioModo(true);
                  this.listener.onActualizacion();
               }
               
               this.procesoActual.getPcb().setEstado(EstadoProceso.TERMINADO);
               this.procesoActual.getPcb().setTiempoFinalizacion(System.currentTimeMillis());
               
               // Calcular tiempos de espera y respuesta correctamente
               calcularTiemposProceso(this.procesoActual);
               
               this.procesosTerminados.agregar(this.procesoActual);
               this.metricas.registrarProcesoCompletado(this.procesoActual);
               this.registrarEvento("Proceso " + this.procesoActual.getPcb().getNombre() + " TERMINADO" +
                                   " | Espera: " + this.procesoActual.getPcb().getTiempoEspera() + "ms" +
                                   " | Respuesta: " + this.procesoActual.getPcb().getTiempoRespuesta() + "ms");
               this.procesoActual = null;
               return;
            }

            // Verificar si el planificador quiere desalojar el proceso actual
            if (this.planificador.debeDesalojar(this.procesoActual, this.colaListos)) {
               this.modoSO = true;
               if (this.listener != null) {
                  this.listener.onCambioModo(true);
                  this.listener.onActualizacion();
               }
               
               this.procesoActual.getPcb().setEstado(EstadoProceso.LISTO);
               this.colaListos.encolar(this.procesoActual);
               this.registrarEvento("Proceso " + this.procesoActual.getPcb().getNombre() + 
                                   " desalojado por planificador preemptive");
               this.procesoActual = null;
               return;
            }

            // Manejo especial según el planificador
            if (this.planificador instanceof RoundRobin) {
               RoundRobin rr = (RoundRobin)this.planificador;
               rr.consumirQuantum();
               if (rr.seAcaboQuantum()) {
                  this.modoSO = true;
                  if (this.listener != null) {
                     this.listener.onCambioModo(true);
                     this.listener.onActualizacion();
                  }
                  
                  this.procesoActual.getPcb().setEstado(EstadoProceso.LISTO);
                  this.colaListos.encolar(this.procesoActual);
                  this.registrarEvento("Proceso " + this.procesoActual.getPcb().getNombre() + 
                                      " agotó quantum, vuelve a LISTO");
                  this.procesoActual = null;
               }
            } else if (this.planificador instanceof ColasMultinivelRetroalimentacion) {
               ColasMultinivelRetroalimentacion cmr = (ColasMultinivelRetroalimentacion)this.planificador;
               int nivel = cmr.getNivelProceso(this.procesoActual);
               int quantum = cmr.getQuantumParaNivel(nivel);
               if (this.procesoActual.getCiclosEjecutados() % quantum == 0 && 
                   this.procesoActual.getCiclosEjecutados() > 0) {
                  this.modoSO = true;
                  if (this.listener != null) {
                     this.listener.onCambioModo(true);
                     this.listener.onActualizacion();
                  }
                  
                  cmr.degradarProceso(this.procesoActual);
                  this.procesoActual.getPcb().setEstado(EstadoProceso.LISTO);
                  this.colaListos.encolar(this.procesoActual);
                  this.registrarEvento("Proceso " + this.procesoActual.getPcb().getNombre() + 
                                      " ahora es  " + cmr.getNivelProceso(this.procesoActual));
                  this.procesoActual = null;
               }
            }
         }

         if (this.listener != null) {
            this.listener.onActualizacion();
         }
      } catch (InterruptedException e) {
         this.registrarEvento("ERROR: Hilo interrumpido durante adquisición de CPU");
         e.printStackTrace();
      } finally {
         this.lock.unlock();
         semaforoCPU.release(); // Liberar acceso a la CPU
      }
   }

   private void procesarExcepciones() {
      // NOTA: Con la nueva implementación usando ExceptionThread,
      // este método se simplifica ya que los threads manejan automáticamente
      // el retorno de procesos de I/O a la cola de listos.
      
      // Solo verificamos si hay nuevos procesos que acaban de generar excepción
      // y necesitan crear su thread de I/O (esto se maneja en ejecutarCiclo)
   }

   public void agregarProceso(Proceso proceso) {
      this.lock.lock();

      try {
         proceso.getPcb().setEstado(EstadoProceso.LISTO);
         this.todosLosProcesos.agregar(proceso);
         
         // Gestión inteligente de memoria
         if (getNumeroProcesosenMemoria() >= LIMITE_MEMORIA) {
            
            // Verificar si el planificador actual considera prioridades
            boolean planificadorUsaPrioridades = (this.planificador instanceof Prioridad) ||
                                                 (this.planificador instanceof ColasMultinivel) ||
                                                 (this.planificador instanceof ColasMultinivelRetroalimentacion);
            
            if (planificadorUsaPrioridades) {
               // Gestión basada en prioridades
               Proceso procesoASuspender = encontrarProcesoMenorPrioridad();
               
               // Solo suspender si el nuevo proceso tiene MAYOR prioridad
               if (procesoASuspender != null && 
                   proceso.getPcb().getPrioridad() < procesoASuspender.getPcb().getPrioridad()) {
                  suspenderProceso(procesoASuspender);
                  this.registrarEvento("SISTEMA: Proceso Prio(" + procesoASuspender.getPcb().getPrioridad() + 
                                      ") suspendido para proceso Prio(" + proceso.getPcb().getPrioridad() + ")");
               } else if (procesoASuspender != null) {
                  // El nuevo proceso tiene menor o igual prioridad -> suspenderlo directamente
                  proceso.getPcb().setEstado(EstadoProceso.LISTO_SUSPENDIDO);
                  this.colaListosSuspendidos.encolar(proceso);
                  this.registrarEvento("SISTEMA: Proceso Prio(" + proceso.getPcb().getPrioridad() + 
                                      ") enviado a suspensión (memoria llena)");
                  return; // No agregar a memoria activa
               }
            } else {
               // Gestión FIFO para algoritmos que no usan prioridades (FCFS, SJF, RR)
               Proceso procesoASuspender = encontrarProcesoMenorPrioridad();
               if (procesoASuspender != null) {
                  suspenderProceso(procesoASuspender);
                  this.registrarEvento("SISTEMA: Proceso suspendido por límite de memoria (FIFO)");
               }
            }
         }
         
         this.planificador.agregarProceso(proceso, this.colaListos);
         this.registrarEvento("Proceso creado: " + proceso.getPcb().getNombre() + 
                            " con " + proceso.getNumeroInstrucciones() + " instrucciones");
                            
         // Verificar si hay procesos suspendidos que puedan reanudarse
         verificarReanudacionAutomatica();
      } finally {
         this.lock.unlock();
      }
   }

   public void suspenderProceso(Proceso proceso) {
      this.lock.lock();

      try {
         Lista temp;
         int i;
         boolean encontrado = false;
         
         if (proceso.getPcb().getEstado() == EstadoProceso.LISTO) {
            // Primero intentar remover de cola general de listos
            temp = this.colaListos.obtenerTodos();
            this.colaListos.limpiar();

            for(i = 0; i < temp.tamaño(); ++i) {
               if (temp.obtener(i) != proceso) {
                  this.colaListos.encolar((Proceso)temp.obtener(i));
               } else {
                  encontrado = true;
               }
            }
            
            // Si no se encontró en cola general, buscar en colas internas
            if (!encontrado) {
               encontrado = removerDeColasInternas(proceso);
            }
            
            if (encontrado) {
               proceso.getPcb().setEstado(EstadoProceso.LISTO_SUSPENDIDO);
               this.colaListosSuspendidos.encolar(proceso);
               this.registrarEvento("Proceso " + proceso.getPcb().getNombre() + " suspendido desde LISTO");
            } else {
               this.registrarEvento("ERROR: No se pudo encontrar proceso " + proceso.getPcb().getNombre() + " para suspender");
            }
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

   public void cambiarPlanificador(Planificador nuevoPlanificador) {
      this.lock.lock();

      try {
         Lista<Proceso> procesosActuales = this.colaListos.obtenerTodos();
         this.planificador = nuevoPlanificador;
         this.colaListos.limpiar();

         for(int i = 0; i < procesosActuales.tamaño(); ++i) {
            this.planificador.agregarProceso((Proceso)procesosActuales.obtener(i), this.colaListos);
         }
         
         this.registrarEvento("Planificador cambiado a: " + this.planificador.getNombre());
         if (this.listener != null) {
            this.listener.onCambioPlanificador(this.planificador.getNombre());
         }
      } finally {
         this.lock.unlock();
      }
   }

   private void registrarEvento(String evento) {
      String eventoCompleto = "[Ciclo " + this.cicloGlobal + "] " + evento;
      this.log.append(eventoCompleto).append("\n");
      if (this.listener != null) {
         this.listener.onNuevoEvento(eventoCompleto);
      }
   }

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

   public void setDuracionCiclo(int milisegundos) {
      this.duracionCiclo = milisegundos;
   }

   public void setListener(SimuladorListener listener) {
      this.listener = listener;
   }

   public void pausar() {
      this.pausado = true;
   }

   public void reanudar() {
      this.pausado = false;
   }

   public void detener() {
      this.ejecutando = false;
      this.registrarEvento("SIMULADOR DETENIDO MANUALMENTE");
   }

   public boolean isEjecutando() {
      return this.ejecutando && !this.pausado;
   }

   public void reiniciarSimulacion() {
      this.lock.lock();

      try {
         this.colaListos.limpiar();
         this.colaBloqueados.limpiar();
         this.colaListosSuspendidos.limpiar();
         this.colaBloqueadosSuspendidos.limpiar();
         this.procesosTerminados = new Lista();
         this.todosLosProcesos = new Lista();
         
         this.procesoActual = null;
         this.cicloGlobal = 0;
         this.modoSO = true;  
         
         this.metricas.reiniciar();
         this.log = new StringBuilder();
         
         PCB.reiniciarContador();
         this.planificador.reiniciar();
         
         this.registrarEvento(" SIMULACIÓN REINICIADA");
      } finally {
         this.lock.unlock();
      }
   }
   
   // Métodos auxiliares para gestión de recursos y suspensión automática
   
   /**
    * Cuenta procesos activos en memoria (listos + bloqueados + ejecutando)
    * Incluye colas internas de algoritmos multinivel
    */
   private int getNumeroProcesosenMemoria() {
      int contador = 0;
      
      // Procesos en cola general de listos
      contador += this.colaListos.tamaño();
      
      // Procesos en colas internas de algoritmos multinivel
      if (this.planificador instanceof ColasMultinivelRetroalimentacion) {
         ColasMultinivelRetroalimentacion cmr = (ColasMultinivelRetroalimentacion) this.planificador;
         contador += cmr.getCola1().tamaño();
         contador += cmr.getCola2().tamaño();
         contador += cmr.getCola3().tamaño();
      } else if (this.planificador instanceof ColasMultinivel) {
         ColasMultinivel cm = (ColasMultinivel) this.planificador;
         contador += cm.getColaPrioridad1().tamaño();
         contador += cm.getColaPrioridad2().tamaño();
         contador += cm.getColaPrioridad3().tamaño();
      }
      
      // Procesos bloqueados y proceso actual
      contador += this.colaBloqueados.tamaño();
      if (this.procesoActual != null) contador++;
      
      return contador;
   }
   
   /**
    * Encuentra el proceso con menor prioridad para suspender
    * Busca en todas las colas incluyendo las internas de algoritmos multinivel
    */
   private Proceso encontrarProcesoMenorPrioridad() {
      Proceso candidato = null;
      int menorPrioridad = Integer.MAX_VALUE;
      
      // Buscar en cola general de listos
      Lista<Proceso> listos = this.colaListos.obtenerTodos();
      for (int i = 0; i < listos.tamaño(); i++) {
         Proceso p = listos.obtener(i);
         if (p.getPcb().getPrioridad() < menorPrioridad) {
            menorPrioridad = p.getPcb().getPrioridad();
            candidato = p;
         }
      }
      
      // Buscar en colas internas de algoritmos multinivel
      if (this.planificador instanceof ColasMultinivelRetroalimentacion) {
         ColasMultinivelRetroalimentacion cmr = (ColasMultinivelRetroalimentacion) this.planificador;
         
         // Buscar en Cola 1
         Lista<Proceso> cola1 = cmr.getCola1().obtenerTodos();
         for (int i = 0; i < cola1.tamaño(); i++) {
            Proceso p = cola1.obtener(i);
            if (p.getPcb().getPrioridad() < menorPrioridad) {
               menorPrioridad = p.getPcb().getPrioridad();
               candidato = p;
            }
         }
         
         // Buscar en Cola 2
         Lista<Proceso> cola2 = cmr.getCola2().obtenerTodos();
         for (int i = 0; i < cola2.tamaño(); i++) {
            Proceso p = cola2.obtener(i);
            if (p.getPcb().getPrioridad() < menorPrioridad) {
               menorPrioridad = p.getPcb().getPrioridad();
               candidato = p;
            }
         }
         
         // Buscar en Cola 3
         Lista<Proceso> cola3 = cmr.getCola3().obtenerTodos();
         for (int i = 0; i < cola3.tamaño(); i++) {
            Proceso p = cola3.obtener(i);
            if (p.getPcb().getPrioridad() < menorPrioridad) {
               menorPrioridad = p.getPcb().getPrioridad();
               candidato = p;
            }
         }
         
      } else if (this.planificador instanceof ColasMultinivel) {
         ColasMultinivel cm = (ColasMultinivel) this.planificador;
         
         // Buscar en todas las colas de prioridad
         Lista<Proceso>[] colas = new Lista[] {
            cm.getColaPrioridad1().obtenerTodos(),
            cm.getColaPrioridad2().obtenerTodos(),
            cm.getColaPrioridad3().obtenerTodos()
         };
         
         for (Lista<Proceso> cola : colas) {
            for (int i = 0; i < cola.tamaño(); i++) {
               Proceso p = cola.obtener(i);
               if (p.getPcb().getPrioridad() < menorPrioridad) {
                  menorPrioridad = p.getPcb().getPrioridad();
                  candidato = p;
               }
            }
         }
      }
      
      // Buscar en cola de bloqueados
      Lista<Proceso> bloqueados = this.colaBloqueados.obtenerTodos();
      for (int i = 0; i < bloqueados.tamaño(); i++) {
         Proceso p = bloqueados.obtener(i);
         if (p.getPcb().getPrioridad() < menorPrioridad) {
            menorPrioridad = p.getPcb().getPrioridad();
            candidato = p;
         }
      }
      
      return candidato;
   }
   
   /**
    * Remueve un proceso de las colas internas de algoritmos multinivel
    * @param proceso El proceso a remover
    * @return true si se encontró y removió el proceso, false caso contrario
    */
   private boolean removerDeColasInternas(Proceso proceso) {
      if (this.planificador instanceof ColasMultinivelRetroalimentacion) {
         ColasMultinivelRetroalimentacion cmr = (ColasMultinivelRetroalimentacion) this.planificador;
         
         // Intentar remover de Cola 1
         if (removerDeCola(cmr.getCola1(), proceso)) return true;
         // Intentar remover de Cola 2
         if (removerDeCola(cmr.getCola2(), proceso)) return true;
         // Intentar remover de Cola 3
         if (removerDeCola(cmr.getCola3(), proceso)) return true;
         
      } else if (this.planificador instanceof ColasMultinivel) {
         ColasMultinivel cm = (ColasMultinivel) this.planificador;
         
         // Intentar remover de cada cola de prioridad
         if (removerDeCola(cm.getColaPrioridad1(), proceso)) return true;
         if (removerDeCola(cm.getColaPrioridad2(), proceso)) return true;
         if (removerDeCola(cm.getColaPrioridad3(), proceso)) return true;
      }
      
      return false;
   }
   
   /**
    * Método auxiliar para remover un proceso específico de una cola
    */
   private boolean removerDeCola(Cola<Proceso> cola, Proceso proceso) {
      Lista<Proceso> temp = cola.obtenerTodos();
      cola.limpiar();
      boolean encontrado = false;
      
      for (int i = 0; i < temp.tamaño(); i++) {
         if (temp.obtener(i) != proceso) {
            cola.encolar(temp.obtener(i));
         } else {
            encontrado = true;
         }
      }
      
      return encontrado;
   }
   
   /**
    * Encuentra el proceso suspendido con MAYOR prioridad para cargar a memoria
    * @return El proceso con mayor prioridad (menor número) en colas suspendidas
    */
   private Proceso encontrarProcesoMayorPrioridadSuspendido() {
      Proceso candidato = null;
      int mayorPrioridad = Integer.MAX_VALUE; // Menor número = mayor prioridad
      
      // Buscar en listos suspendidos
      Lista<Proceso> listosSuspendidos = this.colaListosSuspendidos.obtenerTodos();
      for (int i = 0; i < listosSuspendidos.tamaño(); i++) {
         Proceso p = listosSuspendidos.obtener(i);
         if (p.getPcb().getPrioridad() < mayorPrioridad) {
            mayorPrioridad = p.getPcb().getPrioridad();
            candidato = p;
         }
      }
      
      // Buscar en bloqueados suspendidos
      Lista<Proceso> bloqueadosSuspendidos = this.colaBloqueadosSuspendidos.obtenerTodos();
      for (int i = 0; i < bloqueadosSuspendidos.tamaño(); i++) {
         Proceso p = bloqueadosSuspendidos.obtener(i);
         if (p.getPcb().getPrioridad() < mayorPrioridad) {
            mayorPrioridad = p.getPcb().getPrioridad();
            candidato = p;
         }
      }
      
      return candidato;
   }
   
   /**
    * Verifica si hay espacio para reanudar procesos suspendidos
    * PRIORIZA los procesos de mayor prioridad para cargar a memoria
    */
   private void verificarReanudacionAutomatica() {
      if (getNumeroProcesosenMemoria() < LIMITE_MEMORIA && !this.colaListosSuspendidos.estaVacia()) {
         // Encontrar el proceso suspendido con MAYOR prioridad para reanudar
         Proceso procesoAReanudar = encontrarProcesoMayorPrioridadSuspendido();
         if (procesoAReanudar != null) {
            reanudarProceso(procesoAReanudar);
            this.registrarEvento("SISTEMA: Proceso de alta prioridad reanudado automáticamente (Prio: " + 
                                procesoAReanudar.getPcb().getPrioridad() + ")");
         }
      }
   }
   
   /**
    * Gestiona recursos del sistema periódicamente
    */
   private void gestionarRecursos() {
      int procesosEnMemoria = getNumeroProcesosenMemoria();
      
      // Si hay demasiados procesos en memoria, suspender algunos
      if (procesosEnMemoria > LIMITE_MEMORIA) {
         Proceso procesoASuspender = encontrarProcesoMenorPrioridad();
         if (procesoASuspender != null) {
            suspenderProceso(procesoASuspender);
            this.registrarEvento("SISTEMA: Gestión automática de recursos - proceso suspendido");
         }
      }
      
      // Si hay espacio, reanudar procesos suspendidos
      else if (procesosEnMemoria < LIMITE_MEMORIA) {
         verificarReanudacionAutomatica();
      }
   }
   
   /**
    * Verifica si todo el trabajo ha sido completado
    */
   private boolean todoElTrabajoCompletado() {
      // No hay trabajo si:
      // 1. No hay proceso ejecutándose
      // 2. No hay procesos en cola de listos
      // 3. No hay procesos bloqueados esperando I/O
      // 4. No hay procesos suspendidos esperando recursos
      // 5. Todos los procesos creados están terminados
      
      boolean sinProcesoActual = (this.procesoActual == null);
      boolean sinProcesosListos = this.colaListos.estaVacia();
      boolean sinProcesosBloqueados = this.colaBloqueados.estaVacia();
      boolean sinProcesosSuspendidos = this.colaListosSuspendidos.estaVacia() && 
                                      this.colaBloqueadosSuspendidos.estaVacia();
      
      // Verificación adicional: todos los procesos creados están terminados
      boolean todosTerminados = (this.todosLosProcesos.tamaño() > 0) && 
                               (this.procesosTerminados.tamaño() == this.todosLosProcesos.tamaño());
      
      return sinProcesoActual && sinProcesosListos && sinProcesosBloqueados && 
             sinProcesosSuspendidos && todosTerminados;
   }
   
   /**
    * Muestra estadísticas finales cuando termina la simulación
    */
   private void mostrarEstadisticasFinales() {
      this.registrarEvento("==================== ESTADÍSTICAS FINALES ====================");
      this.registrarEvento("Planificador usado: " + this.planificador.getNombre());
      this.registrarEvento("Ciclos totales ejecutados: " + this.cicloGlobal);
      this.registrarEvento("Procesos creados: " + this.todosLosProcesos.tamaño());
      this.registrarEvento("Procesos completados: " + this.procesosTerminados.tamaño());
      
      if (this.metricas != null) {
         this.registrarEvento("Utilización de CPU: " + 
                            String.format("%.2f%%", this.metricas.calcularUtilizacionCPU()));
         this.registrarEvento("Throughput: " + 
                            String.format("%.2f procesos/segundo", this.metricas.calcularThroughput()));
      }
      
      this.registrarEvento("============================================================");
   }
   
   /**
    * Calcula los tiempos de espera y respuesta de un proceso al terminar
    */
   private void calcularTiemposProceso(Proceso proceso) {
      PCB pcb = proceso.getPcb();
      
      // Tiempo total desde llegada hasta finalización
      long tiempoTotal = pcb.getTiempoFinalizacion() - pcb.getTiempoLlegada();
      
      // Tiempo teórico que DEBERÍA haber tomado ejecutar todas las instrucciones
      // (número de instrucciones * duración de ciclo)
      long tiempoEjecucionTeorico = proceso.getNumeroInstrucciones() * this.duracionCiclo;
      
      // Si el proceso tuvo I/O, agregar tiempo de bloqueo estimado
      long tiempoIOEstimado = 0;
      if (proceso.esIOBound()) {
         // Calcular cuántas excepciones I/O debería haber tenido
         int numeroExcepciones = proceso.getCiclosEjecutados() / proceso.getCiclosParaExcepcion();
         tiempoIOEstimado = numeroExcepciones * proceso.getCiclosParaCompletarExcepcion() * this.duracionCiclo;
      }
      
      // Tiempo de espera = Tiempo total - Tiempo ejecución - Tiempo I/O
      long tiempoEspera = tiempoTotal - tiempoEjecucionTeorico - tiempoIOEstimado;
      
      // El tiempo de espera nunca puede ser negativo
      pcb.setTiempoEspera(Math.max(0, tiempoEspera));
      
      // Tiempo de respuesta ya se calculó cuando empezó a ejecutar por primera vez
      if (pcb.getTiempoRespuesta() == -1) {
         long tiempoRespuesta = pcb.getTiempoInicioEjecucion() - pcb.getTiempoLlegada();
         pcb.setTiempoRespuesta(Math.max(0, tiempoRespuesta));
      }
      
      // Debug: mostrar cálculos en el log
      this.registrarEvento("DEBUG: Proceso " + proceso.getPcb().getNombre() + 
                          " | Total: " + tiempoTotal + "ms" +
                          " | Ejecución: " + tiempoEjecucionTeorico + "ms" + 
                          " | I/O: " + tiempoIOEstimado + "ms" +
                          " | Espera calculada: " + pcb.getTiempoEspera() + "ms");
   }
   
   /**
    * Thread independiente para manejar excepciones I/O
    * Cada excepción I/O se ejecuta en su propio Thread
    */
   private class ExceptionThread extends Thread {
      private Proceso proceso;
      private Simulador simulador;
      
      public ExceptionThread(Proceso proceso, Simulador simulador) {
         this.proceso = proceso;
         this.simulador = simulador;
         this.setName("IOThread-" + proceso.getPcb().getNombre());
      }
      
      @Override
      public void run() {
         try {
            // Simular tiempo de I/O real
            int tiempoIO = proceso.getCiclosParaCompletarExcepcion() * simulador.getDuracionCiclo();
            Thread.sleep(tiempoIO);
            
            // Regresar proceso a cola de listos
            simulador.lock.lock();
            try {
               // Remover de cola bloqueados
               Lista<Proceso> temp = simulador.colaBloqueados.obtenerTodos();
               simulador.colaBloqueados.limpiar();
               
               for (int i = 0; i < temp.tamaño(); i++) {
                  if (temp.obtener(i) != proceso) {
                     simulador.colaBloqueados.encolar(temp.obtener(i));
                  }
               }
               
               // Agregar a cola de listos
               proceso.getPcb().setEstado(EstadoProceso.LISTO);
               simulador.colaListos.encolar(proceso);
               simulador.registrarEvento("THREAD-IO: Proceso " + proceso.getPcb().getNombre() + 
                                        " completó I/O y regresa a LISTO");
               
            } finally {
               simulador.lock.unlock();
            }
            
         } catch (InterruptedException e) {
            simulador.registrarEvento("ERROR: Thread I/O interrumpido para " + proceso.getPcb().getNombre());
         }
      }
   }
}