package compiladores;

import org.antlr.v4.runtime.tree.TerminalNode;

import compiladores.compiladoresParser.AsignacionContext;
import compiladores.compiladoresParser.ProgramaContext;

/**
 * Implementación del patrón Visitor de ANTLR4 para recorrer el árbol sintáctico.
 * <p>
 * Esta clase extiende {@link compiladoresBaseVisitor} e implementa el patrón de diseño
 * <b>Visitor</b>, que permite realizar operaciones sobre los nodos del árbol sintáctico
 * sin modificar la estructura de las clases del árbol.
 * </p>
 * <p>
 * A diferencia del patrón Listener (push), el patrón Visitor (pull) da control total
 * sobre el recorrido del árbol, permitiendo decidir qué nodos visitar y en qué orden.
 * Es útil para generar código, evaluar expresiones o realizar análisis específicos.
 * </p>
 * <p>
 * <b>Responsabilidades:</b>
 * <ul>
 *   <li>Recorrer selectivamente el árbol sintáctico</li>
 *   <li>Contar y reportar asignaciones encontradas</li>
 *   <li>Mostrar información estructural del árbol (número de hijos)</li>
 * </ul>
 *
 * @author Compiladores 2024
 * @version 1.0
 * @since 1.0
 * @see compiladoresBaseVisitor
 * @see Escucha
 */
public class Caminante extends compiladoresBaseVisitor<String> {

    /**
     * Contador de asignaciones encontradas durante el recorrido del árbol.
     * Se incrementa cada vez que se visita un nodo de tipo asignación.
     */
    private Integer asignaciones = 0;
    
    /**
     * Visita el nodo raíz del programa.
     * <p>
     * Este método se invoca para iniciar el recorrido del árbol sintáctico.
     * Después de visitar todos los nodos hijos recursivamente, muestra
     * estadísticas sobre el número total de asignaciones encontradas.
     * </p>
     *
     * @param ctx el contexto del nodo {@code programa} del árbol sintáctico
     * @return el resultado del recorrido (String, puede ser null o un valor agregado)
     */
    @Override
    public String visitPrograma(ProgramaContext ctx) {
        String ret = super.visitPrograma(ctx);
        System.out.println("Se realizaron " + asignaciones + " asignaciones");
        return ret;
    }

    /**
     * Visita un nodo de asignación en el árbol sintáctico.
     * <p>
     * Este método se invoca cada vez que se encuentra una asignación durante
     * el recorrido. Incrementa el contador de asignaciones y muestra información
     * de debug sobre la estructura del nodo (número de hijos y el identificador
     * al que se está asignando el valor).
     * </p>
     * <p>
     * <b>Ejemplo de salida:</b><br>
     * {@code Soy una hoja y tengo 3 hijos}<br>
     * {@code    -> asignando valor a x}
     * </p>
     *
     * @param ctx el contexto del nodo {@code asignacion} del árbol sintáctico
     * @return el resultado del recorrido de los nodos hijos
     */
    @Override
    public String visitAsignacion(AsignacionContext ctx) {
        asignaciones++;
        System.out.println("Soy una hoja y tengo " + ctx.getChildCount() + " hijos");
        System.out.println("    -> asignando valor a " + ctx.getChild(0).getText());
        return super.visitAsignacion(ctx);
    }

    /**
     * Visita un nodo terminal (token) del árbol sintáctico.
     * <p>
     * Los nodos terminales representan los tokens del lexer (identificadores,
     * palabras clave, operadores, literales, etc.). Este método puede ser
     * sobrescrito para realizar acciones específicas al visitar tokens.
     * </p>
     * <p>
     * La implementación actual simplemente delega en la implementación base
     * sin realizar acciones adicionales.
     * </p>
     *
     * @param node el nodo terminal visitado
     * @return el resultado de la visita (por defecto, el texto del token)
     */
    @Override
    public String visitTerminal(TerminalNode node) {
        return super.visitTerminal(node);
    }
}