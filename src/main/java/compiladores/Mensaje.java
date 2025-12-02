package compiladores;

/**
 * Representa un mensaje generado durante el proceso de compilación.
 * Puede ser de tipo INFO, WARNING o ERROR y almacena texto, línea y columna.
 */
public class Mensaje {

    public enum Tipo {
        INFO,
        WARNING,
        ERROR
    }

    private final Tipo tipo;
    private final String texto;
    private final int linea;
    private final int columna;

    public Mensaje(Tipo tipo, String texto, int linea, int columna) {
        this.tipo = tipo;
        this.texto = texto;
        this.linea = linea;
        this.columna = columna;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getTexto() {
        return texto;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        return "[" + tipo + "] (" + linea + "," + columna + ") " + texto;
    }
}
