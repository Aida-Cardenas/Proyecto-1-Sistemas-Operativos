package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import modelo.Proceso;

public class DialogoAgregarProceso extends JDialog {
   /**
    * DialogoAgregarProceso - Wizard para crear procesos
    * 
    * Permite definir nombre, cantidad de instrucciones, tipo (CPU/I-O bound),
    * patrón y duración de I/O y prioridad. Devuelve un Proceso listo para
    * agregar al simulador.
    */
   private JTextField txtNombre;
   private JSpinner spinnerInstrucciones;
   private JRadioButton rbCPUBound;
   private JRadioButton rbIOBound;
   private JSpinner spinnerCiclosExcepcion;
   private JSpinner spinnerCiclosCompletarExcepcion;
   private JSpinner spinnerPrioridad;
   private boolean confirmado = false;
   private Proceso proceso;

   public DialogoAgregarProceso(Frame parent) {
      super(parent, "Agregar Proceso", true);
      this.initComponents();
      this.pack();
      this.setLocationRelativeTo(parent);
   }

   private void initComponents() {
      this.setLayout(new BorderLayout(10, 10));
      JPanel panelCentral = new JPanel(new GridBagLayout());
      panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(5, 5, 5, 5);
      gbc.fill = 2;
      gbc.gridx = 0;
      gbc.gridy = 0;
      panelCentral.add(new JLabel("Nombre:"), gbc);
      gbc.gridx = 1;
      this.txtNombre = new JTextField(15);
      panelCentral.add(this.txtNombre, gbc);
      gbc.gridx = 0;
      gbc.gridy = 1;
      panelCentral.add(new JLabel("Instrucciones:"), gbc);
      gbc.gridx = 1;
      this.spinnerInstrucciones = new JSpinner(new SpinnerNumberModel(20, 5, 1000, 5));
      panelCentral.add(this.spinnerInstrucciones, gbc);
      gbc.gridx = 0;
      gbc.gridy = 2;
      panelCentral.add(new JLabel("Tipo:"), gbc);
      gbc.gridx = 1;
      JPanel panelTipo = new JPanel(new FlowLayout(0));
      this.rbCPUBound = new JRadioButton("CPU Bound");
      this.rbIOBound = new JRadioButton("I/O Bound", true);
      ButtonGroup grupoBound = new ButtonGroup();
      grupoBound.add(this.rbCPUBound);
      grupoBound.add(this.rbIOBound);
      panelTipo.add(this.rbCPUBound);
      panelTipo.add(this.rbIOBound);
      panelCentral.add(panelTipo, gbc);
      gbc.gridx = 0;
      gbc.gridy = 3;
      panelCentral.add(new JLabel("Ciclos para Excepción:"), gbc);
      gbc.gridx = 1;
      this.spinnerCiclosExcepcion = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
      panelCentral.add(this.spinnerCiclosExcepcion, gbc);
      gbc.gridx = 0;
      gbc.gridy = 4;
      panelCentral.add(new JLabel("Ciclos para  I/O:"), gbc);
      gbc.gridx = 1;
      this.spinnerCiclosCompletarExcepcion = new JSpinner(new SpinnerNumberModel(3, 0, 100, 1));
      panelCentral.add(this.spinnerCiclosCompletarExcepcion, gbc);
      gbc.gridx = 0;
      gbc.gridy = 5;
      panelCentral.add(new JLabel("Prioridad (0=alta):"), gbc);
      gbc.gridx = 1;
      this.spinnerPrioridad = new JSpinner(new SpinnerNumberModel(2, 0, 10, 1));
      panelCentral.add(this.spinnerPrioridad, gbc);
      this.add(panelCentral, "Center");
      JPanel panelBotones = new JPanel(new FlowLayout(2));
      JButton btnAceptar = new JButton("Aceptar");
      btnAceptar.addActionListener((e) -> {
         this.aceptar();
      });
      panelBotones.add(btnAceptar);
      JButton btnCancelar = new JButton("Cancelar");
      btnCancelar.addActionListener((e) -> {
         this.cancelar();
      });
      panelBotones.add(btnCancelar);
      this.add(panelBotones, "South");
      this.rbCPUBound.addActionListener((e) -> {
         this.actualizarCamposIO();
      });
      this.rbIOBound.addActionListener((e) -> {
         this.actualizarCamposIO();
      });
   }

   private void actualizarCamposIO() {
      boolean esIOBound = this.rbIOBound.isSelected();
      this.spinnerCiclosExcepcion.setEnabled(esIOBound);
      this.spinnerCiclosCompletarExcepcion.setEnabled(esIOBound);
   }

   private void aceptar() {
      String nombre = this.txtNombre.getText().trim();
      if (nombre.isEmpty()) {
         JOptionPane.showMessageDialog(this, "Debe ingresar un nombre para el proceso", "Error", 0);
      } else {
         int numInstrucciones = (Integer)this.spinnerInstrucciones.getValue();
         boolean esCPUBound = this.rbCPUBound.isSelected();
         int ciclosExcepcion = (Integer)this.spinnerCiclosExcepcion.getValue();
         int ciclosCompletarExcepcion = (Integer)this.spinnerCiclosCompletarExcepcion.getValue();
         int prioridad = (Integer)this.spinnerPrioridad.getValue();
         if (esCPUBound) {
            ciclosExcepcion = 0;
            ciclosCompletarExcepcion = 0;
         }

         this.proceso = new Proceso(nombre, numInstrucciones, esCPUBound, ciclosExcepcion, ciclosCompletarExcepcion);
         this.proceso.getPcb().setPrioridad(prioridad);
         this.confirmado = true;
         this.dispose();
      }
   }

   private void cancelar() {
      this.confirmado = false;
      this.dispose();
   }

   public boolean isConfirmado() {
      return this.confirmado;
   }

   public Proceso getProceso() {
      return this.proceso;
   }
}