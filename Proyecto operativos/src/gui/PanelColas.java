package gui;

import estructuras.Cola;
import estructuras.Lista;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import modelo.Proceso;
import sistema.Simulador;

public class PanelColas extends JPanel {
   private Simulador simulador;
   private JTextArea txtListos;
   private JTextArea txtBloqueados;
   private JTextArea txtListosSuspendidos;
   private JTextArea txtBloqueadosSuspendidos;
   private JTextArea txtTerminados;

   public PanelColas(Simulador simulador) {
      this.simulador = simulador;
      this.initComponents();
   }

   private void initComponents() {
      this.setLayout(new GridLayout(5, 1, 5, 5));
      this.setBorder(BorderFactory.createTitledBorder("Estado de las Colas"));
      JPanel panelListos = new JPanel(new BorderLayout());
      panelListos.setBorder(BorderFactory.createTitledBorder("Procesos Listos"));
      this.txtListos = new JTextArea(3, 20);
      this.txtListos.setEditable(false);
      this.txtListos.setFont(new Font("Monospaced", 0, 11));
      JScrollPane scrollListos = new JScrollPane(this.txtListos);
      panelListos.add(scrollListos, "Center");
      this.add(panelListos);
      JPanel panelBloqueados = new JPanel(new BorderLayout());
      panelBloqueados.setBorder(BorderFactory.createTitledBorder("Procesos Bloqueados"));
      this.txtBloqueados = new JTextArea(3, 20);
      this.txtBloqueados.setEditable(false);
      this.txtBloqueados.setFont(new Font("Monospaced", 0, 11));
      JScrollPane scrollBloqueados = new JScrollPane(this.txtBloqueados);
      panelBloqueados.add(scrollBloqueados, "Center");
      this.add(panelBloqueados);
      JPanel panelListosSusp = new JPanel(new BorderLayout());
      panelListosSusp.setBorder(BorderFactory.createTitledBorder("Listos Suspendidos"));
      this.txtListosSuspendidos = new JTextArea(2, 20);
      this.txtListosSuspendidos.setEditable(false);
      this.txtListosSuspendidos.setFont(new Font("Monospaced", 0, 11));
      JScrollPane scrollListosSusp = new JScrollPane(this.txtListosSuspendidos);
      panelListosSusp.add(scrollListosSusp, "Center");
      this.add(panelListosSusp);
      JPanel panelBloqueadosSusp = new JPanel(new BorderLayout());
      panelBloqueadosSusp.setBorder(BorderFactory.createTitledBorder("Bloqueados Suspendidos"));
      this.txtBloqueadosSuspendidos = new JTextArea(2, 20);
      this.txtBloqueadosSuspendidos.setEditable(false);
      this.txtBloqueadosSuspendidos.setFont(new Font("Monospaced", 0, 11));
      JScrollPane scrollBloqueadosSusp = new JScrollPane(this.txtBloqueadosSuspendidos);
      panelBloqueadosSusp.add(scrollBloqueadosSusp, "Center");
      this.add(panelBloqueadosSusp);
      JPanel panelTerminados = new JPanel(new BorderLayout());
      panelTerminados.setBorder(BorderFactory.createTitledBorder("Procesos Terminados"));
      this.txtTerminados = new JTextArea(3, 20);
      this.txtTerminados.setEditable(false);
      this.txtTerminados.setFont(new Font("Monospaced", 0, 11));
      JScrollPane scrollTerminados = new JScrollPane(this.txtTerminados);
      panelTerminados.add(scrollTerminados, "Center");
      this.add(panelTerminados);
   }

   public void actualizar() {
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
   }

   public void setSimulador(Simulador simulador) {
      this.simulador = simulador;
      this.actualizar();
   }
}
