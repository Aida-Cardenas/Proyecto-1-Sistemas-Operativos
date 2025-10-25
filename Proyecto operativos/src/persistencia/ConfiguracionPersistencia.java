package persistencia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import estructuras.Lista;
import modelo.Proceso;

public class ConfiguracionPersistencia {
    /**
     * ConfiguracionPersistencia - Guarda y carga configuración simple en CSV
     * 
     * Dos responsabilidades:
     * - Guardar/cargar la duración del ciclo
     * - Guardar/cargar la lista de procesos (parametrizada) como CSV
     */
    private static final String ARCHIVO_CONFIG = "configuracion.csv";
    private static final String ARCHIVO_PROCESOS = "procesos.csv";
    
    // Guarda la configuración global (por ahora, solo duración del ciclo)
    public static void guardarConfiguracion(int duracionCiclo) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARCHIVO_CONFIG))) {
            writer.println("parametro,valor");
            writer.println("duracionCiclo," + duracionCiclo);
        } catch (IOException e) {
            System.err.println("Error al guardar configuración: " + e.getMessage());
        }
    }
    
    // Lee la duración del ciclo desde el archivo, con 1000ms como default
    public static int cargarDuracionCiclo() {
        int duracionCiclo = 1000; // valor por defecto
        
        File archivo = new File(ARCHIVO_CONFIG);
        if (!archivo.exists()) {
            return duracionCiclo;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO_CONFIG))) {
            String linea;
            reader.readLine(); 
            
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length >= 2 && partes[0].equals("duracionCiclo")) {
                    duracionCiclo = Integer.parseInt(partes[1]);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error al cargar configuración: " + e.getMessage());
        }
        
        return duracionCiclo;
    }
    
    // Guarda una lista parametrizada de procesos en un CSV con cabecera
    public static void guardarProcesos(Lista<ProcesoConfig> procesos) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARCHIVO_PROCESOS))) {
            writer.println("nombre,numInstrucciones,esCPUBound,ciclosExcepcion,ciclosCompletarExcepcion,prioridad");
            
            for (int i = 0; i < procesos.tamaño(); i++) {
                ProcesoConfig pc = procesos.obtener(i);
                writer.printf("%s,%d,%b,%d,%d,%d%n",
                    pc.nombre,
                    pc.numInstrucciones,
                    pc.esCPUBound,
                    pc.ciclosExcepcion,
                    pc.ciclosCompletarExcepcion,
                    pc.prioridad
                );
            }
        } catch (IOException e) {
            System.err.println("Error al guardar procesos: " + e.getMessage());
        }
    }
    
    // Carga la lista de procesos desde el CSV, validando formato
    public static Lista<ProcesoConfig> cargarProcesos() {
        Lista<ProcesoConfig> procesos = new Lista<>();
        
        File archivo = new File(ARCHIVO_PROCESOS);
        if (!archivo.exists()) {
            return procesos;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO_PROCESOS))) {
            String linea;
            reader.readLine(); 
            
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length >= 6) {
                    ProcesoConfig pc = new ProcesoConfig();
                    pc.nombre = partes[0];
                    pc.numInstrucciones = Integer.parseInt(partes[1]);
                    pc.esCPUBound = Boolean.parseBoolean(partes[2]);
                    pc.ciclosExcepcion = Integer.parseInt(partes[3]);
                    pc.ciclosCompletarExcepcion = Integer.parseInt(partes[4]);
                    pc.prioridad = Integer.parseInt(partes[5]);
                    procesos.agregar(pc);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error al cargar procesos: " + e.getMessage());
        }
        
        return procesos;
    }
    
    public static class ProcesoConfig {
        // DTO simple para parametrizar procesos en disco
        public String nombre;
        public int numInstrucciones;
        public boolean esCPUBound;
        public int ciclosExcepcion;
        public int ciclosCompletarExcepcion;
        public int prioridad;
        
        public ProcesoConfig() {}
        
        public ProcesoConfig(String nombre, int numInstrucciones, boolean esCPUBound,
                           int ciclosExcepcion, int ciclosCompletarExcepcion, int prioridad) {
            this.nombre = nombre;
            this.numInstrucciones = numInstrucciones;
            this.esCPUBound = esCPUBound;
            this.ciclosExcepcion = ciclosExcepcion;
            this.ciclosCompletarExcepcion = ciclosCompletarExcepcion;
            this.prioridad = prioridad;
        }
        
        // Convierte esta configuración en un Proceso listo para simular
        public Proceso crearProceso() {
            Proceso p = new Proceso(nombre, numInstrucciones, esCPUBound, 
                                   ciclosExcepcion, ciclosCompletarExcepcion);
            p.getPcb().setPrioridad(prioridad);
            return p;
        }
    }
}