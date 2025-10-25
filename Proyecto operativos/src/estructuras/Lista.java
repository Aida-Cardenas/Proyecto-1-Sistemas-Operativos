package estructuras;

public class Lista<T> {
    /**
     * Lista - Implementación simple de una lista enlazada
     * 
     * Hecha a mano para aprender y tener control total:
     * - agregar(): pega el elemento al final
     * - obtener(): trae el elemento en la posición i
     * - eliminar(): quita el elemento en la posición i y lo devuelve
     * - eliminarElemento(): busca y elimina la primera aparición de x
     * - contiene(): te dice si está o no
     */
    private Nodo<T> cabeza; // inicio
    private Nodo<T> cola;   // fin
    private int tamaño;     // cantidad de elementos

    // nodo
    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;
        Nodo(T dato) {
            this.dato = dato;
            this.siguiente = null;
        }
    }

    public Lista() {
        cabeza = null;
        cola = null;
        tamaño = 0;
    }

    // Agregar al final 
    public void agregar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (cabeza == null) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            cola.siguiente = nuevo;
            cola = nuevo;
        }
        tamaño++;
    }

    // Obtener por índice 
    public T obtener(int indice) {
        if (indice < 0 || indice >= tamaño) throw new IndexOutOfBoundsException();
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) actual = actual.siguiente;
        return actual.dato;
    }

    // Eliminar por índice y devolver el elemento eliminado
    public T eliminar(int indice) {
        if (indice < 0 || indice >= tamaño) throw new IndexOutOfBoundsException();
        T dato;
        if (indice == 0) {
            dato = cabeza.dato;
            cabeza = cabeza.siguiente;
            if (cabeza == null) cola = null;
        } else {
            Nodo<T> anterior = cabeza;
            for (int i = 0; i < indice - 1; i++) anterior = anterior.siguiente;
            dato = anterior.siguiente.dato;
            anterior.siguiente = anterior.siguiente.siguiente;
            if (anterior.siguiente == null) cola = anterior;
        }
        tamaño--;
        return dato;
    }

    // Eliminar la primera aparición de un elemento por igualdad
    public boolean eliminarElemento(T elemento) {
        if (cabeza == null) return false;
        if (cabeza.dato.equals(elemento)) {
            cabeza = cabeza.siguiente;
            if (cabeza == null) cola = null;
            tamaño--;
            return true;
        }
        Nodo<T> actual = cabeza;
        while (actual.siguiente != null) {
            if (actual.siguiente.dato.equals(elemento)) {
                actual.siguiente = actual.siguiente.siguiente;
                if (actual.siguiente == null) cola = actual;
                tamaño--;
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }

    public int tamaño() { return tamaño; }
    public boolean estaVacia() { return tamaño == 0; }
    public void limpiar() { cabeza = null; cola = null; tamaño = 0; }
    public boolean contiene(T elemento) {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (actual.dato.equals(elemento)) return true;
            actual = actual.siguiente;
        }
        return false;
    }
}