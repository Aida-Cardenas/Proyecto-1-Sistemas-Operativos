package persistencia;

import estructuras.Lista;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ConfiguracionPersistencia {
   private static final String ARCHIVO_CONFIG = "configuracion.csv";
   private static final String ARCHIVO_PROCESOS = "procesos.csv";

   public ConfiguracionPersistencia() {
   }

   public static void guardarConfiguracion(int duracionCiclo) {
      try {
         PrintWriter writer = new PrintWriter(new FileWriter("configuracion.csv"));

         try {
            writer.println("parametro,valor");
            writer.println("duracionCiclo," + duracionCiclo);
         } catch (Throwable var5) {
            try {
               writer.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         writer.close();
      } catch (IOException var6) {
         System.err.println("Error al guardar configuración: " + var6.getMessage());
      }

   }

   public static int cargarDuracionCiclo() {
      int duracionCiclo = 1000;
      File archivo = new File("configuracion.csv");
      if (!archivo.exists()) {
         return duracionCiclo;
      } else {
         try {
            BufferedReader reader = new BufferedReader(new FileReader("configuracion.csv"));

            try {
               reader.readLine();

               String linea;
               while((linea = reader.readLine()) != null) {
                  String[] partes = linea.split(",");
                  if (partes.length >= 2 && partes[0].equals("duracionCiclo")) {
                     duracionCiclo = Integer.parseInt(partes[1]);
                  }
               }
            } catch (Throwable var6) {
               try {
                  reader.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }

               throw var6;
            }

            reader.close();
         } catch (NumberFormatException | IOException var7) {
            System.err.println("Error al cargar configuración: " + var7.getMessage());
         }

         return duracionCiclo;
      }
   }

   public static void guardarProcesos(Lista<ProcesoConfig> procesos) {
      try {
         PrintWriter writer = new PrintWriter(new FileWriter("procesos.csv"));

         try {
            writer.println("nombre,numInstrucciones,esCPUBound,ciclosExcepcion,ciclosCompletarExcepcion,prioridad");

            for(int i = 0; i < procesos.tamaño(); ++i) {
               ProcesoConfig pc = (ProcesoConfig)procesos.obtener(i);
               writer.printf("%s,%d,%b,%d,%d,%d%n", pc.nombre, pc.numInstrucciones, pc.esCPUBound, pc.ciclosExcepcion, pc.ciclosCompletarExcepcion, pc.prioridad);
            }
         } catch (Throwable var5) {
            try {
               writer.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         writer.close();
      } catch (IOException var6) {
         System.err.println("Error al guardar procesos: " + var6.getMessage());
      }

   }

   public static Lista<ProcesoConfig> cargarProcesos() {
      Lista<ProcesoConfig> procesos = new Lista();
      File archivo = new File("procesos.csv");
      if (!archivo.exists()) {
         return procesos;
      } else {
         try {
            BufferedReader reader = new BufferedReader(new FileReader("procesos.csv"));

            try {
               reader.readLine();

               String linea;
               while((linea = reader.readLine()) != null) {
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
            } catch (Throwable var7) {
               try {
                  reader.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            reader.close();
         } catch (NumberFormatException | IOException var8) {
            System.err.println("Error al cargar procesos: " + var8.getMessage());
         }

         return procesos;
      }
   }
}
