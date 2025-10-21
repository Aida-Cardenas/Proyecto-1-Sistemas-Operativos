package persistencia;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import estructuras.Lista;
import modelo.Proceso;
import sistema.Metricas;
import sistema.Simulador;

/**
 * ConfiguracionPersistencia - Manejo de archivos CSV y configuraciones
 * 
 * Básicamente es el "Excel automático" del simulador que guarda todo
 * lo importante para poder revisar después qué pasó en cada simulación.
 */
public class ConfiguracionPersistencia {
    private static final String CARPETA_DATOS = "datos_simulacion";
    private static final String ARCHIVO_CONFIG = "configuracion.properties";
    private static final String PATRON_FECHA = "yyyy-MM-dd_HH-mm-ss";
    
    // Constructor
    public ConfiguracionPersistencia() {
        crearCarpetaDatos();
    }
    
    // Crear carpeta de datos si no existe
    private void crearCarpetaDatos() {
        File carpeta = new File(CARPETA_DATOS);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
    }
    

    public void guardarConfiguracion(String planificador, int duracionCiclo, int numProcesos) {
        Properties config = new Properties();
        config.setProperty("planificador", planificador);
        config.setProperty("duracion_ciclo", String.valueOf(duracionCiclo));
        config.setProperty("num_procesos", String.valueOf(numProcesos));
        config.setProperty("fecha_guardado", new Date().toString());
        
        try (FileOutputStream out = new FileOutputStream(CARPETA_DATOS + File.separator + ARCHIVO_CONFIG)) {
            config.store(out, "Configuración del Simulador de SO");
        } catch (IOException e) {
            System.err.println("Error al guardar configuración: " + e.getMessage());
        }
    }
    

    public Properties cargarConfiguracion() {
        Properties config = new Properties();
        File archivo = new File(CARPETA_DATOS + File.separator + ARCHIVO_CONFIG);
        
        if (archivo.exists()) {
            try (FileInputStream in = new FileInputStream(archivo)) {
                config.load(in);
            } catch (IOException e) {
                System.err.println("Error al cargar configuración: " + e.getMessage());
            }
        } else {
            // Configuración por defecto
            config.setProperty("planificador", "FCFS");
            config.setProperty("duracion_ciclo", "1000");
            config.setProperty("num_procesos", "5");
        }
        
        return config;
    }
    
    // Exportar métricas a CSV
    public void exportarMetricas(Simulador simulador, String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat(PATRON_FECHA);
            nombreArchivo = "metricas_" + sdf.format(new Date()) + ".csv";
        }
        
        if (!nombreArchivo.endsWith(".csv")) {
            nombreArchivo += ".csv";
        }
        
        String rutaArchivo = CARPETA_DATOS + File.separator + nombreArchivo;
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
            Metricas metricas = simulador.getMetricas();
            
            // Encabezados del CSV
            writer.println("Métrica,Valor,Unidad");
            writer.println("Fecha de Simulación," + new Date() + ",");
            writer.println("Planificador," + simulador.getPlanificador().getNombre() + ",");
            writer.println("Ciclos Totales," + metricas.getCiclosTotales() + ",ciclos");
            writer.println("Procesos Completados," + metricas.getProcesosCompletados() + ",procesos");
            writer.println("Tiempo Espera Promedio," + String.format("%.2f", metricas.calcularTiempoEsperaPromedio()) + ",ms");
            writer.println("Tiempo Respuesta Promedio," + String.format("%.2f", metricas.calcularTiempoRespuestaPromedio()) + ",ms");
            writer.println("Tiempo Retorno Promedio," + String.format("%.2f", metricas.calcularTiempoRetornoPromedio()) + ",ms");
            writer.println("Utilización CPU," + String.format("%.2f", metricas.calcularUtilizacionCPU()) + ",%");
            writer.println("Throughput," + String.format("%.4f", metricas.calcularThroughput()) + ",procesos/seg");
            writer.println("Equidad," + String.format("%.4f", metricas.calcularEquidad()) + ",ratio");
            
            System.out.println("Métricas exportadas a: " + rutaArchivo);
        } catch (IOException e) {
            System.err.println("Error al exportar métricas: " + e.getMessage());
        }
    }
    

    public void exportarProcesos(Lista<Proceso> procesos, String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat(PATRON_FECHA);
            nombreArchivo = "procesos_" + sdf.format(new Date()) + ".csv";
        }
        
        if (!nombreArchivo.endsWith(".csv")) {
            nombreArchivo += ".csv";
        }
        
        String rutaArchivo = CARPETA_DATOS + File.separator + nombreArchivo;
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
            // Encabezados del CSV
            writer.println("ID,Nombre,Estado,Instrucciones Totales,Instrucciones Ejecutadas,Tiempo Llegada,Tiempo Inicio,Tiempo Finalización,Tiempo Espera,Tiempo Respuesta");
            
            // Datos de cada proceso
            for (int i = 0; i < procesos.tamaño(); i++) {
                Proceso p = procesos.obtener(i);
                writer.printf("%d,%s,%s,%d,%d,%d,%d,%d,%d,%d%n",
                    p.getPcb().getId(),
                    p.getPcb().getNombre(),
                    p.getPcb().getEstado(),
                    p.getNumeroInstrucciones(),
                    p.getCiclosEjecutados(),
                    p.getPcb().getTiempoLlegada(),
                    p.getPcb().getTiempoInicioEjecucion(),
                    p.getPcb().getTiempoFinalizacion(),
                    p.getPcb().getTiempoEspera(),
                    p.getPcb().getTiempoRespuesta()
                );
            }
            
            System.out.println("Procesos exportados a: " + rutaArchivo);
        } catch (IOException e) {
            System.err.println("Error al exportar procesos: " + e.getMessage());
        }
    }
    

    public void exportarLog(String log, String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat(PATRON_FECHA);
            nombreArchivo = "log_" + sdf.format(new Date()) + ".txt";
        }
        
        if (!nombreArchivo.endsWith(".txt")) {
            nombreArchivo += ".txt";
        }
        
        String rutaArchivo = CARPETA_DATOS + File.separator + nombreArchivo;
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
            writer.println("=== LOG DE SIMULACIÓN ===");
            writer.println("Fecha: " + new Date());
            writer.println("========================");
            writer.println();
            writer.println(log);
            
            System.out.println("Log exportado a: " + rutaArchivo);
        } catch (IOException e) {
            System.err.println("Error al exportar log: " + e.getMessage());
        }
    }
    

    public void generarReporteCompleto(Simulador simulador, String nombreBase) {
        if (nombreBase == null || nombreBase.trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat(PATRON_FECHA);
            nombreBase = "reporte_" + sdf.format(new Date());
        }
        
        // Exportar métricas
        exportarMetricas(simulador, nombreBase + "_metricas.csv");
        
        // Exportar procesos completados
        if (simulador.getProcesosTerminados().tamaño() > 0) {
            exportarProcesos(simulador.getProcesosTerminados(), nombreBase + "_procesos_terminados.csv");
        }
        
        // Exportar todos los procesos
        if (simulador.getTodosLosProcesos().tamaño() > 0) {
            exportarProcesos(simulador.getTodosLosProcesos(), nombreBase + "_todos_procesos.csv");
        }
        
        // Exportar log
        exportarLog(simulador.getLog(), nombreBase + "_log.txt");
        
        System.out.println("Reporte completo generado con base: " + nombreBase);
    }
    

    public String[] listarArchivosSimulacion() {
        File carpeta = new File(CARPETA_DATOS);
        if (!carpeta.exists()) {
            return new String[0];
        }
        
        return carpeta.list((dir, name) -> 
            name.endsWith(".csv") || name.endsWith(".txt") || name.equals(ARCHIVO_CONFIG)
        );
    }
    
    // Eliminar archivos antiguos (mantener solo los más recientes)
    public void limpiarArchivosAntiguos(int maxArchivos) {
        File carpeta = new File(CARPETA_DATOS);
        if (!carpeta.exists()) {
            return;
        }
        
        File[] archivos = carpeta.listFiles((dir, name) -> 
            name.endsWith(".csv") || name.endsWith(".txt")
        );
        
        if (archivos != null && archivos.length > maxArchivos) {
            // Ordenar por fecha de modificación
            java.util.Arrays.sort(archivos, (a, b) -> 
                Long.compare(a.lastModified(), b.lastModified())
            );
            
            // Eliminar los más antiguos
            for (int i = 0; i < archivos.length - maxArchivos; i++) {
                if (archivos[i].delete()) {
                    System.out.println("Archivo eliminado: " + archivos[i].getName());
                }
            }
        }
    }
    

    public boolean existeConfiguracion() {
        File archivo = new File(CARPETA_DATOS + File.separator + ARCHIVO_CONFIG);
        return archivo.exists();
    }
    
    
    public String getRutaCarpetaDatos() {
        return new File(CARPETA_DATOS).getAbsolutePath();
    }
}
