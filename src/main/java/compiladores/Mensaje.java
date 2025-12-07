package compiladores;

/**
 * Representa un mensaje generado durante el proceso de compilación.
 * Puede ser de tipo INFO, WARNING o ERROR y almacena texto, línea y columna.
 */
public class Mensaje {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";

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
        String color;
        switch (tipo) {
            case ERROR:
                color = ANSI_RED;
                break;
            case WARNING:
                color = ANSI_YELLOW;
                break;
            default:
                color = ANSI_BLUE;
                break;
        }
        return color + "[" + tipo + "] (" + linea + "," + columna + ") " + texto + ANSI_RESET;
    }
}
