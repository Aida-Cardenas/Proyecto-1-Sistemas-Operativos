package gui;

import javax.swing.*;
import java.awt.*;

public class PanelLog extends JPanel {
    private JTextArea txtLog;
    private JScrollPane scrollPane;
    
    public PanelLog() {
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Log de Eventos"));
        
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtLog.setLineWrap(false);
        
        scrollPane = new JScrollPane(txtLog);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        add(scrollPane, BorderLayout.CENTER);
     
        JButton btnLimpiar = new JButton("Limpiar Log");
        btnLimpiar.addActionListener(e -> limpiarLog());
        add(btnLimpiar, BorderLayout.SOUTH);
    }
    
    public void agregarEvento(String evento) {
        txtLog.append(evento + "\n");
        
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    public void limpiarLog() {
        txtLog.setText("");
    }
}