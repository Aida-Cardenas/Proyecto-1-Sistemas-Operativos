package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import estructuras.Lista;
import modelo.PCB;
import modelo.Proceso;
import sistema.Simulador;

public class PanelPCB extends JPanel {
    /**
     * PanelPCB - Resumen de PCBs y proceso en CPU
     * 
     * Arriba: muestra el proceso actual en CPU con PC y MAR.
     * Abajo: una tabla con los registros y estado de todos los procesos.
     */
    private Simulador simulador;
    private JTable tablaPCB;
    private DefaultTableModel modeloTabla;
    private JLabel lblProcesoEnCPU;
    
    public PanelPCB(Simulador simulador) {
        this.simulador = simulador;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Información de PCBs"));

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createTitledBorder("Proceso en CPU"));
        lblProcesoEnCPU = new JLabel("Ninguno", SwingConstants.CENTER);
        lblProcesoEnCPU.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblProcesoEnCPU.setForeground(Color.RED);
        lblProcesoEnCPU.setPreferredSize(new Dimension(0, 60));
        panelSuperior.add(lblProcesoEnCPU, BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);
        
        String[] columnas = {"ID", "Nombre", "Estado", "PC", "MAR", "Prioridad"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaPCB = new JTable(modeloTabla);
        tablaPCB.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tablaPCB.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 12));
        tablaPCB.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(tablaPCB);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public void actualizar() {
        Proceso actual = simulador.getProcesoActual();
        if (actual != null) {
            lblProcesoEnCPU.setText(String.format("<html><center>%s<br/>PC: %d | MAR: %d</center></html>",
                actual.getPcb().getNombre(),
                actual.getPcb().getProgramCounter(),
                actual.getPcb().getMemoryAddressRegister()));
            lblProcesoEnCPU.setForeground(new Color(0, 150, 0));
        } else {
            lblProcesoEnCPU.setText("CPU Inactivo");
            lblProcesoEnCPU.setForeground(Color.RED);
        }
        
        modeloTabla.setRowCount(0);
        
        Lista<Proceso> todosLosProcesos = simulador.getTodosLosProcesos();
        
        for (int i = 0; i < todosLosProcesos.tamaño(); i++) {
            Proceso p = todosLosProcesos.obtener(i);
            PCB pcb = p.getPcb();
            
            Object[] fila = {
                pcb.getId(),
                pcb.getNombre(),
                pcb.getEstado().toString(),
                pcb.getProgramCounter(),
                pcb.getMemoryAddressRegister(),
                pcb.getPrioridad()
            };
            
            modeloTabla.addRow(fila);
        }
    }
    
    public void setSimulador(Simulador simulador) {
        this.simulador = simulador;
        actualizar();
    }
}