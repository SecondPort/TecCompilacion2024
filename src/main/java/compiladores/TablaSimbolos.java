package compiladores;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;

/**
 * Tabla de símbolos para el compilador que gestiona los identificadores y sus contextos.
 * <p>
 * Implementa el patrón de diseño <b>Singleton</b> para garantizar una única instancia global
 * de la tabla de símbolos durante todo el proceso de compilación. Utiliza una estructura de
 * contextos anidados (scopes) para manejar la visibilidad de identificadores según las reglas
 * de alcance léxico del lenguaje.
 * </p>
 * <p>
 * La tabla mantiene una lista de mapas donde cada mapa representa un contexto (scope).
 * Los contextos se apilan cuando se entra a un bloque y se desapilan cuando se sale,
 * permitiendo el sombreado (shadowing) de identificadores en contextos internos.
 * </p>
 *
 * @author Compiladores 2024
 * @version 1.0
 * @since 1.0
 * @see Id
 * @see Variable
 * @see Funcion
 */
public final class TablaSimbolos {
    /**
     * Instancia única de la tabla de símbolos (Patrón Singleton).
     */
    private static TablaSimbolos instancia;
    
    /**
     * Lista de contextos (scopes) donde cada contexto es un mapa de identificadores.
     * El último elemento de la lista representa el contexto actual (más interno).
     */
    private List<Map<String, Id>> ts;

    /**
     * Constructor privado para prevenir instanciación directa (Patrón Singleton).
     * Inicializa la lista de contextos vacía.
     */
    private TablaSimbolos() {
        ts = new LinkedList<>();
    }
    
    /**
     * Obtiene la instancia única de la tabla de símbolos.
     * <p>
     * Implementa lazy initialization: crea la instancia solo cuando se solicita
     * por primera vez. Este método no es thread-safe, pero es suficiente para
     * el contexto de un compilador de un solo hilo.
     * </p>
     *
     * @return la instancia única de {@link TablaSimbolos}
     */
    public static TablaSimbolos getInstancia() {
        if (instancia == null) {
            instancia = new TablaSimbolos();
        }
        return instancia;
    }

    /**
     * Añade un nuevo contexto (scope) a la tabla de símbolos.
     * <p>
     * Se debe invocar al entrar a un nuevo bloque de código (función, bloque if,
     * while, for, etc.). El nuevo contexto se apila sobre los existentes.
     * </p>
     * <p>
     * <b>Postcondición:</b> Un nuevo mapa vacío se añade al final de la lista de contextos.
     * </p>
     *
     * @see #delContexto()
     */
    public void addContexto() {
        ts.add(new HashMap<>());
    }

    /**
     * Elimina el contexto (scope) actual de la tabla de símbolos.
     * <p>
     * Se debe invocar al salir de un bloque de código. Remueve el último contexto
     * añadido, liberando todos los símbolos declarados en ese scope.
     * </p>
     * <p>
     * <b>Precondición:</b> La lista de contextos no debe estar vacía.
     * </p>
     * <p>
     * <b>Postcondición:</b> El último contexto es removido de la lista.
     * </p>
     *
     * @see #addContexto()
     */
    public void delContexto() {
        if (!ts.isEmpty()) {
            ts.remove(ts.size() - 1);
        }
    }

    /**
     * Añade un símbolo al contexto actual si no existe globalmente.
     * <p>
     * Verifica que el símbolo no exista en ningún contexto visible antes de añadirlo
     * al contexto actual. Esto previene declaraciones duplicadas dentro del alcance visible.
     * </p>
     * <p>
     * <b>Precondición:</b> Debe existir al menos un contexto en la tabla.
     * </p>
     * <p>
     * <b>Postcondición:</b> Si el símbolo no existe, se añade al contexto actual.
     * </p>
     *
     * @param nombre el nombre del identificador a añadir
     * @param id el objeto {@link Id} que representa el símbolo (variable o función)
     * @see #contieneSimbolo(String)
     */
    public void addSimbolo(String nombre, Id id) {    
        if (!ts.isEmpty() && contieneSimbolo(nombre) == false) {
            Map<String, Id> contextoActual = ts.get(ts.size() - 1);
            contextoActual.put(nombre, id);
        }
    }

    /**
     * Busca y retorna un símbolo por su nombre en todos los contextos visibles.
     * <p>
     * Realiza una búsqueda desde el contexto más interno hacia el más externo,
     * implementando la regla de alcance léxico del lenguaje. Retorna el primer
     * símbolo encontrado, permitiendo el sombreado (shadowing) de identificadores.
     * </p>
     *
     * @param nombre el nombre del identificador a buscar
     * @return el objeto {@link Id} asociado al nombre, o {@code null} si no existe
     * @see #contieneSimbolo(String)
     */
    public Id getSimbolo(String nombre) {
        for (int i = ts.size() - 1; i >= 0; i--) {
            Map<String, Id> contextoActual = ts.get(i);
            if (contextoActual.containsKey(nombre)) {
                return contextoActual.get(nombre);
            }
        }
        return null;
    }

    /**
     * Verifica si un símbolo existe en alguno de los contextos visibles.
     * <p>
     * Busca el símbolo desde el contexto más interno hacia el más externo.
     * Útil para validaciones semánticas como la detección de uso de variables
     * no declaradas.
     * </p>
     *
     * @param nombre el nombre del identificador a verificar
     * @return {@code true} si el símbolo existe en algún contexto visible,
     *         {@code false} en caso contrario
     * @see #getSimbolo(String)
     * @see #contieneSimboloLocal(String)
     */
    public Boolean contieneSimbolo(String nombre) {
        for (int i = ts.size() - 1; i >= 0; i--) {
            Map<String, Id> contextoActual = ts.get(i);
            if (contextoActual.containsKey(nombre)) {
                return true;
            }
        }
        return false;
    }   
    
    /**
     * Verifica si un símbolo existe solo en el contexto actual (más interno).
     * <p>
     * A diferencia de {@link #contieneSimbolo(String)}, esta función solo busca
     * en el último contexto añadido. Es útil para detectar declaraciones duplicadas
     * en el mismo scope.
     * </p>
     * <p>
     * <b>Precondición:</b> Debe existir al menos un contexto en la tabla.
     * </p>
     *
     * @param nombre el nombre del identificador a verificar
     * @return {@code true} si el símbolo existe en el contexto actual,
     *         {@code false} en caso contrario
     * @see #contieneSimbolo(String)
     */
    public Boolean contieneSimboloLocal(String nombre) {
        Map<String, Id> contextoActual = ts.get(ts.size() - 1);
        if (contextoActual.containsKey(nombre)) {
            return true;
        }
        return false;
    }  
}

/**
 * Clase abstracta que representa un identificador en la tabla de símbolos.
 * <p>
 * Sirve como clase base para todos los tipos de identificadores que pueden
 * aparecer en el código fuente (variables, funciones, etc.). Mantiene información
 * común a todos los identificadores: nombre, tipo de dato, estado de inicialización
 * y estado de uso.
 * </p>
 * <p>
 * Esta clase implementa el patrón <b>Template Method</b> al definir la estructura
 * común de todos los identificadores, permitiendo que las subclases extiendan
 * o especialicen el comportamiento según el tipo de identificador.
 * </p>
 *
 * @author Compiladores 2024
 * @version 1.0
 * @since 1.0
 * @see Variable
 * @see Funcion
 */
abstract class Id {
    /**
     * Nombre del identificador tal como aparece en el código fuente.
     */
    private String nombre;
    
    /**
     * Tipo de dato del identificador (ej: "int", "double", "void").
     */
    private String tipoDato;
    
    /**
     * Indica si el identificador ha sido inicializado con un valor.
     * Permite detectar uso de variables no inicializadas.
     */
    private Boolean inicializado;
    
    /**
     * Indica si el identificador ha sido utilizado en alguna expresión.
     * Permite detectar variables o funciones declaradas pero nunca usadas.
     */
    private Boolean usado;

    /**
     * Obtiene el nombre del identificador.
     *
     * @return el nombre del identificador
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del identificador.
     *
     * @param nombre el nombre del identificador
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el tipo de dato del identificador.
     *
     * @return el tipo de dato como String (ej: "int", "double", "void")
     */
    public String getTipoDato() {
        return tipoDato;
    }

    /**
     * Establece el tipo de dato del identificador.
     *
     * @param tipoDato el tipo de dato como String (ej: "int", "double", "void")
     * @see TipoDato
     */
    public void setTipoDato(String tipoDato) {
        this.tipoDato = tipoDato;
    }

    /**
     * Verifica si el identificador ha sido inicializado.
     * <p>
     * Un identificador se considera inicializado cuando se le ha asignado un valor
     * en su declaración o mediante una asignación posterior.
     * </p>
     *
     * @return {@code true} si está inicializado, {@code false} en caso contrario
     */
    public Boolean getInicializado() {
        return inicializado;
    }

    /**
     * Establece el estado de inicialización del identificador.
     * <p>
     * Debe llamarse cuando el identificador recibe un valor inicial en su
     * declaración o cuando se le asigna un valor por primera vez.
     * </p>
     *
     * @param inicializado {@code true} para marcar como inicializado,
     *                     {@code false} en caso contrario
     */
    public void setInicializado(Boolean inicializado) {
        this.inicializado = inicializado;
    }

    /**
     * Verifica si el identificador ha sido utilizado en alguna expresión.
     * <p>
     * Útil para generar advertencias sobre variables o funciones declaradas
     * pero nunca utilizadas en el código.
     * </p>
     *
     * @return {@code true} si ha sido usado, {@code false} en caso contrario
     */
    public Boolean getUsado() {
        return usado;
    }

    /**
     * Establece el estado de uso del identificador.
     * <p>
     * Debe llamarse cuando el identificador aparece en una expresión o
     * llamada de función.
     * </p>
     *
     * @param usado {@code true} para marcar como usado, {@code false} en caso contrario
     */
    public void setUsado(Boolean usado) {
        this.usado = usado;
    }
}

/**
 * Representa una variable en la tabla de símbolos.
 * <p>
 * Extiende {@link Id} sin añadir funcionalidad adicional. Sirve para distinguir
 * semánticamente las variables de otros tipos de identificadores como funciones.
 * </p>
 * <p>
 * En futuras extensiones podría incluir atributos específicos de variables como
 * constancia, volatilidad, o información de almacenamiento.
 * </p>
 *
 * @author Compiladores 2024
 * @version 1.0
 * @since 1.0
 * @see Id
 */
class Variable extends Id {}

/**
 * Representa una función en la tabla de símbolos.
 * <p>
 * Extiende {@link Id} añadiendo información específica de funciones como la
 * lista de tipos de sus argumentos. Permite validar llamadas a funciones
 * verificando el número y tipo de argumentos.
 * </p>
 *
 * @author Compiladores 2024
 * @version 1.0
 * @since 1.0
 * @see Id
 * @see TipoDato
 */
class Funcion extends Id {
    /**
     * Lista de tipos de datos de los argumentos de la función.
     * <p>
     * Mantiene el orden de los parámetros para validar la correspondencia
     * con los argumentos en las llamadas a la función.
     * </p>
     */
    private List<TipoDato> argumentos;
}

/**
 * Enumeración de los tipos de datos soportados por el compilador.
 * <p>
 * Define los tipos primitivos que pueden ser utilizados en declaraciones
 * de variables y funciones. Esta enumeración facilita el análisis semántico
 * y la verificación de tipos durante la compilación.
 * </p>
 *
 * @author Compiladores 2024
 * @version 1.0
 * @since 1.0
 */
enum TipoDato {
    /**
     * Tipo void, utilizado para funciones que no retornan valor.
     */
    VOID,
    
    /**
     * Tipo entero de 32 bits con signo.
     */
    INT,
    
    /**
     * Tipo punto flotante de doble precisión (64 bits).
     */
    DOUBLE
}