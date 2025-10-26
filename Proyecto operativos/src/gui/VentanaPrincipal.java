//ESTA ES LA CLASE MAIN NO CREEN OTRA PLS :)
package gui;

import javax.swing.*;
import java.awt.*;
import sistema.*;
import modelo.*;
import planificacion.*;
import persistencia.*;
import estructuras.*;

public class VentanaPrincipal extends JFrame implements SimuladorListener {
    private Simulador simulador;
    
    // componentes
    private JPanel panelSuperior;
    private JTabbedPane tabbedPane; 
    
    // controles
    private JButton btnIniciar;
    private JButton btnPausar;
    private JButton btnDetener;
    private JButton btnAgregarProceso;
    private JButton btnAgregar20Aleatorios;
    private JButton btnGuardarConfig;
    private JButton btnCargarConfig;
    private JComboBox<String> comboPlanificador;
    private JSpinner spinnerDuracionCiclo;
    private JLabel lblCicloGlobal;
    private JLabel lblModo;
    private JLabel lblProcesoActual;
    
    // paneles
    private PanelColas panelColas;
    private PanelPCB panelPCB;
    private PanelLog panelLog;
    private PanelMetricas panelMetricas;
    private PanelGraficas panelGraficas;  
    
    public VentanaPrincipal() {
        simulador = new Simulador();
        simulador.setListener(this);
        
        initComponents();
        cargarConfiguracionInicial();
    }
    
    private void initComponents() {
        setTitle("Simulador de Planificación de Procesos");
        setSize(1600, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        crearPanelSuperior();
        crearTabbedPane(); 
        
        add(panelSuperior, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER); 
    }
    
    private void crearPanelSuperior() {
        panelSuperior = new JPanel(new GridBagLayout());
        panelSuperior.setBorder(BorderFactory.createTitledBorder("Controles"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // FILA 1: Botones de control
        gbc.gridx = 0;
        gbc.gridy = 0;
        btnIniciar = new JButton("Iniciar Simulación");
        btnIniciar.addActionListener(e -> iniciarSimulacion());
        panelSuperior.add(btnIniciar, gbc);
        
        gbc.gridx = 1;
        btnPausar = new JButton("Pausar");
        btnPausar.setEnabled(false);
        btnPausar.addActionListener(e -> pausarSimulacion());
        panelSuperior.add(btnPausar, gbc);
        
        gbc.gridx = 2;
        btnDetener = new JButton("Detener");
        btnDetener.setEnabled(false);
        btnDetener.addActionListener(e -> detenerSimulacion());
        panelSuperior.add(btnDetener, gbc);
        
        gbc.gridx = 3;
        btnAgregarProceso = new JButton("Agregar Proceso");
        btnAgregarProceso.addActionListener(e -> mostrarDialogoAgregarProceso());
        panelSuperior.add(btnAgregarProceso, gbc);
        
        gbc.gridx = 4;
        btnAgregar20Aleatorios = new JButton("20 Aleatorios");
        btnAgregar20Aleatorios.setToolTipText("Agregar 20 procesos con configuración aleatoria");
        btnAgregar20Aleatorios.setFont(new Font("Arial", Font.BOLD, 12));
        btnAgregar20Aleatorios.setBackground(new Color(100, 200, 100));
        btnAgregar20Aleatorios.addActionListener(e -> agregar20ProcesosAleatorios());
        panelSuperior.add(btnAgregar20Aleatorios, gbc);
        
        // FILA 2: Configuración de planificador
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelSuperior.add(new JLabel("Planificador:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        String[] planificadores = {
            "FCFS",
            "SJF Non-Preemptive",
            "SJF Preemptive (SRTF)",
            "Round Robin",
            "Prioridad Non-Preemptive",
            "Prioridad Preemptive",
            "Colas Multinivel con Retroalimentación"
        };
        comboPlanificador = new JComboBox<>(planificadores);
        comboPlanificador.addActionListener(e -> cambiarPlanificador());
        panelSuperior.add(comboPlanificador, gbc);
        
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        panelSuperior.add(new JLabel("Duración Ciclo (ms):"), gbc);
        
        gbc.gridx = 4;
        spinnerDuracionCiclo = new JSpinner(new SpinnerNumberModel(1000, 100, 5000, 100));
        spinnerDuracionCiclo.addChangeListener(e -> cambiarDuracionCiclo());
        panelSuperior.add(spinnerDuracionCiclo, gbc);
        
        // FILA 3: Información de estado
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelSuperior.add(new JLabel("Ciclo Global:"), gbc);
        
        gbc.gridx = 1;
        lblCicloGlobal = new JLabel("0");
        lblCicloGlobal.setFont(new Font("Monospaced", Font.BOLD, 14));
        panelSuperior.add(lblCicloGlobal, gbc);
        
        gbc.gridx = 2;
        panelSuperior.add(new JLabel("Modo:"), gbc);
        
        gbc.gridx = 3;
        lblModo = new JLabel("SISTEMA OPERATIVO");
        lblModo.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblModo.setForeground(Color.BLUE);
        lblModo.setOpaque(true);
        lblModo.setBackground(new Color(200, 220, 255));
        lblModo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLUE, 3),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panelSuperior.add(lblModo, gbc);
        
        // FILA 4: Proceso actual
        gbc.gridx = 0;
        gbc.gridy = 3;
        panelSuperior.add(new JLabel("Proceso Actual:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        lblProcesoActual = new JLabel("Ninguno");
        lblProcesoActual.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblProcesoActual.setForeground(Color.RED);
        panelSuperior.add(lblProcesoActual, gbc);
        
        // FILA 5: Guardar/Cargar configuración
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        btnGuardarConfig = new JButton("Guardar Configuración");
        btnGuardarConfig.addActionListener(e -> guardarConfiguracion());
        panelSuperior.add(btnGuardarConfig, gbc);
        
        gbc.gridx = 1;
        btnCargarConfig = new JButton("Cargar Configuración");
        btnCargarConfig.addActionListener(e -> cargarConfiguracion());
        panelSuperior.add(btnCargarConfig, gbc);
    }
    
    // crear pestanas
  
    private void crearTabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 13));
        
        // PESTAÑA 1: Vista Principal (Colas, PCB, Log)
        JPanel panelPrincipal = crearPanelPrincipal();
        tabbedPane.addTab("Vista Principal", null, panelPrincipal, "Colas y Procesos");
        
        // PESTAÑA 2: Métricas
        JPanel panelMetricasTab = crearPanelMetricas();
        tabbedPane.addTab("Métricas", null, panelMetricasTab, "Metricas a tiempo real");
        
        // PESTAÑA 3: Gráficas
        JPanel panelGraficasTab = crearPanelGraficas();
        tabbedPane.addTab("Gráficas", null, panelGraficasTab, "Graficas a tiempo real");
    }
    
    private JPanel crearPanelPrincipal() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panelColas = new PanelColas(simulador);
        panel.add(panelColas);
        
        panelPCB = new PanelPCB(simulador);
        panel.add(panelPCB);
        
        panelLog = new PanelLog();
        panel.add(panelLog);
        
        return panel;
    }
    
    private JPanel crearPanelMetricas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Título
        JLabel titulo = new JLabel("METRICAS DEL SISTEMA", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(new Color(0, 100, 200));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        panel.add(titulo, BorderLayout.NORTH);
        
        // Panel de métricas
        panelMetricas = new PanelMetricas(simulador);
        panel.add(panelMetricas, BorderLayout.CENTER);
        
        // Descripción
        JTextArea descripcion = new JTextArea();
        descripcion.setText(
            "\n" +
            "DESCRIPCIÓN DE LAS MÉTRICAS:\n" +
            "\n\n" +
            "• Throughput: Procesos completados por segundo\n" +
            "• Utilización CPU: Porcentaje de tiempo que el CPU está ocupado\n" +
            "• T. Espera Prom.: Tiempo promedio que los procesos esperan en cola\n" +
            "• T. Respuesta Prom.: Tiempo promedio desde llegada hasta primera ejecución\n" +
            "• T. Retorno Prom.: Tiempo promedio total desde llegada hasta terminación\n" +
            "• Proc. Completados: Número total de procesos que han terminado\n" +
            "• Ciclos Totales: Número total de ciclos de simulación ejecutados\n" +
            "• Equidad: Equidad en la dist del CPU"
        );
        descripcion.setEditable(false);
        descripcion.setFont(new Font("Monospaced", Font.PLAIN, 11));
        descripcion.setBackground(new Color(250, 250, 250));
        descripcion.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Informacion"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        JScrollPane scrollDesc = new JScrollPane(descripcion);
        scrollDesc.setPreferredSize(new Dimension(0, 200));
        panel.add(scrollDesc, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel crearPanelGraficas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Título
        JLabel titulo = new JLabel("GRAFICAS DE RENDIMIENTO", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(new Color(200, 0, 100));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        panel.add(titulo, BorderLayout.NORTH);
        
        // Panel de gráficas
        panelGraficas = new PanelGraficas(simulador);
        panel.add(panelGraficas, BorderLayout.CENTER);
        
        // Panel de información
        JTextArea info = new JTextArea();
        info.setText(
            "\n" +
            "Se mantienen los últimos 50 puntos de datos para visualización.\n" +
            ""
        );
        info.setEditable(false);
        info.setFont(new Font("Monospaced", Font.PLAIN, 11));
        info.setBackground(new Color(250, 250, 250));
        info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(info, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void iniciarSimulacion() {
        if (!simulador.isAlive()) {
            simulador.start();
        } else {
            simulador.reanudar();
        }
        
        btnIniciar.setEnabled(false);
        btnPausar.setEnabled(true);
        btnDetener.setEnabled(true);
    }
    
    private void pausarSimulacion() {
        if (simulador.isEjecutando()) {
            simulador.pausar();
            btnPausar.setText("Reanudar");
        } else {
            simulador.reanudar();
            btnPausar.setText("Pausar");
        }
    }
    
    private void detenerSimulacion() {
        simulador.detener();
        btnIniciar.setEnabled(true);
        btnPausar.setEnabled(false);
        btnDetener.setEnabled(false);
        
        int opcion = JOptionPane.showConfirmDialog(this,
            "¿Desea reiniciar la simulación?",
            "Simulación Detenida",
            JOptionPane.YES_NO_OPTION);
        
        if (opcion == JOptionPane.YES_OPTION) {
            simulador = new Simulador();
            simulador.setListener(this);
            panelColas.setSimulador(simulador);
            panelPCB.setSimulador(simulador);
            panelMetricas.setSimulador(simulador);
            
            if (panelGraficas != null) {
                panelGraficas.setSimulador(simulador);
            }
            
            actualizarTodo();
        }
    }
    
    private void cambiarPlanificador() {
        String seleccion = (String) comboPlanificador.getSelectedItem();
        Planificador nuevoPlanificador = null;
        
        switch (seleccion) {
            case "FCFS":
                nuevoPlanificador = new FCFS();
                break;
            case "SJF Non-Preemptive":
                nuevoPlanificador = new SJF(false);
                break;
            case "SJF Preemptive (SRTF)":
                nuevoPlanificador = new SJF(true);
                break;
            case "Round Robin":
                String quantumStr = JOptionPane.showInputDialog(this, 
                    "Ingrese el quantum:", "3");
                if (quantumStr == null || quantumStr.trim().isEmpty()) {
                    return;
                }
                try {
                    int quantum = Integer.parseInt(quantumStr);
                    if (quantum < 1) {
                        JOptionPane.showMessageDialog(this, 
                            "El quantum debe ser mayor a 0", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    nuevoPlanificador = new RoundRobin(quantum);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Debe ingresar un número válido", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                break;
            case "Prioridad Non-Preemptive":
                nuevoPlanificador = new Prioridad(false);
                break;
            case "Prioridad Preemptive":
                nuevoPlanificador = new Prioridad(true);
                break;
            case "Colas Multinivel con Retroalimentación":
                String quantumMRStr = JOptionPane.showInputDialog(this, 
                    "Ingrese el quantum para Cola 1:", "2");
                if (quantumMRStr == null || quantumMRStr.trim().isEmpty()) {
                    return;
                }
                try {
                    int quantumMR = Integer.parseInt(quantumMRStr);
                    if (quantumMR < 1) {
                        JOptionPane.showMessageDialog(this, 
                            "El quantum debe ser mayor a 0", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    ColasMultinivelRetroalimentacion cmr = new ColasMultinivelRetroalimentacion();
                    cmr.setQuantum(quantumMR);
                    nuevoPlanificador = cmr;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Debe ingresar un número válido", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                break;
        }
        
        if (nuevoPlanificador != null) {
            simulador.cambiarPlanificador(nuevoPlanificador);
        }
    }
    
    private void cambiarDuracionCiclo() {
        int duracion = (Integer) spinnerDuracionCiclo.getValue();
        simulador.setDuracionCiclo(duracion);
    }
    
    private void mostrarDialogoAgregarProceso() {
        DialogoAgregarProceso dialogo = new DialogoAgregarProceso(this);
        dialogo.setVisible(true);
        
        if (dialogo.isConfirmado()) {
            Proceso proceso = dialogo.getProceso();
            simulador.agregarProceso(proceso);
            actualizarTodo();
        }
    }
    
    private void agregar20ProcesosAleatorios() {
        System.out.println(">>> 20 procesos aleatorios creados<<<");
        
        java.util.Random random = new java.util.Random();
        StringBuilder resumen = new StringBuilder();
        resumen.append("\n");
        resumen.append("GENERACIÓN DE 20 PROCESOS ALEATORIOS\n");
        resumen.append("\n\n");
        
        int cpuBoundCount = 0;
        int ioBoundCount = 0;
        
        for (int i = 1; i <= 20; i++) {
            String nombre = "Proc_" + (System.currentTimeMillis() % 10000) + "_" + i;
            boolean esCPUBound = random.nextBoolean();
            int numInstrucciones = 10 + random.nextInt(41);
            int prioridad = random.nextInt(11);
            
            int ciclosExcepcion = 0;
            int ciclosCompletarExcepcion = 0;
            
            if (!esCPUBound) {
                ciclosExcepcion = 3 + random.nextInt(6);
                ciclosCompletarExcepcion = 2 + random.nextInt(4);
            }
            
            Proceso proceso = new Proceso(nombre, numInstrucciones, esCPUBound, 
                                         ciclosExcepcion, ciclosCompletarExcepcion);
            proceso.getPcb().setPrioridad(prioridad);
            
            simulador.agregarProceso(proceso);
            
            String tipo = esCPUBound ? "CPU-Bound" : "I/O-Bound ";
            resumen.append(String.format("%2d. %-22s | %-11s | Instr: %2d | Prio: %2d", 
                i, nombre, tipo, numInstrucciones, prioridad));
            
            if (!esCPUBound) {
                resumen.append(String.format(" | I/O: cada %d ciclos, dur. %d", 
                    ciclosExcepcion, ciclosCompletarExcepcion));
            }
            resumen.append("\n");
            
            if (esCPUBound) {
                cpuBoundCount++;
            } else {
                ioBoundCount++;
            }
        }
        
        resumen.append("\n\n");
        resumen.append("                         ESTADÍSTICAS\n");
        resumen.append("\n");
        resumen.append(String.format("  Total de procesos:    20\n"));
        resumen.append(String.format("  CPU-Bound:            %d (%.0f%%)\n", 
            cpuBoundCount, (cpuBoundCount/20.0)*100));
        resumen.append(String.format("  I/O-Bound:            %d (%.0f%%)\n", 
            ioBoundCount, (ioBoundCount/20.0)*100));
        resumen.append("c: \n");
        
        actualizarTodo();
        
        JTextArea textArea = new JTextArea(resumen.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textArea.setCaretPosition(0);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(750, 550));
        
        JOptionPane.showMessageDialog(this, 
            scrollPane,
            "Se agregaron 20 procesos aleatorios",
            JOptionPane.INFORMATION_MESSAGE);
        
    }
    
    private void guardarConfiguracion() {
        int duracion = (Integer) spinnerDuracionCiclo.getValue();
        ConfiguracionPersistencia.guardarConfiguracion(duracion);
        
        Lista<ConfiguracionPersistencia.ProcesoConfig> procesosConfig = new Lista<>();
        Lista<Proceso> todosLosProcesos = simulador.getTodosLosProcesos();
        
        for (int i = 0; i < todosLosProcesos.tamaño(); i++) {
            Proceso p = todosLosProcesos.obtener(i);
            ConfiguracionPersistencia.ProcesoConfig pc = 
                new ConfiguracionPersistencia.ProcesoConfig(
                    p.getPcb().getNombre(),
                    p.getNumeroInstrucciones(),
                    p.esCPUBound(),
                    p.getCiclosParaExcepcion(),
                    p.getCiclosParaCompletarExcepcion(),
                    p.getPcb().getPrioridad()
                );
            procesosConfig.agregar(pc);
        }
        
        ConfiguracionPersistencia.guardarProcesos(procesosConfig);
        
        JOptionPane.showMessageDialog(this, 
            "Configuración guardada exitosamente",
            "Éxito",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void cargarConfiguracion() {
        int duracion = ConfiguracionPersistencia.cargarDuracionCiclo();
        spinnerDuracionCiclo.setValue(duracion);
        simulador.setDuracionCiclo(duracion);
        
        Lista<ConfiguracionPersistencia.ProcesoConfig> procesosConfig = 
            ConfiguracionPersistencia.cargarProcesos();
        
        for (int i = 0; i < procesosConfig.tamaño(); i++) {
            Proceso p = procesosConfig.obtener(i).crearProceso();
            simulador.agregarProceso(p);
        }
        
        actualizarTodo();
        
        JOptionPane.showMessageDialog(this, 
            "Se cargaron: " + procesosConfig.tamaño() + " procesos",
            "..",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void cargarConfiguracionInicial() {
        try {
            int duracion = ConfiguracionPersistencia.cargarDuracionCiclo();
            spinnerDuracionCiclo.setValue(duracion);
            simulador.setDuracionCiclo(duracion);
        } catch (Exception e) {
        }
    }
    
    private void actualizarTodo() {
        lblCicloGlobal.setText(String.valueOf(simulador.getCicloGlobal()));
        
        Proceso actual = simulador.getProcesoActual();
        if (actual != null) {
            lblProcesoActual.setText(actual.getPcb().getNombre() + 
                " (PC: " + actual.getPcb().getProgramCounter() + ")");
        } else {
            lblProcesoActual.setText("Ninguno");
        }
        
        panelColas.actualizar();
        panelPCB.actualizar();
        panelMetricas.actualizar();
        
        if (panelGraficas != null) {
            panelGraficas.actualizar();
        }
    }
    
    @Override
    public void onActualizacion() {
        SwingUtilities.invokeLater(() -> actualizarTodo());
    }
    
    @Override
    public void onNuevoEvento(String evento) {
        SwingUtilities.invokeLater(() -> panelLog.agregarEvento(evento));
    }
    
    @Override
    public void onCambioPlanificador(String nombrePlanificador) {
        SwingUtilities.invokeLater(() -> {
        });
    }
    
    @Override
    public void onCambioModo(boolean esModoSO) {
        SwingUtilities.invokeLater(() -> {
            if (esModoSO) {
                lblModo.setText("MODO SISTEMA OPERATIVO");
                lblModo.setForeground(Color.BLUE);
                lblModo.setBackground(new Color(200, 220, 255));
                lblModo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLUE, 3),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            } else {
                lblModo.setText("MODO USUARIO");
                lblModo.setForeground(new Color(0, 128, 0));
                lblModo.setBackground(new Color(220, 255, 220));
                lblModo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GREEN, 3),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}
