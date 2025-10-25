package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import sistema.*;

public class PanelGraficas extends JPanel {
    private Simulador simulador;
    private List<Double> historialThroughput;
    private List<Double> historialUtilizacion;
    private List<Double> historialTiempoEspera;
    private List<Double> historialTiempoRespuesta;
    private int maxPuntos = 50;
    
    public PanelGraficas(Simulador simulador) {
        this.simulador = simulador;
        this.historialThroughput = new ArrayList<>();
        this.historialUtilizacion = new ArrayList<>();
        this.historialTiempoEspera = new ArrayList<>();
        this.historialTiempoRespuesta = new ArrayList<>();
        
        setPreferredSize(new Dimension(800, 400));
        setBorder(BorderFactory.createTitledBorder("Gráficas"));
        setBackground(Color.WHITE);
    }
    
    public void actualizar() {
        if (simulador == null) return;
        
        Metricas metricas = simulador.getMetricas();
        
        // agregar datos
        historialThroughput.add(metricas.calcularThroughput());
        historialUtilizacion.add(metricas.calcularUtilizacionCPU());
        historialTiempoEspera.add(metricas.calcularTiempoEsperaPromedio());
        historialTiempoRespuesta.add(metricas.calcularTiempoRespuestaPromedio());
        
        // mantener ultimos 50 puntos
        if (historialThroughput.size() > maxPuntos) {
            historialThroughput.remove(0);
            historialUtilizacion.remove(0);
            historialTiempoEspera.remove(0);
            historialTiempoRespuesta.remove(0);
        }
        
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int w = width / 2 - 20;
        int h = height / 2 - 40;
        
        // throughput
        dibujarGrafica(g2, 10, 30, w, h, historialThroughput, 
                      "Throughput (proc/seg)", Color.BLUE, 0, 0.1);
        
        // uso cpu
        dibujarGrafica(g2, width/2 + 10, 30, w, h, historialUtilizacion, 
                      "Utilización CPU (%)", Color.GREEN, 0, 100);
        
        // tiempo de espera prom
        dibujarGrafica(g2, 10, height/2 + 30, w, h, historialTiempoEspera, 
                      "Tiempo Espera Prom. (ms)", Color.ORANGE, 0, -1);
        
        // tiempo de espera prom
        dibujarGrafica(g2, width/2 + 10, height/2 + 30, w, h, historialTiempoRespuesta, 
                      "Tiempo Respuesta Prom. (ms)", Color.MAGENTA, 0, -1);
    }
    
    private void dibujarGrafica(Graphics2D g2, int x, int y, int width, int height,
                                List<Double> datos, String titulo, Color color,
                                double minY, double maxY) {
        g2.setColor(new Color(250, 250, 250));
        g2.fillRect(x, y, width, height);
        
        g2.setColor(Color.GRAY);
        g2.drawRect(x, y, width, height);
        
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int titleWidth = fm.stringWidth(titulo);
        g2.drawString(titulo, x + (width - titleWidth) / 2, y - 5);
        
        if (datos.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("Arial", Font.ITALIC, 11));
            String msg = "no hay datos";
            int msgWidth = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, x + (width - msgWidth) / 2, y + height / 2);
            return;
        }
        
   
        double max = maxY;
        if (maxY < 0) { 
            max = datos.stream().max(Double::compare).orElse(1.0);
            if (max == 0) max = 1.0;
        }
        double min = minY;
        
        g2.setColor(new Color(200, 200, 200));
        g2.drawLine(x + 5, y + height - 5, x + width - 5, y + height - 5);
        
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Arial", Font.PLAIN, 9));
        g2.drawString(String.format("%.2f", max), x + 2, y + 15);
        g2.drawString(String.format("%.2f", min), x + 2, y + height - 8);
        
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2));
        
        int numPuntos = datos.size();
        int margenX = 10;
        int margenY = 10;
        int areaWidth = width - 2 * margenX;
        int areaHeight = height - 2 * margenY;
        
        for (int i = 0; i < numPuntos - 1; i++) {
            double valor1 = datos.get(i);
            double valor2 = datos.get(i + 1);
            
            int x1 = x + margenX + (i * areaWidth / Math.max(1, numPuntos - 1));
            int y1 = y + margenY + (int)(areaHeight - ((valor1 - min) / (max - min) * areaHeight));
            
            int x2 = x + margenX + ((i + 1) * areaWidth / Math.max(1, numPuntos - 1));
            int y2 = y + margenY + (int)(areaHeight - ((valor2 - min) / (max - min) * areaHeight));
            
            g2.drawLine(x1, y1, x2, y2);
        }
        
        g2.setColor(color.darker());
        for (int i = 0; i < numPuntos; i++) {
            double valor = datos.get(i);
            int px = x + margenX + (i * areaWidth / Math.max(1, numPuntos - 1));
            int py = y + margenY + (int)(areaHeight - ((valor - min) / (max - min) * areaHeight));
            g2.fillOval(px - 3, py - 3, 6, 6);
        }
        
        if (!datos.isEmpty()) {
            double valorActual = datos.get(datos.size() - 1);
            g2.setColor(color);
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            String valorStr = String.format("%.2f", valorActual);
            g2.drawString(valorStr, x + width - 50, y + height - 10);
        }
    }
    
    public void setSimulador(Simulador simulador) {
        this.simulador = simulador;
        limpiar();
    }
    
    public void limpiar() {
        historialThroughput.clear();
        historialUtilizacion.clear();
        historialTiempoEspera.clear();
        historialTiempoRespuesta.clear();
        repaint();
    }
}