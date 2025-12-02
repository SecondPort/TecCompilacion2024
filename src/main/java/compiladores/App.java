package compiladores;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Clase principal del compilador que orquesta el proceso completo de compilación.
 * <p>
 * Esta clase actúa como punto de entrada de la aplicación y coordina todas las fases
 * del proceso de compilación:
 * <ol>
 *   <li><b>Análisis Léxico:</b> Creación del lexer para tokenizar el código fuente</li>
 *   <li><b>Análisis Sintáctico:</b> Creación del parser para construir el árbol sintáctico</li>
 *   <li><b>Análisis Semántico:</b> Conexión del listener para validaciones semánticas</li>
 *   <li><b>Generación del Árbol:</b> Construcción del árbol sintáctico (parse tree)</li>
 * </ol>
 * <p>
 * El flujo de compilación sigue el patrón de diseño <b>Pipeline</b>, donde cada
 * fase procesa la salida de la fase anterior:
 * {@code Archivo → Lexer → Tokens → Parser → Árbol Sintáctico → Validaciones}
 * </p>
 *
 * @author Compiladores 2024
 * @version 1.0
 * @since 1.0
 * @see compiladoresLexer
 * @see compiladoresParser
 * @see Escucha
 * @see Caminante
 */
public class App {
    /**
     * Método principal que ejecuta el proceso de compilación.
     * <p>
     * <b>Proceso de compilación:</b>
     * <ol>
     *   <li>Lee el archivo fuente desde el argumento de línea de comandos o usa {@code entrada/programa.txt} por defecto</li>
     *   <li>Crea un {@link compiladoresLexer} para análisis léxico</li>
     *   <li>Genera un stream de tokens a partir del lexer</li>
     *   <li>Crea un {@link compiladoresParser} que consume los tokens</li>
     *   <li>Conecta un {@link Escucha} (listener) para análisis semántico</li>
     *   <li>Inicia el parsing desde la regla {@code programa}</li>
     *   <li>Imprime la representación textual del árbol sintáctico</li>
     * </ol>
     * <p>
     * El método incluye código comentado que demuestra cómo integrar el
     * patrón Visitor ({@link Caminante}) para recorridos alternativos del árbol.
     * </p>
     *
     * @param args argumentos de línea de comandos - args[0] debe ser la ruta del archivo a compilar
     * @throws Exception si ocurre un error durante la lectura del archivo o el proceso
     *                   de compilación (ej: archivo no encontrado, errores de sintaxis graves)
     * @see CharStreams#fromFileName(String)
     * @see ParseTree#toStringTree(org.antlr.v4.runtime.Parser)
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, Compilador!!!");
        
        // Determinar el archivo de entrada
        String archivoEntrada = "entrada/programa.txt"; // Valor por defecto
        if (args.length > 0) {
            archivoEntrada = args[0];
            System.out.println("Compilando archivo: " + archivoEntrada);
        } else {
            System.out.println("No se proporcionó archivo de entrada, usando por defecto: " + archivoEntrada);
        }
        
        // Fase 1: Crear un CharStream que lee desde el archivo de entrada
        CharStream input = CharStreams.fromFileName(archivoEntrada);

        // Fase 2: Crear el lexer que analiza el CharStream y produce tokens
        compiladoresLexer lexer = new compiladoresLexer(input);
        
        // Fase 3: Crear un buffer de tokens generados por el lexer
        // Este buffer permite al parser consumir tokens según sea necesario
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        
        // Fase 4: Crear el parser que consume tokens y construye el árbol sintáctico
        compiladoresParser parser = new compiladoresParser(tokens);
                
        // Fase 5: Crear el Listener para análisis semántico (patrón Observer)
        compiladoresBaseListener escucha = new Escucha();

        // Fase 6: Conectar el listener al parser para recibir notificaciones
        // durante el recorrido del árbol
        parser.addParseListener(escucha);

        // Fase 7: Iniciar el parsing desde la regla gramatical 'programa' (símbolo inicial)
        // Esto construye el árbol sintáctico completo y dispara los eventos del listener
        ParseTree tree =  parser.programa();
        
        // Opción alternativa: Usar el patrón Visitor para recorridos personalizados
        // Caminante visitor = new Caminante();
        // visitor.visit(tree);
        
        // Fase 8: Imprimir la representación textual del árbol sintáctico generado
        System.out.println(tree.toStringTree(parser));
        
        // Fase 9: Generación de código ensamblador
        System.out.println("\n--- Iniciando generación de código ensamblador ---");
        GeneradorAssembler generador = new GeneradorAssembler("salida/programa.asm");
        generador.visit(tree);
        
    }
}
