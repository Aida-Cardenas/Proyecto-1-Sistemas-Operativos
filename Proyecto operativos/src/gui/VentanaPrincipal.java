//ESTA ES LA CLASE MAIN NO CREEN OTRA PLS :)
package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import estructuras.Lista;
import modelo.Proceso;
import persistencia.ConfiguracionPersistencia;
import planificacion.FCFS;
import planificacion.Planificador;
import planificacion.Prioridad;
import planificacion.RoundRobin;
import planificacion.SJF;
import sistema.Simulador;
import sistema.SimuladorListener;

public class VentanaPrincipal extends JFrame implements SimuladorListener {
    /**
     * VentanaPrincipal - La cara del simulador
     * 
     * Aquí vive toda la interfaz gráfica: botones de control, paneles de
     * colas, PCBs, log y métricas. Se conecta al Simulador mediante la
     * interfaz SimuladorListener para recibir actualizaciones en tiempo real.
     * 
     * Meta: que sea claro, útil y lo más visual posible para entender cómo
     * se comportan los algoritmos de planificación.
     */
    private Simulador simulador;
    
    // componentes
    private JPanel panelSuperior;
    private JPanel panelCentral;
    private JPanel panelInferior;
    
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
    
    public VentanaPrincipal() {
        simulador = new Simulador();
        simulador.setListener(this);
        
        initComponents();
        cargarConfiguracionInicial();
    }
    
    private void initComponents() {
        setTitle("Simulador de Planificación de Procesos");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        crearPanelSuperior(); // Controles y estado general
        
        crearPanelCentral(); // Colas, PCB y Log
        
        crearPanelInferior(); // Métricas
        
        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private void crearPanelSuperior() {
        panelSuperior = new JPanel(new GridBagLayout());
        panelSuperior.setBorder(BorderFactory.createTitledBorder("Controles"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // fila 1 botones
        gbc.gridx = 0; gbc.gridy = 0;
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
        btnAgregar20Aleatorios.addActionListener(e -> agregar20ProcesosAleatorios());
        panelSuperior.add(btnAgregar20Aleatorios, gbc);
        
    // fila 2: configuración del planificador y duración de ciclo
        gbc.gridx = 0; gbc.gridy = 1;
        panelSuperior.add(new JLabel("Planificador:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        String[] planificadores = {
            "FCFS",
            "SJF Non-Preemptive",
            "SJF Preemptive (SRTF)",
            "Round Robin",
            "Prioridad Non-Preemptive",
            "Prioridad Preemptive"
        };
        comboPlanificador = new JComboBox<>(planificadores);
        comboPlanificador.addActionListener(e -> cambiarPlanificador());
        panelSuperior.add(comboPlanificador, gbc);
        
        gbc.gridx = 3; gbc.gridwidth = 1;
        panelSuperior.add(new JLabel("Duración Ciclo (ms):"), gbc);
        
        gbc.gridx = 4;
        spinnerDuracionCiclo = new JSpinner(new SpinnerNumberModel(1000, 100, 5000, 100));
        spinnerDuracionCiclo.addChangeListener(e -> cambiarDuracionCiclo());
        panelSuperior.add(spinnerDuracionCiclo, gbc);
        
    // fila 3: info de estado del simulador
        gbc.gridx = 0; gbc.gridy = 2;
        panelSuperior.add(new JLabel("Ciclo Global:"), gbc);
        
        gbc.gridx = 1;
        lblCicloGlobal = new JLabel("0");
        lblCicloGlobal.setFont(new Font("Monospaced", Font.BOLD, 14));
        panelSuperior.add(lblCicloGlobal, gbc);
        
        gbc.gridx = 2;
        panelSuperior.add(new JLabel("Modo:"), gbc);
        
        gbc.gridx = 3;
        lblModo = new JLabel("Sistema Operativo");
        lblModo.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblModo.setForeground(Color.BLUE);
        panelSuperior.add(lblModo, gbc);
        
    // fila 4: indicador del proceso actualmente en CPU
        gbc.gridx = 0; gbc.gridy = 3;
        panelSuperior.add(new JLabel("Proceso Actual:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 3;
        lblProcesoActual = new JLabel("Ninguno");
        lblProcesoActual.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblProcesoActual.setForeground(Color.RED);
        panelSuperior.add(lblProcesoActual, gbc);
        
    // fila 5: persistencia de config y procesos
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        btnGuardarConfig = new JButton("Guardar Configuración");
        btnGuardarConfig.addActionListener(e -> guardarConfiguracion());
        panelSuperior.add(btnGuardarConfig, gbc);
        
        gbc.gridx = 1;
        btnCargarConfig = new JButton("Cargar Configuración");
        btnCargarConfig.addActionListener(e -> cargarConfiguracion());
        panelSuperior.add(btnCargarConfig, gbc);
    }
    
    private void crearPanelCentral() {
        panelCentral = new JPanel(new GridLayout(1, 3, 10, 10));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel de colas: visualiza LISTO/BLOQUEADO/SUSPENDIDOS/TERMINADOS
        panelColas = new PanelColas(simulador);
        panelCentral.add(panelColas);
        
        // Panel de PCB: tabla con los registros y estados por proceso
        panelPCB = new PanelPCB(simulador);
        panelCentral.add(panelPCB);
        
        // Panel de Log: cronología de eventos del simulador
        panelLog = new PanelLog();
        panelCentral.add(panelLog);
    }
    
    private void crearPanelInferior() {
        panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBorder(BorderFactory.createTitledBorder("Métricas del Sistema"));
        panelInferior.setPreferredSize(new Dimension(0, 200));
        
        panelMetricas = new PanelMetricas(simulador);
        panelInferior.add(panelMetricas, BorderLayout.CENTER);
    }
    
    private void iniciarSimulacion() {
        // Si es la primera vez, arranca el hilo; si no, simplemente reanuda
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
            actualizarTodo();
        }
    }
    
    private void cambiarPlanificador() {
        // Cambia el algoritmo en caliente; en Round Robin pide quantum
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
        // Ventana modal para crear un proceso con parámetros
        DialogoAgregarProceso dialogo = new DialogoAgregarProceso(this);
        dialogo.setVisible(true);
        
        if (dialogo.isConfirmado()) {
            Proceso proceso = dialogo.getProceso();
            simulador.agregarProceso(proceso);
            actualizarTodo();
        }
    }
    
    // Genera 20 procesos con parámetros al azar (balanceado CPU/I-O)
    private void agregar20ProcesosAleatorios() {
        java.util.Random random = new java.util.Random();
        StringBuilder resumen = new StringBuilder();
        resumen.append("\n");
        resumen.append("   GENERACIÓN DE 20 PROCESOS ALEATORIOS\n");
        resumen.append("\n");
        
        int cpuBoundCount = 0;
        int ioBoundCount = 0;
        
        for (int i = 1; i <= 20; i++) {
            String nombre = "Proc_" + System.currentTimeMillis() % 10000 + "_" + i;
            
            // 50/50 CPU-Bound o I/O-Bound
            boolean esCPUBound = random.nextBoolean();
            
            // Cantidad de instrucciones (entre 10 y 50)
            int numInstrucciones = 10 + random.nextInt(41);
            
            // prioridad
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
            resumen.append(String.format("%2d. %-20s | %-10s | Instr: %2d | Prio: %2d", 
                i, nombre, tipo, numInstrucciones, prioridad));
            
            if (!esCPUBound) {
                resumen.append(String.format(" | I/O: cada %d ciclos, duración %d", 
                    ciclosExcepcion, ciclosCompletarExcepcion));
            }
            resumen.append("\n");
            
            if (esCPUBound) {
                cpuBoundCount++;
            } else {
                ioBoundCount++;
            }
        }
        
    // Muestra un pequeño resumen en un textarea con monoespaciado
        resumen.append("\n");
        resumen.append("                    Procesos Creados\n");
        resumen.append("\n");
        resumen.append(String.format("Total de procesos: 20\n"));
        resumen.append(String.format("CPU-Bound: %d (%.0f%%)\n", cpuBoundCount, (cpuBoundCount/20.0)*100));
        resumen.append(String.format("I/O-Bound: %d (%.0f%%)\n", ioBoundCount, (ioBoundCount/20.0)*100));
        resumen.append("═══════════════════════════════════════════════════\n");
        
        actualizarTodo();
        
        JTextArea textArea = new JTextArea(resumen.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setCaretPosition(0);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(700, 500));
       
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
            "Configuración cargada: " + procesosConfig.tamaño() + " procesos",
            "Éxito",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void cargarConfiguracionInicial() {
        try {
            int duracion = ConfiguracionPersistencia.cargarDuracionCiclo();
            spinnerDuracionCiclo.setValue(duracion);
            simulador.setDuracionCiclo(duracion);
        } catch (Exception e) {
            // Ignorar silenciosamente si no existe aún
        }
    }
    
    private void actualizarTodo() {
        // Actualiza: ciclo, proceso actual y paneles
        lblCicloGlobal.setText(String.valueOf(simulador.getCicloGlobal()));
        
        // Proceso actual
        Proceso actual = simulador.getProcesoActual();
        if (actual != null) {
            lblProcesoActual.setText(actual.getPcb().getNombre() + 
                " (PC: " + actual.getPcb().getProgramCounter() + ")");
        } else {
            lblProcesoActual.setText("Ninguno");
        }
        
        // Paneles
        panelColas.actualizar();
        panelPCB.actualizar();
        panelMetricas.actualizar();
    }
    
    // simuladorlistener
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
                lblModo.setText("Sistema Operativo");
                lblModo.setForeground(Color.BLUE);
            } else {
                lblModo.setText("Proceso de Usuario");
                lblModo.setForeground(Color.GREEN);
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