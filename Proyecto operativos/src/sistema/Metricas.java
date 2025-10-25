package sistema;

import modelo.Proceso;

/**
 * Metricas, el contador de estadísticas del simulador
 * 
 * Esta clase es básicamente el "Excel" del simulador XD
 * Lleva la cuenta de TODO lo que pasa:
 * - Cuántos procesos terminaron (como un marcador de videojuego)
 * - Cuánto tiempo tardaron en completarse (speedrun stats)
 * - Cuánto tiempo esperaron en cola (literalmente lo que dice ahí)
 * - Qué tan ocupada estuvo la CPU (Medidor de chamba)
 * - Cálculos promedio de rendimiento (para sacar las conclusiones finales)
 * 
 * Es como el "estadísticas de fin de año" de Spotify, pero para procesos
 * Todo lo registra para después poder analizar qué tan bien (o mal) 
 * funcionó nuestro planificador.
 */

public class Metricas {
   private long tiempoInicio = System.currentTimeMillis();
   private int procesosCompletados = 0;
   private long tiempoTotalEjecucion = 0L;
   private long tiempoTotalEspera = 0L;
   private long tiempoTotalRespuesta = 0L;
   private long tiempoCPUOcupado = 0L;
   private int ciclosTotales = 0;

   public Metricas() {
   }

   public void registrarProcesoCompletado(Proceso proceso) {
      ++this.procesosCompletados;
      long tiempoRetorno = proceso.getPcb().getTiempoFinalizacion() - proceso.getPcb().getTiempoLlegada();
      this.tiempoTotalEjecucion += tiempoRetorno;
      this.tiempoTotalEspera += proceso.getPcb().getTiempoEspera();
      this.tiempoTotalRespuesta += proceso.getPcb().getTiempoRespuesta();
   }

   public void registrarCicloConCPU() {
      ++this.tiempoCPUOcupado;
      ++this.ciclosTotales;
   }

   public void registrarCicloSinCPU() {
      ++this.ciclosTotales;
   }

   public double calcularThroughput() {
      long tiempoTranscurrido = System.currentTimeMillis() - this.tiempoInicio;
      return tiempoTranscurrido == 0L ? 0.0 : (double)this.procesosCompletados * 1000.0 / (double)tiempoTranscurrido;
   }

   public double calcularUtilizacionCPU() {
      return this.ciclosTotales == 0 ? 0.0 : (double)this.tiempoCPUOcupado * 100.0 / (double)this.ciclosTotales;
   }

   public double calcularTiempoEsperaPromedio() {
      return this.procesosCompletados == 0 ? 0.0 : (double)this.tiempoTotalEspera / (double)this.procesosCompletados;
   }

   public double calcularTiempoRespuestaPromedio() {
      return this.procesosCompletados == 0 ? 0.0 : (double)this.tiempoTotalRespuesta / (double)this.procesosCompletados;
   }

   public double calcularTiempoRetornoPromedio() {
      return this.procesosCompletados == 0 ? 0.0 : (double)this.tiempoTotalEjecucion / (double)this.procesosCompletados;
   }

   public double calcularEquidad() {
      return 1.0 / (1.0 + this.calcularTiempoEsperaPromedio());
   }

   public int getProcesosCompletados() {
      return this.procesosCompletados;
   }

   public int getCiclosTotales() {
      return this.ciclosTotales;
   }

   public void reiniciar() {
      this.tiempoInicio = System.currentTimeMillis();
      this.procesosCompletados = 0;
      this.tiempoTotalEjecucion = 0L;
      this.tiempoTotalEspera = 0L;
      this.tiempoTotalRespuesta = 0L;
      this.tiempoCPUOcupado = 0L;
      this.ciclosTotales = 0;
   }
}
