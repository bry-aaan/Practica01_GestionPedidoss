package modelo;

import java.util.ArrayList;

public class UtilGenerico {
    
    public <T> ArrayList<T> crearLista(T... elementos) {
        ArrayList<T> lista = new ArrayList<>();
        for (T elemento : elementos) {
            lista.add(elemento);
        }
        return lista;
    }

    public <T> void agregarElementos(ArrayList<T> lista, T... elementos) {
        for (T elemento : elementos) {
            lista.add(elemento);
        }
    }
}