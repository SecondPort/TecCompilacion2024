package compiladores;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;

public final class TablaSimbolos {
    private static TablaSimbolos instancia;
    private List<Map<String, Id>> ts;

    private TablaSimbolos() {
        ts = new LinkedList<>();
    }
    
    public static TablaSimbolos getInstancia() {
        if (instancia == null) {
            instancia = new TablaSimbolos();
        }
        return instancia;
    }

    public void addContexto() {
        ts.add(new HashMap<>());
    }

    public void delContexto() {
        if (!ts.isEmpty()) {
            ts.remove(ts.size() - 1);
        }
    }

    public void addSimbolo(String nombre, Id id) {    
        if (!ts.isEmpty() && contieneSimbolo(nombre) == false) {
            Map<String, Id> contextoActual = ts.get(ts.size() - 1);
            contextoActual.put(nombre, id);
        }
    }

    public Id getSimbolo(String nombre) {
        for (int i = ts.size() - 1; i >= 0; i--) {
            Map<String, Id> contextoActual = ts.get(i);
            if (contextoActual.containsKey(nombre)) {
                return contextoActual.get(nombre);
            }
        }
        return null;
    }

    public Boolean contieneSimbolo(String nombre) {
        for (int i = ts.size() - 1; i >= 0; i--) {
            Map<String, Id> contextoActual = ts.get(i);
            if (contextoActual.containsKey(nombre)) {
                return true;
            }
        }
        return false;
    }   
    
    public Boolean contieneSimboloLocal(String nombre) {
        Map<String, Id> contextoActual = ts.get(ts.size() - 1);
        if (contextoActual.containsKey(nombre)) {
            return true;
        }
        return false;
    }  
}

abstract class Id {
    private String nombre;
    private String tipoDato;
    private Boolean inicializado;
    private Boolean usado;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoDato() {
        return tipoDato;
    }

    public void setTipoDato(String tipoDato) {
        this.tipoDato = tipoDato;
    }

    public Boolean getInicializado() {
        return inicializado;
    }

    public void setInicializado(Boolean inicializado) {
        this.inicializado = inicializado;
    }

    public Boolean getUsado() {
        return usado;
    }

    public void setUsado(Boolean usado) {
        this.usado = usado;
    }
}

class Variable extends Id {}

class Funcion extends Id {
    private List<TipoDato> argumentos;
}

enum TipoDato {
    VOID, 
    INT, 
    DOUBLE
}