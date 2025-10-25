package estructuras;

public class Cola<T> {
    /**
     * Cola - Estructura FIFO sencillita para el simulador
     * 
     * Esta clase implementa una cola enlazada clásica (First-In, First-Out):
     * - encolar(): mete elementos al final (como hacer fila)
     * - desencolar(): saca el primer elemento (el que lleva más tiempo esperando)
     * - verFrente(): mira quién va primero sin sacarlo
     * - obtenerTodos(): devuelve una Lista con los elementos tal como están en la cola
     */
    private Nodo<T> frente; // primer elemento
    private Nodo<T> fin;    // ultimo elemento
    private int tamaño;     // cantidad de elementos

    // nodo cola
    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;
        Nodo(T dato) {
            this.dato = dato;
            this.siguiente = null;
        }
    }

    public Cola() {
        frente = null;
        fin = null;
        tamaño = 0;
    }

    // Agregar al final de la cola 
    public void encolar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (estaVacia()) {
            frente = nuevo;
            fin = nuevo;
        } else {
            fin.siguiente = nuevo;
            fin = nuevo;
        }
        tamaño++;
    }

    // Sacar del frente de la cola 
    public T desencolar() {
        if (estaVacia()) throw new RuntimeException("Cola vacia");
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) fin = null;
        tamaño--;
        return dato;
    }

    // Ver el frente sin eliminarlo 
    public T verFrente() {
        if (estaVacia()) throw new RuntimeException("Cola vacia");
        return frente.dato;
    }

    public boolean estaVacia() { return tamaño == 0; }
    public int tamaño() { return tamaño; }
    public void limpiar() { frente = null; fin = null; tamaño = 0; }

    // Obtener todos los elementos como una Lista, respetando el orden FIFO
    public Lista<T> obtenerTodos() {
        Lista<T> lista = new Lista<>();
        Nodo<T> actual = frente;
        while (actual != null) {
            lista.agregar(actual.dato);
            actual = actual.siguiente;
        }
        return lista;
    }
}