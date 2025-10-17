package sistema;


/**
 * SimuladorListener, bro está pendiente del sistema 
 * 
 * Esta interfaz es como un "webhook" u "observer" del simulador.
 * Básicamente le dice a quien la implemente: "Voy a avisar cuando pase algo importante"
 * 
 * Es para conectar el backend (Simulador) con el frontend (GUI):
 * - onActualizacion(): "Algo cambió, refresca la pantalla!" (como F5 en el navegador)
 * - onNuevoEvento(): "Pasó esto, anótalo en el log" (la bitácora del simulador)
 * - onCambioModo(): "Cambié entre modo SO/Usuario" (bro cambió a admin)
 * - onCambioPlanificador(): "Cambiamos de algoritmo" (cambio de estrategia en tiempo real)
 * 
 * Sin esto, el simulador sería como alguien trabajando en silencio sin avisar a nadie XD
 * Con esto, la interfaz siempre sabe qué está pasando y se puede actualizar en tiempo real 
 */

public interface SimuladorListener {
   void onActualizacion();

   void onNuevoEvento(String evento);

   void onCambioPlanificador(String nombrePlanificador);

   void onCambioModo(boolean esModoSO);
}
