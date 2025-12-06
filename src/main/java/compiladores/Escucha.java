package compiladores;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.Token;

import compiladores.compiladoresParser.AsignacionContext;
import compiladores.compiladoresParser.BloqueContext;
import compiladores.compiladoresParser.DeclaracionContext;
import compiladores.compiladoresParser.DeclaracionfuncContext;
import compiladores.compiladoresParser.FactorContext;
import compiladores.compiladoresParser.FactorfuncContext;
import compiladores.compiladoresParser.FinforContext;
import compiladores.compiladoresParser.IdfuncContext;
import compiladores.compiladoresParser.ListaidfuncContext;
import compiladores.compiladoresParser.ListafactfuncContext;
import compiladores.compiladoresParser.LlamadafuncContext;
import compiladores.compiladoresParser.ProgramaContext;
import compiladores.compiladoresParser.PrototipofuncContext;

/**
 * Implementación del patrón Listener de ANTLR4 para análisis semántico del compilador.
 * <p>
 * Esta clase extiende {@link compiladoresBaseListener} e implementa el patrón <b>Observer</b>
 * al recibir notificaciones automáticas cuando el parser entra o sale de reglas gramaticales.
 * Realiza validaciones semánticas y sintácticas durante el recorrido del árbol sintáctico.
 * </p>
 * <p>
 * <b>Responsabilidades principales:</b>
 * <ul>
 *   <li>Manejo de la tabla de símbolos y contextos (scopes)</li>
 *   <li>Validación de declaraciones y uso de identificadores</li>
 *   <li>Detección de errores semánticos (doble declaración, uso sin declaración, etc.)</li>
 *   <li>Detección de errores sintácticos (falta de punto y coma, llaves, etc.)</li>
 *   <li>Conteo de nodos, tokens y errores durante el parsing</li>
 * </ul>
 *
 * @author Compiladores 2024
 * @version 1.0
 * @since 1.0
 * @see compiladoresBaseListener
 * @see TablaSimbolos
 */
public class Escucha extends compiladoresBaseListener {
    /**
     * Contador de nodos visitados durante el recorrido del árbol sintáctico.
     * Incluye todos los nodos de reglas gramaticales (no terminales).
     */
    private Integer nodos = 0;
    
    /**
     * Contador de tokens (símbolos terminales) encontrados durante el parsing.
     */
    private Integer tokens = 0;
    
    /**
     * Contador de errores (sintácticos y semánticos) detectados durante la compilación.
     */
    private Integer errors = 0;
    
    /**
     * Instancia única de la tabla de símbolos para gestionar identificadores.
     * Utiliza el patrón Singleton obtenido mediante {@link TablaSimbolos#getInstancia()}.
     */
    private TablaSimbolos tabla = TablaSimbolos.getInstancia();

    /**
     * Reportador centralizado de mensajes (errores, warnings, información).
     */
    private final Reportador reportador = Reportador.getInstancia();

    /** Tipo de función actualmente en análisis, para declarar correctamente los parámetros. */
    private String tipoFuncionActual = null;

    /**
     * Se invoca al entrar al nodo raíz del programa (inicio del parsing).
     * <p>
     * Inicializa el contexto global de la tabla de símbolos donde se almacenarán
     * las declaraciones de nivel superior (variables y funciones globales).
     * </p>
     *
     * @param ctx el contexto del nodo {@code programa} del árbol sintáctico
     */
    @Override
    public void enterPrograma(final ProgramaContext ctx) {
        reportador.info("Comienza el parsing...", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        tabla.addContexto();
        super.enterPrograma(ctx);
    }

    /**
     * Se invoca al salir del nodo raíz del programa (fin del parsing).
     * <p>
     * Elimina el contexto global de la tabla de símbolos y muestra estadísticas
     * finales del proceso de compilación (nodos visitados, tokens encontrados).
     * </p>
     *
     * @param ctx el contexto del nodo {@code programa} del árbol sintáctico
     */
    @Override
    public void exitPrograma(final ProgramaContext ctx) {
        super.exitPrograma(ctx);
        tabla.delContexto();

        // Generar warnings por símbolos no usados o no inicializados
        tabla.paraCadaSimbolo(id -> {
            String nombre = id.getNombre();
            if (Boolean.FALSE.equals(id.getUsado())) {
                reportador.warning("Identificador declarado pero no usado: " + nombre,
                        ctx.getStop().getLine(), ctx.getStop().getCharPositionInLine());
            }
            if (Boolean.FALSE.equals(id.getInicializado())) {
                reportador.warning("Identificador declarado pero no inicializado: " + nombre,
                        ctx.getStop().getLine(), ctx.getStop().getCharPositionInLine());
            }
        });

        reportador.info("Fin de la compilacion", ctx.getStop().getLine(), ctx.getStop().getCharPositionInLine());
        reportador.info("Se visitaron " + nodos + " nodos", ctx.getStop().getLine(), ctx.getStop().getCharPositionInLine());
        reportador.info("Hay " + tokens + " tokens", ctx.getStop().getLine(), ctx.getStop().getCharPositionInLine());
    }

    /**
     * Se invoca al entrar a un bloque de código delimitado por llaves.
     * <p>
     * Crea un nuevo contexto (scope) en la tabla de símbolos para manejar
     * la visibilidad local de las variables declaradas dentro del bloque.
     * </p>
     *
     * @param ctx el contexto del nodo {@code bloque} del árbol sintáctico
     * @see #exitBloque(BloqueContext)
     */
    @Override
    public void enterBloque(BloqueContext ctx) {
        tabla.addContexto();
        super.enterBloque(ctx);
    }

    /**
     * Se invoca al salir de un bloque de código.
     * <p>
     * Valida que el bloque esté correctamente cerrado con llave de cierre '}' 
     * y elimina el contexto local de la tabla de símbolos, liberando todos
     * los identificadores declarados en ese scope.
     * </p>
     *
     * @param ctx el contexto del nodo {@code bloque} del árbol sintáctico
     * @see #enterBloque(BloqueContext)
     */
    @Override
    public void exitBloque(BloqueContext ctx) {
        super.exitBloque(ctx);
        // Validación de llaves correctas al finalizar un bloque
        Token lastToken = ctx.getStop();
        if (lastToken == null || !lastToken.getText().equals("}")) {
            reportador.error("Error sintactico: se esperaba '}' al final del bloque", ctx.getStop().getLine(), ctx.getStop().getCharPositionInLine());
            errors++;
        }
        tabla.delContexto();
    }

    /**
     * Al entrar a la definición de una función se crea un nuevo contexto
     * para sus parámetros y variables locales.
     */
    @Override
    public void enterDeclaracionfunc(DeclaracionfuncContext ctx) {
        super.enterDeclaracionfunc(ctx);
        // Abrimos un contexto propio de la función.
        tabla.addContexto();

        // Intentamos obtener tipo y nombre de la función si el parser los provee.
        tipoFuncionActual = (ctx.tipofunc() != null) ? ctx.tipofunc().getText() : null;
        String nombreFunc = (ctx.ID() != null) ? ctx.ID().getText() : "<sin_nombre>";
        System.out.println("[Escucha] enterDeclaracionfunc tipo=" + tipoFuncionActual + " nombre=" + nombreFunc);

        // Estrategia robusta: buscar nodos Idfunc/Listaidfunc en los hijos y
        // extraer los nombres de parámetros a partir de su texto.
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof IdfuncContext) {
                IdfuncContext idfunc = (IdfuncContext) ctx.getChild(i);
                System.out.println("[Escucha]   idfunc(raw) = " + idfunc.getText());

                // El texto típico será algo del estilo "inta,intb" o "inta,intb,intc".
                String raw = idfunc.getText();
                // Normalizamos separando por comas.
                String[] partes = raw.split(",");
                for (String parte : partes) {
                    // Cada parte es algo como "inta" o "intb" o "doublex" etc.
                    // Buscamos el último tramo de letras (nombre) al final.
                    String p = parte.trim();
                    if (p.isEmpty()) continue;

                    int idx = p.length() - 1;
                    while (idx >= 0 && Character.isLetter(p.charAt(idx))) {
                        idx--;
                    }
                    String nombreParam = p.substring(idx + 1);
                    String tipoParam = p.substring(0, idx + 1);

                    if (!nombreParam.isEmpty() && !tipoParam.isEmpty()) {
                        System.out.println("[Escucha]   param detectado tipo=" + tipoParam + " nombre=" + nombreParam);
                        declararParametro(tipoParam, nombreParam,
                                          idfunc.getStart().getLine(), idfunc.getStart().getCharPositionInLine());
                    }
                }
            }
        }
    }

    /**
     * El helper que declara un parámetro como variable inicializada en el contexto actual.
     */
    private void declararParametro(String tipo, String nombre, int linea, int columna) {
        if (tabla.contieneSimboloLocal(nombre)) {
            reportador.error("Error semantico: Doble declaracion de parametro " + nombre, linea, columna);
            errors++;
            return;
        }
        Variable param = new Variable();
        param.setNombre(nombre);
        param.setTipoDato(tipo);
        param.setInicializado(true); // parámetros se consideran inicializados
        param.setUsado(false);
        tabla.addSimbolo(nombre, param);
    }
    
    /**
     * Se invoca al salir de una declaración de variable.
     * <p>
     * Realiza las siguientes validaciones:
     * <ul>
     *   <li><b>Semántica:</b> Verifica que la variable no esté declarada en el contexto local
     *       (evita doble declaración)</li>
     *   <li><b>Sintáctica:</b> Verifica que la declaración termine con punto y coma ';'</li>
     * </ul>
     * <p>
     * Si la variable es válida, crea un objeto {@link Variable} con su información
     * (nombre, tipo, inicialización) y lo añade a la tabla de símbolos.
     * </p>
     */
    @Override
    public void exitDeclaracion(DeclaracionContext ctx) {
        super.exitDeclaracion(ctx);
    
        String nombre = ctx.ID().getText();
        // Validación semántica: no permitir doble declaración en el mismo bloque
        if (!tabla.contieneSimboloLocal(nombre)) {
            Variable nuevaVariable = new Variable();
            String tipo = ctx.getChild(0).getText();  // Obtención del tipo de la variable
    
            nuevaVariable.setNombre(nombre);
            nuevaVariable.setTipoDato(tipo);
            nuevaVariable.setInicializado(!ctx.getChild(2).getText().isBlank());
            tabla.addSimbolo(nombre, nuevaVariable);
        } else {
            reportador.error("Error semantico: Doble declaracion del mismo identificador", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
            errors++;
        }
    }
    
    /**
     * Se invoca al salir del prototipo de una función (declaración sin cuerpo).
     * <p>
     * Valida que la función no esté declarada previamente en el contexto local
     * (evita doble declaración). Si es válida, crea un objeto {@link Funcion}
     * con su información (nombre, tipo de retorno) y lo añade a la tabla de símbolos.
     * <p>
     * Los prototipos son declaraciones adelantadas (forward declarations) que permiten
     * usar funciones antes de su definición completa.
     * </p>
     *
     * @param ctx el contexto del nodo {@code prototipofunc} del árbol sintáctico
     * @see #exitDeclaracionfunc(DeclaracionfuncContext)
     */
    @Override
    public void exitPrototipofunc(PrototipofuncContext ctx) {
        super.exitPrototipofunc(ctx);

        String nombre = ctx.ID().getText();
        Id existente = tabla.getSimbolo(nombre);

        // Si no existe aún, lo registramos como prototipo
        if (existente == null) {
            Funcion nuevaFuncion = new Funcion();
            String tipo = ctx.getChild(0).getText();
            nuevaFuncion.setNombre(nombre);
            nuevaFuncion.setTipoDato(tipo);
            nuevaFuncion.setUsado(false);
            tabla.addSimbolo(nombre, nuevaFuncion);
        } else if (existente instanceof Funcion) {
            // Si ya hay una función con el mismo nombre, verificamos compatibilidad básica de tipo
            if (!existente.getTipoDato().equals(ctx.getChild(0).getText())) {
                reportador.error("Error semantico: Prototipo incompatible con la funcion existente", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            }
            // No consideramos error un prototipo repetido compatible
        } else {
            // Existe un símbolo no función con el mismo nombre
            reportador.error("Error semantico: Identificador ya usado con otro proposito", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
            errors++;
        }
    }

    /**
     * Se invoca al salir de una declaración completa de función (con cuerpo).
     * <p>
     * Valida que la función no esté declarada previamente en el contexto local
     * (evita doble declaración). Si es válida, crea un objeto {@link Funcion}
     * con su información (nombre, tipo de retorno) y lo añade a la tabla de símbolos.
     * </p>
     * <p>
     * A diferencia de {@link #exitPrototipofunc(PrototipofuncContext)}, esta función
     * incluye el cuerpo (implementación) de la función.
     * </p>
     *
     * @see #exitPrototipofunc(PrototipofuncContext)
     */
    @Override
    public void exitDeclaracionfunc(DeclaracionfuncContext ctx) {
        super.exitDeclaracionfunc(ctx);
        if (ctx.ID() != null) {
            String nombre = ctx.ID().getText();
            Id existente = tabla.getSimbolo(nombre);

            if (existente == null) {
                // Definición sin prototipo previo
                Funcion nuevaFuncion = new Funcion();
                String tipo = ctx.getChild(0).getText();
                nuevaFuncion.setNombre(nombre);
                nuevaFuncion.setTipoDato(tipo);
                nuevaFuncion.setUsado(false);
                tabla.addSimbolo(nombre, nuevaFuncion);
            } else if (existente instanceof Funcion) {
                // Ya había un prototipo; verificamos que el tipo sea compatible
                if (!existente.getTipoDato().equals(ctx.getChild(0).getText())) {
                    reportador.error("Error semantico: Tipo de retorno distinto al del prototipo", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                    errors++;
                }
                // No lo tratamos como doble declaración; es la definición esperada
            } else {
                reportador.error("Error semantico: Identificador ya usado como variable", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            }
        }

        // Al salir de la función cerramos el contexto de parámetros/variables locales
        tabla.delContexto();
        tipoFuncionActual = null;
    }

    /**
     * Se invoca al salir de una asignación de valor a una variable.
     * <p>
     * Realiza las siguientes validaciones:
     * <ul>
     *   <li><b>Semántica:</b> Verifica que la variable esté declarada previamente</li>
     *   <li><b>Semántica:</b> Verifica que la variable esté inicializada antes de su uso</li>
     *   <li><b>Sintáctica:</b> Verifica que la asignación termine con punto y coma ';'</li>
     * </ul>
     * <p>
     * Los errores se acumulan en una lista temporal y se imprimen en conjunto
     * al final del método para mantener la salida organizada.
     * </p>
     *
     * @param ctx el contexto del nodo {@code asignacion} del árbol sintáctico
     */
    @Override
    public void exitAsignacion(AsignacionContext ctx) {
        super.exitAsignacion(ctx);

        // Validación semántica de la asignación
        if (ctx.ID() != null) {
            Id simbolo = tabla.getSimbolo(ctx.ID().getText());
            if (simbolo == null) {
                // La variable destino debe estar declarada previamente
                reportador.error("Error semantico: Uso de un identificador no declarado", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            } else {
                // La variable destino está siendo asignada, marcarla como inicializada.
                // La validación de variables no inicializadas en el lado derecho (expresión)
                // se realiza en exitFactor() cuando se evalúa cada operando.
                simbolo.setInicializado(true);
            }
        }
    }

    /**
     * Se invoca al salir de un factor en una expresión.
     * <p>
     * Los factores son elementos básicos de expresiones (variables, constantes, etc.).
     * Si el factor es un identificador, valida que:
     * <ul>
     *   <li>El identificador esté declarado previamente</li>
     *   <li>El identificador esté inicializado antes de su uso</li>
     * </ul>
     *
     * @param ctx el contexto del nodo {@code factor} del árbol sintáctico
     */
    @Override
    public void exitFactor(FactorContext ctx) {
        super.exitFactor(ctx);
        if (ctx.ID() != null) {
            Id simbolo = tabla.getSimbolo(ctx.ID().getText());
            if (simbolo == null) {
                System.out.println("[Escucha] exitFactor ID='" + ctx.ID().getText() + "' -> no declarado");
                reportador.error("Error semantico: Uso de un identificador no declarado", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            } else {
                if (Boolean.FALSE.equals(simbolo.getInicializado())
                        && !(ctx.getParent() instanceof compiladoresParser.AsignacionContext
                             && ((compiladoresParser.AsignacionContext) ctx.getParent()).ID() == ctx.ID())) {
                    reportador.error("Error semantico: Uso de un identificador no inicializado", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                    errors++;
                }
                simbolo.setUsado(true);
            }
        }  
    }
    
    /**
     * Se invoca al salir de la expresión final de un bucle for.
     * <p>
     * Valida que cualquier identificador usado en la expresión de incremento/decremento
     * del bucle for esté declarado e inicializado.
     * </p>
     *
     * @param ctx el contexto del nodo {@code finfor} del árbol sintáctico
     */
    @Override
    public void exitFinfor(FinforContext ctx) {
        super.exitFinfor(ctx);
       if (ctx.ID() != null) {
            Id simbolo = tabla.getSimbolo(ctx.ID().getText());
            if (simbolo == null) {
                reportador.error("Error semantico: Uso de un identificador no declarado", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            }
            else if (Boolean.FALSE.equals(simbolo.getInicializado())) {
                reportador.error("Error semantico: Uso de un identificador no inicializado", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            } else {
                simbolo.setUsado(true);
            }
        } 
    }

    /**
     * Se invoca al salir de un factor que puede ser una función o variable en contexto de llamada.
     * <p>
     * Valida que el identificador referenciado (variable o función) esté:
     * <ul>
     *   <li>Declarado previamente en algún contexto visible</li>
     *   <li>Inicializado antes de su uso</li>
     * </ul>
     *
     * @param ctx el contexto del nodo {@code factorfunc} del árbol sintáctico
     */
    @Override
    public void exitFactorfunc(FactorfuncContext ctx) {
        super.exitFactorfunc(ctx);
        if (ctx.ID() != null) {
            Id simbolo = tabla.getSimbolo(ctx.ID().getText());
            if (simbolo == null) {
                System.out.println("[Escucha] exitFactorfunc ID='" + ctx.ID().getText() + "' -> no declarado");
                reportador.error("Error semantico: Uso de un identificador no declarado", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            }
            else if (Boolean.FALSE.equals(simbolo.getInicializado())) {
                reportador.error("Error semantico: Uso de un identificador no inicializado", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            } else {
                simbolo.setUsado(true);
            }
        } 
    }

    /**
     * Se invoca al salir de un elemento en la lista de argumentos de una llamada a función.
     * <p>
     * Valida que cada identificador usado como argumento en la llamada a función esté:
     * <ul>
     *   <li>Declarado previamente</li>
     *   <li>Inicializado antes de ser pasado como argumento</li>
     * </ul>
     *
     * @param ctx el contexto del nodo {@code listafactfunc} del árbol sintáctico
     */
    @Override
    public void exitListafactfunc(ListafactfuncContext ctx) {
        super.exitListafactfunc(ctx);
        if (ctx.ID() != null) {
            Id simbolo = tabla.getSimbolo(ctx.ID().getText());
            if (simbolo == null) {
                reportador.error("Error semantico: Uso de un identificador no declarado", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            }
            else if (Boolean.FALSE.equals(simbolo.getInicializado())) {
                reportador.error("Error semantico: Uso de un identificador no inicializado", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            } else {
                simbolo.setUsado(true);
            }
        } 
    }

    /**
     * Se invoca al salir de una llamada a función.
     * <p>
     * Valida que la función llamada esté:
     * <ul>
     *   <li>Declarada previamente (como prototipo o definición completa)</li>
     *   <li>Inicializada (en el caso de variables de tipo función o punteros a función)</li>
     * </ul>
     *
     * @param ctx el contexto del nodo {@code llamadafunc} del árbol sintáctico
     */
    @Override
    public void exitLlamadafunc(LlamadafuncContext ctx) {
        super.exitLlamadafunc(ctx);
       if (ctx.llamada_expr() != null && ctx.llamada_expr().ID() != null) {
            Id simbolo = tabla.getSimbolo(ctx.llamada_expr().ID().getText());
            if (simbolo == null) {
                reportador.error("Error semantico: Uso de un identificador no declarado", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;             
            }
            else if (Boolean.FALSE.equals(simbolo.getInicializado())) {
                reportador.error("Error semantico: Uso de un identificador no inicializado", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            } else {
                simbolo.setUsado(true);
            }
        } 
    }

    /**
     * Se invoca automáticamente al entrar a cualquier regla del parser.
     * <p>
     * Incrementa el contador de nodos visitados. Este método es parte del
     * mecanismo de monitoreo del proceso de parsing, proporcionando estadísticas
     * sobre la complejidad del árbol sintáctico generado.
     * </p>
     *
     * @param ctx el contexto de cualquier regla del parser
     */
    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        nodos++;
        super.enterEveryRule(ctx);
    }

    /**
     * Se invoca cuando el parser encuentra un nodo de error.
     * <p>
     * Un nodo de error se crea cuando ANTLR detecta tokens inesperados
     * o estructuras sintácticas inválidas que no coinciden con la gramática.
     * Este método incrementa el contador de errores.
     * </p>
     *
     * @param node el nodo de error detectado por el parser
     */
    @Override
    public void visitErrorNode(ErrorNode node) {
        errors++;
        super.visitErrorNode(node);
    }

    /**
     * Se invoca al visitar un nodo terminal (token) del árbol sintáctico.
     * <p>
     * Los nodos terminales representan los tokens del lexer (palabras clave,
     * identificadores, operadores, literales, etc.). Este método incrementa
     * el contador de tokens para estadísticas del proceso de compilación.
     * </p>
     *
     * @param node el nodo terminal visitado
     */
    @Override
    public void visitTerminal(TerminalNode node) {
        tokens++;
        super.visitTerminal(node);
    } 
    
}
