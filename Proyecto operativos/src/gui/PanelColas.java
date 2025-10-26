package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import estructuras.Cola;
import estructuras.Lista;
import modelo.Proceso;
import sistema.Simulador;
import planificacion.ColasMultinivel;
import planificacion.ColasMultinivelRetroalimentacion;
import planificacion.Prioridad;

public class PanelColas extends JPanel {
   /**
    * PanelColas - Vista de todas las colas del sistema
    * 
    * Muestra, en tiempo real, el contenido de:
    * - Listos
    * - Bloqueados (con progreso de I/O)
    * - Listos suspendidos
    * - Bloqueados suspendidos
    * - Terminados
    */
   private Simulador simulador;
   private JTextArea txtListos;
   private JTextArea txtBloqueados;
   private JTextArea txtListosSuspendidos;
   private JTextArea txtBloqueadosSuspendidos;
   private JTextArea txtTerminados;
   
   // Colas para algoritmos multinivel y prioridad
   private JPanel panelColasMultinivel;
   private JPanel panelCola1Individual;
   private JPanel panelCola2Individual;
   private JPanel panelCola3Individual;
   private JTextArea txtCola1;
   private JTextArea txtCola2;
   private JTextArea txtCola3;

   public PanelColas(Simulador simulador) {
      this.simulador = simulador;
      this.initComponents();
   }

   private void initComponents() {
      this.setLayout(new BorderLayout());
      this.setBorder(BorderFactory.createTitledBorder("Estado de las Colas"));
      
      // Panel principal para las colas estándar
      JPanel panelColasEstandar = new JPanel(new GridLayout(5, 1, 5, 5));
      JPanel panelListos = new JPanel(new BorderLayout());
      panelListos.setBorder(BorderFactory.createTitledBorder("Procesos Listos"));
      this.txtListos = new JTextArea(3, 20);
      this.txtListos.setEditable(false);
      this.txtListos.setFont(new Font("Monospaced", 0, 11));
      JScrollPane scrollListos = new JScrollPane(this.txtListos);
      panelListos.add(scrollListos, "Center");
      panelColasEstandar.add(panelListos);
      JPanel panelBloqueados = new JPanel(new BorderLayout());
      panelBloqueados.setBorder(BorderFactory.createTitledBorder("Procesos Bloqueados"));
      this.txtBloqueados = new JTextArea(3, 20);
      this.txtBloqueados.setEditable(false);
      this.txtBloqueados.setFont(new Font("Monospaced", 0, 11));
      JScrollPane scrollBloqueados = new JScrollPane(this.txtBloqueados);
      panelBloqueados.add(scrollBloqueados, "Center");
      panelColasEstandar.add(panelBloqueados);
      JPanel panelListosSusp = new JPanel(new BorderLayout());
      panelListosSusp.setBorder(BorderFactory.createTitledBorder("Listos Suspendidos"));
      this.txtListosSuspendidos = new JTextArea(2, 20);
      this.txtListosSuspendidos.setEditable(false);
      this.txtListosSuspendidos.setFont(new Font("Monospaced", 0, 11));
      JScrollPane scrollListosSusp = new JScrollPane(this.txtListosSuspendidos);
      panelListosSusp.add(scrollListosSusp, "Center");
      panelColasEstandar.add(panelListosSusp);
      JPanel panelBloqueadosSusp = new JPanel(new BorderLayout());
      panelBloqueadosSusp.setBorder(BorderFactory.createTitledBorder("Bloqueados Suspendidos"));
      this.txtBloqueadosSuspendidos = new JTextArea(2, 20);
      this.txtBloqueadosSuspendidos.setEditable(false);
      this.txtBloqueadosSuspendidos.setFont(new Font("Monospaced", 0, 11));
      JScrollPane scrollBloqueadosSusp = new JScrollPane(this.txtBloqueadosSuspendidos);
      panelBloqueadosSusp.add(scrollBloqueadosSusp, "Center");
      panelColasEstandar.add(panelBloqueadosSusp);
      JPanel panelTerminados = new JPanel(new BorderLayout());
      panelTerminados.setBorder(BorderFactory.createTitledBorder("Procesos Terminados"));
      this.txtTerminados = new JTextArea(3, 20);
      this.txtTerminados.setEditable(false);
      this.txtTerminados.setFont(new Font("Monospaced", 0, 11));
      JScrollPane scrollTerminados = new JScrollPane(this.txtTerminados);
      panelTerminados.add(scrollTerminados, "Center");
      panelColasEstandar.add(panelTerminados);
      
      // Panel para colas multinivel (inicialmente oculto)
      panelColasMultinivel = new JPanel(new GridLayout(1, 3, 5, 5));
      panelColasMultinivel.setBorder(BorderFactory.createTitledBorder("🎯 Backstage - Colas Internas del Planificador"));
      panelColasMultinivel.setVisible(false);
      
      // Cola 1 (Alta prioridad)
      panelCola1Individual = new JPanel(new BorderLayout());
      panelCola1Individual.setBorder(BorderFactory.createTitledBorder("Cola 1 (Alta Prioridad)"));
      this.txtCola1 = new JTextArea(4, 15);
      this.txtCola1.setEditable(false);
      this.txtCola1.setFont(new Font("Monospaced", Font.PLAIN, 10));
      JScrollPane scrollCola1 = new JScrollPane(this.txtCola1);
      panelCola1Individual.add(scrollCola1, BorderLayout.CENTER);
      panelColasMultinivel.add(panelCola1Individual);
      
      // Cola 2 (Media prioridad)
      panelCola2Individual = new JPanel(new BorderLayout());
      panelCola2Individual.setBorder(BorderFactory.createTitledBorder("Cola 2 (Media Prioridad)"));
      this.txtCola2 = new JTextArea(4, 15);
      this.txtCola2.setEditable(false);
      this.txtCola2.setFont(new Font("Monospaced", Font.PLAIN, 10));
      JScrollPane scrollCola2 = new JScrollPane(this.txtCola2);
      panelCola2Individual.add(scrollCola2, BorderLayout.CENTER);
      panelColasMultinivel.add(panelCola2Individual);
      
      // Cola 3 (Baja prioridad)
      panelCola3Individual = new JPanel(new BorderLayout());
      panelCola3Individual.setBorder(BorderFactory.createTitledBorder("Cola 3 (Baja Prioridad/FCFS)"));
      this.txtCola3 = new JTextArea(4, 15);
      this.txtCola3.setEditable(false);
      this.txtCola3.setFont(new Font("Monospaced", Font.PLAIN, 10));
      JScrollPane scrollCola3 = new JScrollPane(this.txtCola3);
      panelCola3Individual.add(scrollCola3, BorderLayout.CENTER);
      panelColasMultinivel.add(panelCola3Individual);
      
      // Agregar paneles al layout principal
      this.add(panelColasEstandar, BorderLayout.CENTER);
      this.add(panelColasMultinivel, BorderLayout.SOUTH);
   }

   public void actualizar() {
      // Verificar si estamos usando algoritmos con backstage
      boolean usandoMultinivel = simulador.getPlanificador() instanceof ColasMultinivel || 
                                simulador.getPlanificador() instanceof ColasMultinivelRetroalimentacion;
      boolean usandoPrioridad = simulador.getPlanificador() instanceof Prioridad;
      boolean mostrarBackstage = usandoMultinivel || usandoPrioridad;
      
      // Actualizar título del panel y subpaneles según el algoritmo
      if (usandoMultinivel) {
         panelColasMultinivel.setBorder(BorderFactory.createTitledBorder("🎯 Backstage - Colas Internas del Planificador"));
         panelCola1Individual.setBorder(BorderFactory.createTitledBorder("Cola 1 (Alta Prioridad)"));
         panelCola2Individual.setBorder(BorderFactory.createTitledBorder("Cola 2 (Media Prioridad)"));
         panelCola3Individual.setBorder(BorderFactory.createTitledBorder("Cola 3 (Baja Prioridad/FCFS)"));
      } else if (usandoPrioridad) {
         Prioridad planificadorPrioridad = (Prioridad) simulador.getPlanificador();
         String tipoPreemptive = planificadorPrioridad.esPreemptive() ? " ⚡Preemptive" : " 🔒Non-Preemptive";
         panelColasMultinivel.setBorder(BorderFactory.createTitledBorder("🎯 Backstage - Procesos Agrupados por Prioridad" + tipoPreemptive));
         panelCola1Individual.setBorder(BorderFactory.createTitledBorder("Prioridad Alta (0-1)"));
         panelCola2Individual.setBorder(BorderFactory.createTitledBorder("Prioridad Media (2-3)"));
         panelCola3Individual.setBorder(BorderFactory.createTitledBorder("Prioridad Baja (4+)"));
      }
      
      // Mostrar/ocultar panel de backstage
      panelColasMultinivel.setVisible(mostrarBackstage);
      
      // Actualizar colas estándar
      StringBuilder sbListos = new StringBuilder();
      Cola<Proceso> colaListos = this.simulador.getColaListos();
      Lista<Proceso> listaProcesos = colaListos.obtenerTodos();
      if (listaProcesos.tamaño() == 0) {
         sbListos.append("(Vacía)\n");
      } else {
         for(int i = 0; i < listaProcesos.tamaño(); ++i) {
            Proceso p = (Proceso)listaProcesos.obtener(i);
            sbListos.append(String.format("[%d] %s (PC:%d)\n", p.getPcb().getId(), p.getPcb().getNombre(), p.getPcb().getProgramCounter()));
         }
      }

      this.txtListos.setText(sbListos.toString());
      StringBuilder sbBloqueados = new StringBuilder();
      Cola<Proceso> colaBloqueados = this.simulador.getColaBloqueados();
      Lista<Proceso> listaBloqueados = colaBloqueados.obtenerTodos();
      if (listaBloqueados.tamaño() == 0) {
         sbBloqueados.append("(Vacía)\n");
      } else {
         for(int i = 0; i < listaBloqueados.tamaño(); ++i) {
            Proceso p = (Proceso)listaBloqueados.obtener(i);
            sbBloqueados.append(String.format("[%d] %s (I/O: %d/%d)\n", p.getPcb().getId(), p.getPcb().getNombre(), p.getCiclosEnExcepcion(), p.getCiclosParaCompletarExcepcion()));
         }
      }

      this.txtBloqueados.setText(sbBloqueados.toString());
      StringBuilder sbListosSusp = new StringBuilder();
      Cola<Proceso> colaListosSusp = this.simulador.getColaListosSuspendidos();
      Lista<Proceso> listaListosSusp = colaListosSusp.obtenerTodos();
      if (listaListosSusp.tamaño() == 0) {
         sbListosSusp.append("(Vacía)\n");
      } else {
         for(int i = 0; i < listaListosSusp.tamaño(); ++i) {
            Proceso p = (Proceso)listaListosSusp.obtener(i);
            sbListosSusp.append(String.format("[%d] %s\n", p.getPcb().getId(), p.getPcb().getNombre()));
         }
      }

      this.txtListosSuspendidos.setText(sbListosSusp.toString());
      StringBuilder sbBloqueadosSusp = new StringBuilder();
      Cola<Proceso> colaBloqueadosSusp = this.simulador.getColaBloqueadosSuspendidos();
      Lista<Proceso> listaBloqueadosSusp = colaBloqueadosSusp.obtenerTodos();
      if (listaBloqueadosSusp.tamaño() == 0) {
         sbBloqueadosSusp.append("(Vacía)\n");
      } else {
         for(int i = 0; i < listaBloqueadosSusp.tamaño(); ++i) {
            Proceso p = (Proceso)listaBloqueadosSusp.obtener(i);
            sbBloqueadosSusp.append(String.format("[%d] %s\n", p.getPcb().getId(), p.getPcb().getNombre()));
         }
      }

      this.txtBloqueadosSuspendidos.setText(sbBloqueadosSusp.toString());
      StringBuilder sbTerminados = new StringBuilder();
      Lista<Proceso> terminados = this.simulador.getProcesosTerminados();
      if (terminados.tamaño() == 0) {
         sbTerminados.append("(Ninguno)\n");
      } else {
         for(int i = 0; i < terminados.tamaño(); ++i) {
            Proceso p = (Proceso)terminados.obtener(i);
            sbTerminados.append(String.format("[%d] %s ✓\n", p.getPcb().getId(), p.getPcb().getNombre()));
         }
      }

      this.txtTerminados.setText(sbTerminados.toString());
      
      // Actualizar backstage si está en uso
      if (mostrarBackstage) {
         if (usandoMultinivel) {
            actualizarColasMultinivel();
         } else if (usandoPrioridad) {
            actualizarColasPrioridad();
         }
      }
   }

   private void actualizarColasMultinivel() {
      if (simulador.getPlanificador() instanceof ColasMultinivelRetroalimentacion) {
         ColasMultinivelRetroalimentacion cmr = (ColasMultinivelRetroalimentacion) simulador.getPlanificador();
         
         // Actualizar Cola 1
         StringBuilder sbCola1 = new StringBuilder();
         Lista<Proceso> lista1 = cmr.getCola1().obtenerTodos();
         if (lista1.tamaño() == 0) {
            sbCola1.append("(Vacía)\n");
         } else {
            for (int i = 0; i < lista1.tamaño(); i++) {
               Proceso p = lista1.obtener(i);
               sbCola1.append(String.format("[%d] %s\n(Nivel %d, Q=%d)\n", 
                  p.getPcb().getId(), 
                  p.getPcb().getNombre(),
                  cmr.getNivelProceso(p),
                  cmr.getQuantumParaNivel(1)));
            }
         }
         this.txtCola1.setText(sbCola1.toString());
         
         // Actualizar Cola 2
         StringBuilder sbCola2 = new StringBuilder();
         Lista<Proceso> lista2 = cmr.getCola2().obtenerTodos();
         if (lista2.tamaño() == 0) {
            sbCola2.append("(Vacía)\n");
         } else {
            for (int i = 0; i < lista2.tamaño(); i++) {
               Proceso p = lista2.obtener(i);
               sbCola2.append(String.format("[%d] %s\n(Nivel %d, Q=%d)\n", 
                  p.getPcb().getId(), 
                  p.getPcb().getNombre(),
                  cmr.getNivelProceso(p),
                  cmr.getQuantumParaNivel(2)));
            }
         }
         this.txtCola2.setText(sbCola2.toString());
         
         // Actualizar Cola 3
         StringBuilder sbCola3 = new StringBuilder();
         Lista<Proceso> lista3 = cmr.getCola3().obtenerTodos();
         if (lista3.tamaño() == 0) {
            sbCola3.append("(Vacía)\n");
         } else {
            for (int i = 0; i < lista3.tamaño(); i++) {
               Proceso p = lista3.obtener(i);
               sbCola3.append(String.format("[%d] %s\n(Nivel %d, FCFS)\n", 
                  p.getPcb().getId(), 
                  p.getPcb().getNombre(),
                  cmr.getNivelProceso(p)));
            }
         }
         this.txtCola3.setText(sbCola3.toString());
         
      } else if (simulador.getPlanificador() instanceof ColasMultinivel) {
         ColasMultinivel cm = (ColasMultinivel) simulador.getPlanificador();
         
         // Actualizar Cola Prioridad 1
         StringBuilder sbCola1 = new StringBuilder();
         Lista<Proceso> lista1 = cm.getColaPrioridad1().obtenerTodos();
         if (lista1.tamaño() == 0) {
            sbCola1.append("(Vacía)\n");
         } else {
            for (int i = 0; i < lista1.tamaño(); i++) {
               Proceso p = lista1.obtener(i);
               sbCola1.append(String.format("[%d] %s\n(Prio: %d)\n", 
                  p.getPcb().getId(), 
                  p.getPcb().getNombre(),
                  p.getPcb().getPrioridad()));
            }
         }
         this.txtCola1.setText(sbCola1.toString());
         
         // Actualizar Cola Prioridad 2
         StringBuilder sbCola2 = new StringBuilder();
         Lista<Proceso> lista2 = cm.getColaPrioridad2().obtenerTodos();
         if (lista2.tamaño() == 0) {
            sbCola2.append("(Vacía)\n");
         } else {
            for (int i = 0; i < lista2.tamaño(); i++) {
               Proceso p = lista2.obtener(i);
               sbCola2.append(String.format("[%d] %s\n(Prio: %d)\n", 
                  p.getPcb().getId(), 
                  p.getPcb().getNombre(),
                  p.getPcb().getPrioridad()));
            }
         }
         this.txtCola2.setText(sbCola2.toString());
         
         // Actualizar Cola Prioridad 3
         StringBuilder sbCola3 = new StringBuilder();
         Lista<Proceso> lista3 = cm.getColaPrioridad3().obtenerTodos();
         if (lista3.tamaño() == 0) {
            sbCola3.append("(Vacía)\n");
         } else {
            for (int i = 0; i < lista3.tamaño(); i++) {
               Proceso p = lista3.obtener(i);
               sbCola3.append(String.format("[%d] %s\n(Prio: %d)\n", 
                  p.getPcb().getId(), 
                  p.getPcb().getNombre(),
                  p.getPcb().getPrioridad()));
            }
         }
         this.txtCola3.setText(sbCola3.toString());
      }
   }
   
   private void actualizarColasPrioridad() {
      // Para algoritmos de prioridad, agrupamos los procesos de la cola de listos por prioridad
      Prioridad planificadorPrioridad = (Prioridad) simulador.getPlanificador();
      Lista<Proceso> todosLosListos = simulador.getColaListos().obtenerTodos();
      
      // Agrupar procesos por prioridad
      Lista<Proceso> prioridadAlta = new Lista<>();    // Prioridad 0-1
      Lista<Proceso> prioridadMedia = new Lista<>();   // Prioridad 2-3  
      Lista<Proceso> prioridadBaja = new Lista<>();    // Prioridad 4+
      
      for (int i = 0; i < todosLosListos.tamaño(); i++) {
         Proceso p = todosLosListos.obtener(i);
         int prioridad = p.getPcb().getPrioridad();
         
         if (prioridad <= 1) {
            prioridadAlta.agregar(p);
         } else if (prioridad <= 3) {
            prioridadMedia.agregar(p);
         } else {
            prioridadBaja.agregar(p);
         }
      }
      
      // Actualizar Cola 1 (Alta Prioridad)
      StringBuilder sbCola1 = new StringBuilder();
      if (prioridadAlta.tamaño() == 0) {
         sbCola1.append("(Vacía)\n");
      } else {
         for (int i = 0; i < prioridadAlta.tamaño(); i++) {
            Proceso p = prioridadAlta.obtener(i);
            sbCola1.append(String.format("[%d] %s\n(Prio: %d) %s\n", 
               p.getPcb().getId(), 
               p.getPcb().getNombre(),
               p.getPcb().getPrioridad(),
               planificadorPrioridad.esPreemptive() ? "⚡" : "🔒"));
         }
      }
      this.txtCola1.setText(sbCola1.toString());
      
      // Actualizar Cola 2 (Media Prioridad)
      StringBuilder sbCola2 = new StringBuilder();
      if (prioridadMedia.tamaño() == 0) {
         sbCola2.append("(Vacía)\n");
      } else {
         for (int i = 0; i < prioridadMedia.tamaño(); i++) {
            Proceso p = prioridadMedia.obtener(i);
            sbCola2.append(String.format("[%d] %s\n(Prio: %d) %s\n", 
               p.getPcb().getId(), 
               p.getPcb().getNombre(),
               p.getPcb().getPrioridad(),
               planificadorPrioridad.esPreemptive() ? "⚡" : "🔒"));
         }
      }
      this.txtCola2.setText(sbCola2.toString());
      
      // Actualizar Cola 3 (Baja Prioridad)
      StringBuilder sbCola3 = new StringBuilder();
      if (prioridadBaja.tamaño() == 0) {
         sbCola3.append("(Vacía)\n");
      } else {
         for (int i = 0; i < prioridadBaja.tamaño(); i++) {
            Proceso p = prioridadBaja.obtener(i);
            sbCola3.append(String.format("[%d] %s\n(Prio: %d) %s\n", 
               p.getPcb().getId(), 
               p.getPcb().getNombre(),
               p.getPcb().getPrioridad(),
               planificadorPrioridad.esPreemptive() ? "⚡" : "🔒"));
         }
      }
      this.txtCola3.setText(sbCola3.toString());
   }

   public void setSimulador(Simulador simulador) {
      this.simulador = simulador;
      this.actualizar();
   }
}
