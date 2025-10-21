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
    private JLabel lblTiempoRetorno;
    private JLabel lblProcesosCompletados;
    private JLabel lblCiclosTotales;
    private JLabel lblEquidad;
    
    public PanelMetricas(Simulador simulador) {
        this.simulador = simulador;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new GridLayout(2, 4, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Fila 1: Métricas de rendimiento
        add(crearPanelMetrica("Throughput", "0.00 proc/seg", Color.BLUE));
        add(crearPanelMetrica("Utilización CPU", "0.00 %", Color.GREEN));
        add(crearPanelMetrica("T. Espera Prom.", "0.00 ms", Color.ORANGE));
        add(crearPanelMetrica("T. Respuesta Prom.", "0.00 ms", Color.MAGENTA));
        
        // Fila 2: Contadores y métricas adicionales
        add(crearPanelMetrica("T. Retorno Prom.", "0.00 ms", Color.CYAN));
        add(crearPanelMetrica("Proc. Completados", "0", Color.RED));
        add(crearPanelMetrica("Ciclos Totales", "0", Color.DARK_GRAY));
        add(crearPanelMetrica("Equidad", "0.00", new Color(128, 0, 128)));
        
        // Obtener referencias a los labels después de crear los paneles
        obtenerReferenciasLabels();
    }
    
    private JPanel crearPanelMetrica(String titulo, String valorInicial, Color colorValor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panel.setBackground(Color.WHITE);
        
        // Título de la métrica
        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 11));
        lblTitulo.setForeground(Color.DARK_GRAY);
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        // Valor de la métrica
        JLabel lblValor = new JLabel(valorInicial, SwingConstants.CENTER);
        lblValor.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblValor.setForeground(colorValor);
        lblValor.setName(titulo); // Para identificar el label después
        panel.add(lblValor, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void obtenerReferenciasLabels() {
        Component[] componentes = getComponents();
        for (Component comp : componentes) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                Component[] subComp = panel.getComponents();
                for (Component sub : subComp) {
                    if (sub instanceof JLabel) {
                        JLabel label = (JLabel) sub;
                        String nombre = label.getName();
                        if (nombre != null) {
                            asignarLabelPorNombre(nombre, label);
                        }
                    }
                }
            }
        }
    }
    
    private void asignarLabelPorNombre(String nombre, JLabel label) {
        switch (nombre) {
            case "Throughput":
                lblThroughput = label;
                break;
            case "Utilización CPU":
                lblUtilizacionCPU = label;
                break;
            case "T. Espera Prom.":
                lblTiempoEspera = label;
                break;
            case "T. Respuesta Prom.":
                lblTiempoRespuesta = label;
                break;
            case "T. Retorno Prom.":
                lblTiempoRetorno = label;
                break;
            case "Proc. Completados":
                lblProcesosCompletados = label;
                break;
            case "Ciclos Totales":
                lblCiclosTotales = label;
                break;
            case "Equidad":
                lblEquidad = label;
                break;
        }
    }
    
    public void actualizar() {
        if (simulador == null) return;
        
        Metricas metricas = simulador.getMetricas();
        
        // Actualizar throughput
        if (lblThroughput != null) {
            double throughput = metricas.calcularThroughput();
            lblThroughput.setText(String.format("%.4f proc/seg", throughput));
        }
        
        // Actualizar utilización CPU
        if (lblUtilizacionCPU != null) {
            double utilizacion = metricas.calcularUtilizacionCPU();
            lblUtilizacionCPU.setText(String.format("%.2f %%", utilizacion));
        }
        
        // Actualizar tiempo de espera promedio
        if (lblTiempoEspera != null) {
            double tiempoEspera = metricas.calcularTiempoEsperaPromedio();
            lblTiempoEspera.setText(String.format("%.2f ms", tiempoEspera));
        }
        
        // Actualizar tiempo de respuesta promedio
        if (lblTiempoRespuesta != null) {
            double tiempoRespuesta = metricas.calcularTiempoRespuestaPromedio();
            lblTiempoRespuesta.setText(String.format("%.2f ms", tiempoRespuesta));
        }
        
        // Actualizar tiempo de retorno promedio
        if (lblTiempoRetorno != null) {
            double tiempoRetorno = metricas.calcularTiempoRetornoPromedio();
            lblTiempoRetorno.setText(String.format("%.2f ms", tiempoRetorno));
        }
        
        // Actualizar procesos completados
        if (lblProcesosCompletados != null) {
            int completados = metricas.getProcesosCompletados();
            lblProcesosCompletados.setText(String.valueOf(completados));
        }
        
        // Actualizar ciclos totales
        if (lblCiclosTotales != null) {
            int ciclos = metricas.getCiclosTotales();
            lblCiclosTotales.setText(String.valueOf(ciclos));
        }
        
        // Actualizar equidad
        if (lblEquidad != null) {
            double equidad = metricas.calcularEquidad();
            lblEquidad.setText(String.format("%.4f", equidad));
        }
    }
    
    public void setSimulador(Simulador simulador) {
        this.simulador = simulador;
        if (simulador != null) {
            actualizar();
        }
    }
}