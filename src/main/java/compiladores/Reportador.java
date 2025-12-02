package compiladores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestor centralizado de mensajes del compilador.
 * <p>
 * Acumula mensajes de distintos componentes (lexer, parser, listener, etc.)
 * y permite luego consultarlos o imprimirlos de forma unificada.
 * </p>
 */
public class Reportador {

    private static final Reportador INSTANCIA = new Reportador();

    private final List<Mensaje> mensajes = new ArrayList<>();

    private Reportador() {
    }

    public static Reportador getInstancia() {
        return INSTANCIA;
    }

    public void agregar(Mensaje mensaje) {
        if (mensaje != null) {
            mensajes.add(mensaje);
        }
    }

    public void info(String texto, int linea, int columna) {
        agregar(new Mensaje(Mensaje.Tipo.INFO, texto, linea, columna));
    }

    public void warning(String texto, int linea, int columna) {
        agregar(new Mensaje(Mensaje.Tipo.WARNING, texto, linea, columna));
    }

    public void error(String texto, int linea, int columna) {
        agregar(new Mensaje(Mensaje.Tipo.ERROR, texto, linea, columna));
    }

    public List<Mensaje> getMensajes() {
        return Collections.unmodifiableList(mensajes);
    }

    public long getCantidadErrores() {
        return mensajes.stream().filter(m -> m.getTipo() == Mensaje.Tipo.ERROR).count();
    }

    public void limpiar() {
        mensajes.clear();
    }

    /**
     * Imprime todos los mensajes en consola, con un formato simple.
     */
    public void imprimirEnConsola() {
        for (Mensaje m : mensajes) {
            System.out.println(m.toString());
        }
    }
}
