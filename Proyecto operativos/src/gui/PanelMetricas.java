package gui;

import javax.swing.*;
import java.awt.*;
import sistema.*;

public class PanelMetricas extends JPanel {
    private Simulador simulador;
    private JLabel lblThroughput;
    private JLabel lblUtilizacionCPU;
    private JLabel lblTiempoEspera;
    private JLabel lblTiempoRespuesta;
    private JLabel lblProcesosCompletados;
    private JLabel lblCiclosTotales;
    
    public PanelMetricas(Simulador simulador) {
        this.simulador = simulador;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new GridLayout(2, 3, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel panelThroughput = crearPanelMetrica("Throughput", "0.00 proc/seg");
        lblThroughput = (JLabel) ((JPanel) panelThroughput.getComponent(1)).getComponent(0);
        add(panelThroughput);
        
        JPanel panelUtilizacion = crearPanelMetrica("Utilización CPU", "0.00 %");
        lblUtilizacionCPU = (JLabel) ((JPanel) panelUtilizacion.getComponent(1)).getComponent(0);
        add(panelUtilizacion);
        
        JPanel panelEspera = crearPanelMetrica("Tiempo Espera Promedio", "0.00 ms");
        lblTiempoEspera = (JLabel) ((JPanel) panelEspera.getComponent(1)).getComponent(0);
        add(panelEspera);
        
        JPanel panelRespuesta = crearPanelMetrica("Tiempo Respuesta Promedio", "0.00 ms");
        lblTiempoRespuesta = (JLabel) ((JPanel) panelRespuesta.getComponent(1)).getComponent(0);
        add(panelRespuesta);
        
        JPanel panelCompletados = crearPanelMetrica("Procesos Completados", "0");
        lblProcesosCompletados = (JLabel) ((JPanel) panelCompletados.getComponent(1)).getComponent(0);
        add(panelCompletados);
        
        JPanel panelCiclos = crearPanelMetrica("Ciclos Totales", "0");
        lblCiclosTotales = (JLabel) ((JPanel) panelCiclos.getComponent(1)).getComponent(0);
        add(panelCiclos);
    }
    
    private JPanel crearPanelMetrica(String titulo, String valorInicial) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        panel.setBackground(Color.WHITE);
        
        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 12));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        JPanel panelValor = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelValor.setBackground(Color.WHITE);
        JLabel lblValor = new JLabel(valorInicial);
        lblValor.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblValor.setForeground(new Color(0, 100, 200));
        panelValor.add(lblValor);
        panel.add(panelValor, BorderLayout.CENTER);
        
        return panel;
    }
    
    public void actualizar() {
        Metricas metricas = simulador.getMetricas();
        
        lblThroughput.setText(String.format("%.2f proc/seg", metricas.calcularThroughput()));
        lblUtilizacionCPU.setText(String.format("%.2f %%", metricas.calcularUtilizacionCPU()));
        lblTiempoEspera.setText(String.format("%.2f ms", metricas.calcularTiempoEsperaPromedio()));
        lblTiempoRespuesta.setText(String.format("%.2f ms", metricas.calcularTiempoRespuestaPromedio()));
        lblProcesosCompletados.setText(String.valueOf(metricas.getProcesosCompletados()));
        lblCiclosTotales.setText(String.valueOf(metricas.getCiclosTotales()));
    }
    
    public void setSimulador(Simulador simulador) {
        this.simulador = simulador;
        actualizar();
    }
}