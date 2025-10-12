package estructuras;

public class Cola<T> {
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

    // agregar a l final
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

    // deqeueue
    public T desencolar() {
        if (estaVacia()) throw new RuntimeException("Cola vacia");
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) fin = null;
        tamaño--;
        return dato;
    }

    // volver al frente sin eliminar
    public T verFrente() {
        if (estaVacia()) throw new RuntimeException("Cola vacia");
        return frente.dato;
    }

    public boolean estaVacia() { return tamaño == 0; }
    public int tamaño() { return tamaño; }
    public void limpiar() { frente = null; fin = null; tamaño = 0; }

    // obtener elem de la lista
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