package compiladores;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.Token;

import compiladores.compiladoresParser.AsignacionContext;
import compiladores.compiladoresParser.BloqueContext;
import compiladores.compiladoresParser.DeclaracionContext;
import compiladores.compiladoresParser.DeclaracionfuncContext;
import compiladores.compiladoresParser.IbreakContext;
import compiladores.compiladoresParser.IcontinueContext;
import compiladores.compiladoresParser.IforContext;
import compiladores.compiladoresParser.IwhileContext;
import compiladores.compiladoresParser.FactorContext;
import compiladores.compiladoresParser.FactorfuncContext;
import compiladores.compiladoresParser.FinforContext;
import compiladores.compiladoresParser.IdfuncContext;
import compiladores.compiladoresParser.ListaidfuncContext;
import compiladores.compiladoresParser.ListafactfuncContext;
import compiladores.compiladoresParser.LlamadafuncContext;
import compiladores.compiladoresParser.Llamada_exprContext;
import compiladores.compiladoresParser.ExpresionContext;
import compiladores.compiladoresParser.IreturnContext;
import compiladores.compiladoresParser.ProgramaContext;
import compiladores.compiladoresParser.PrototipofuncContext;
import java.util.List;
import java.util.stream.Collectors;

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

    /** Tipo de función actualmente en análisis, para validar retornos si se requiere. */
    private TipoDato tipoFuncionActual = null;
    /** Indica si estamos recorriendo la definición (con cuerpo) de una función. */
    private boolean enDeclaracionFuncion = false;
    /** Tipos inferidos por nodo de expresión/factor. */
    private final ParseTreeProperty<TipoDato> tipos = new ParseTreeProperty<>();

    /** Profundidad actual de estructuras repetitivas para validar break/continue. */
    private int profundidadBucles = 0;
    /** Marca si se encontró al menos un return en la función actual. */
    private boolean retornoEncontrado = false;

    /** Devuelve true si el tipo es numérico (no void). */
    private boolean esTipoNumerico(TipoDato t) {
        return t == TipoDato.INT || t == TipoDato.DOUBLE || t == TipoDato.CHAR || t == TipoDato.BOOL;
    }

    /** Promoción numérica simple: char->int->double. */
    private TipoDato promocionar(TipoDato a, TipoDato b) {
        if (a == null || b == null) {
            return null;
        }
        if (a == TipoDato.DOUBLE || b == TipoDato.DOUBLE) {
            return TipoDato.DOUBLE;
        }
        if (a == TipoDato.INT || b == TipoDato.INT) {
            return TipoDato.INT;
        }
        if (a == TipoDato.BOOL || b == TipoDato.BOOL) {
            return TipoDato.BOOL;
        }
        return TipoDato.CHAR;
    }


    /** Valida asignación/coerción implícita ancho -> angosto. */
    private boolean puedeAsignar(TipoDato destino, TipoDato origen) {
        if (destino == null || origen == null) {
            return false;
        }
        if (destino == origen) {
            return true;
        }
        switch (destino) {
            case DOUBLE:
                return origen == TipoDato.INT || origen == TipoDato.CHAR;
            case INT:
                return origen == TipoDato.CHAR || origen == TipoDato.BOOL;
            case BOOL:
                return origen == TipoDato.BOOL || origen == TipoDato.CHAR || origen == TipoDato.INT;
            default:
                return false;
        }
    }

    /** Infere el tipo de un literal NUMERO según contenga decimal. */
    private TipoDato tipoNumero(String lexema) {
        if (lexema != null && lexema.contains(".")) {
            return TipoDato.DOUBLE;
        }
        return TipoDato.INT;
    }

    private String toLexema(TipoDato tipo) {
        if (tipo == null) {
            return "-";
        }
        switch (tipo) {
            case INT: return "int";
            case DOUBLE: return "double";
            case CHAR: return "char";
            case BOOL: return "bool";
            case VOID: return "void";
            default: return tipo.toString().toLowerCase();
        }
    }

    private String formatearFirma(java.util.List<TipoDato> tiposArgs) {
        if (tiposArgs == null || tiposArgs.isEmpty()) {
            return "()";
        }
        return "(" + tiposArgs.stream().map(this::toLexema).collect(Collectors.joining(", ")) + ")";
    }

    private String detalleFuncion(TipoDato retorno, java.util.List<TipoDato> firma, String etiqueta) {
        String detalle = "retorno=" + toLexema(retorno) + " args=" + formatearFirma(firma);
        if (etiqueta != null && !etiqueta.isEmpty()) {
            detalle += " " + etiqueta;
        }
        return detalle;
    }

    /** Infere tipo para literales booleanos. */
    private TipoDato tipoBooleano(String lexema) {
        if (lexema == null) return null;
        if (lexema.equals("true") || lexema.equals("false")) {
            return TipoDato.BOOL;
        }
        return null;
    }

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
            if (!(id instanceof Variable)) {
                return; // No advertimos sobre funciones/prototipos.
            }
            String nombre = id.getNombre();
            int linea = id.getLinea() >= 0 ? id.getLinea() : ctx.getStop().getLine();
            int columna = id.getColumna() >= 0 ? id.getColumna() : ctx.getStop().getCharPositionInLine();
            if (Boolean.FALSE.equals(id.getUsado())) {
                reportador.warning("Identificador declarado pero no usado: " + nombre,
                        linea, columna);
            }
            if (Boolean.FALSE.equals(id.getInicializado())) {
                reportador.warning("Identificador declarado pero no inicializado: " + nombre,
                        linea, columna);
            }
        });

        tabla.imprimirTablaSimbolos();

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
     * para sus parámetros y variables locales, y se registran correctamente
     * los parámetros definidos en la gramática (idfunc/listaidfunc).
     */
    @Override
    public void enterDeclaracionfunc(DeclaracionfuncContext ctx) {
        super.enterDeclaracionfunc(ctx);

        // Abrimos un contexto propio de la función.
        tabla.addContexto();
        enDeclaracionFuncion = true;
        retornoEncontrado = false;

        // Tipo y nombre de la función (primer token del contexto).
        String tipoFuncLexema = ctx.getStart() != null ? ctx.getStart().getText() : null;
        tipoFuncionActual = resolverTipo(tipoFuncLexema);

        if (tipoFuncionActual == null) {
            reportador.error("Error semantico: Tipo de retorno desconocido", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
            errors++;
            tipoFuncionActual = TipoDato.INT; // Fallback para evitar cascada de errores
        }

        String nombreFunc = (ctx.ID() != null)
            ? ctx.ID().getText()
            : extraerPrimerId(ctx);
        if ("<sin_nombre>".equals(nombreFunc) && ctx.getChildCount() > 1) {
            nombreFunc = ctx.getChild(1).getText();
        }

        // Registrar la función en el ámbito global para permitir referencias dentro de su propio cuerpo.
        if (nombreFunc != null && !"<sin_nombre>".equals(nombreFunc)) {
            Id existente = tabla.getSimboloGlobal(nombreFunc);
            if (existente == null) {
                Funcion f = new Funcion();
                f.setNombre(nombreFunc);
                f.setTipoDato(tipoFuncionActual);
                f.setInicializado(true);
                f.setUsado(false);
                f.setArgumentos(extraerTiposParametros(ctx.idfunc()));
                f.setLinea(ctx.getStart().getLine());
                f.setColumna(ctx.getStart().getCharPositionInLine());
                f.setCategoria("funcion");
                f.setAmbito("global");
                f.setDetalles(detalleFuncion(tipoFuncionActual, f.getArgumentos(), "definicion"));
                tabla.addSimboloGlobal(nombreFunc, f);
            }
        }

        System.out.println("[Escucha] enterDeclaracionfunc tipo=" + tipoFuncionActual + " nombre=" + nombreFunc);
    }

    /**
     * Busca de manera defensiva el primer nodo ID dentro del contexto de la función.
     */
    private String extraerPrimerId(ParserRuleContext ctx) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) ctx.getChild(i);
                if (tn.getSymbol().getType() == compiladoresParser.ID) {
                    return tn.getText();
                }
            }
        }
        return "<sin_nombre>";
    }

    /**
     * Busca un nodo Idfunc dentro de los hijos si el getter directo regresa null.
     */
    private IdfuncContext buscarIdfunc(ParserRuleContext ctx) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof IdfuncContext) {
                return (IdfuncContext) ctx.getChild(i);
            }
        }
        return null;
    }

    /** Traduce el lexema de tipo a {@link TipoDato}. */
    private TipoDato resolverTipo(String lexema) {
        if (lexema == null) {
            return null;
        }
        return TablaSimbolos.parseTipoDato(lexema.trim());
    }

    /** Obtiene la lista de tipos declarados para los parámetros de una función. */
    private java.util.List<TipoDato> extraerTiposParametros(IdfuncContext idfunc) {
        java.util.List<TipoDato> tipos = new java.util.ArrayList<>();
        if (idfunc == null) {
            return tipos;
        }

        if (idfunc.tipo() != null) {
            tipos.add(resolverTipo(idfunc.tipo().getText()));
        }

        ListaidfuncContext lista = idfunc.listaidfunc();
        while (lista != null && lista.getChildCount() > 0) {
            if (lista.tipo() != null) {
                tipos.add(resolverTipo(lista.tipo().getText()));
            }
            lista = lista.listaidfunc();
        }
        return tipos;
    }

    private boolean firmasCompatibles(Funcion funcion, TipoDato retorno, java.util.List<TipoDato> firma) {
        if (funcion == null) {
            return false;
        }
        if (retorno != null && funcion.getTipoDato() != null && funcion.getTipoDato() != retorno) {
            return false;
        }
        if (firma == null || funcion.getArgumentos() == null) {
            return true;
        }
        if (funcion.getArgumentos().size() != firma.size()) {
            return false;
        }
        for (int i = 0; i < firma.size(); i++) {
            if (funcion.getArgumentos().get(i) != firma.get(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Registra todos los parámetros de una definición de función en el contexto actual.
     */
    private void registrarParametros(IdfuncContext idfunc) {
        if (idfunc == null) {
            return;
        }

        // Caso vacío: sin parámetros (idfunc ::= epsilon).
        if (idfunc.getChildCount() == 0) {
            return;
        }

        // Primer parámetro (variante tipo ID listaidfunc).
        if (idfunc.tipo() != null && idfunc.ID() != null) {
            declararParametro(resolverTipo(idfunc.tipo().getText()), idfunc.ID().getText(),
                    idfunc.getStart().getLine(), idfunc.getStart().getCharPositionInLine());
        }

        // Parámetros adicionales en listaidfunc.
        ListaidfuncContext lista = idfunc.listaidfunc();
        while (lista != null) {
            if (lista.tipo() != null && lista.ID() != null) {
                declararParametro(resolverTipo(lista.tipo().getText()), lista.ID().getText(),
                        lista.getStart().getLine(), lista.getStart().getCharPositionInLine());
            }
            lista = lista.listaidfunc();
        }
    }

    /**
     * Declara un parámetro como variable inicializada en el contexto actual.
     */
    private void declararParametro(TipoDato tipo, String nombre, int linea, int columna) {
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
        param.setLinea(linea);
        param.setColumna(columna);
        param.setCategoria("parametro");
        param.setAmbito("parametros");
        param.setDetalles("parametro de funcion");
        tabla.addSimbolo(nombre, param);
    }

    /** Devuelve la lista de tipos de argumentos en una llamada. */
    private java.util.List<TipoDato> recolectarTiposArgumentos(FactorfuncContext ctx) {
        java.util.List<TipoDato> tiposArgs = new java.util.ArrayList<>();
        if (ctx == null) {
            return tiposArgs;
        }

        // Primer argumento
        if (ctx.NUMERO() != null) {
            tiposArgs.add(tipoNumero(ctx.NUMERO().getText()));
        } else if (ctx.ID() != null) {
            Id s = tabla.getSimbolo(ctx.ID().getText());
            tiposArgs.add(s != null ? s.getTipoDato() : null);
        } else if (ctx.expresion() != null) {
            tiposArgs.add(tipos.get(ctx.expresion()));
        }

        // Argumentos adicionales
        ListafactfuncContext lista = ctx.listafactfunc();
        while (lista != null && lista.getChildCount() > 0) {
            if (lista.NUMERO() != null) {
                tiposArgs.add(tipoNumero(lista.NUMERO().getText()));
            } else if (lista.ID() != null) {
                Id s = tabla.getSimbolo(lista.ID().getText());
                tiposArgs.add(s != null ? s.getTipoDato() : null);
            } else if (lista.expresion() != null) {
                tiposArgs.add(tipos.get(lista.expresion()));
            }
            lista = lista.listafactfunc();
        }
        return tiposArgs;
    }

    /** Valida la llamada y retorna el tipo de retorno de la función. */
    private TipoDato validarLlamada(String nombre, FactorfuncContext argsCtx, int linea, int columna) {
        Id simbolo = tabla.getSimbolo(nombre);
        if (simbolo == null) {
            reportador.error("Error semantico: Uso de un identificador no declarado", linea, columna);
            errors++;
            return null;
        }
        simbolo.setUsado(true);
        if (!(simbolo instanceof Funcion)) {
            reportador.error("Error semantico: Identificador no es funcion", linea, columna);
            errors++;
            return simbolo.getTipoDato();
        }

        Funcion f = (Funcion) simbolo;
        java.util.List<TipoDato> tiposArgs = recolectarTiposArgumentos(argsCtx);
        List<TipoDato> firma = f.getArgumentos();
        if (firma != null && tiposArgs.size() != firma.size()) {
            reportador.error("Error semantico: Cantidad de argumentos incompatible con la firma", linea, columna);
            errors++;
        }
        if (firma != null) {
            int limite = Math.min(firma.size(), tiposArgs.size());
            for (int i = 0; i < limite; i++) {
                TipoDato esperado = firma.get(i);
                TipoDato recibido = tiposArgs.get(i);
                if (!puedeAsignar(esperado, recibido)) {
                    reportador.error("Error semantico: Tipo de argumento incompatible en posicion " + (i + 1), linea, columna);
                    errors++;
                }
            }
        }
        return f.getTipoDato();
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
            TipoDato tipo = resolverTipo(ctx.getChild(0).getText());
            if (tipo == null) {
                reportador.error("Error semantico: Tipo de dato desconocido", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
                return;
            }

            boolean hayInicializacion = ctx.inicializacion() != null && ctx.inicializacion().getChildCount() > 0;
            boolean esGlobal = tabla.estaEnContextoGlobal();
            TipoDato tipoInicializacion = null;
            if (hayInicializacion && ctx.inicializacion().expresion() != null) {
                tipoInicializacion = tipos.get(ctx.inicializacion().expresion());
                if (tipoInicializacion != null && !puedeAsignar(tipo, tipoInicializacion)) {
                    reportador.error("Error semantico: Tipo incompatible en inicializacion", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                    errors++;
                }
            }

            nuevaVariable.setNombre(nombre);
            nuevaVariable.setTipoDato(tipo);
            // En C las variables globales se consideran inicializadas en cero
            nuevaVariable.setInicializado(esGlobal || hayInicializacion);
            nuevaVariable.setUsado(false);
            nuevaVariable.setLinea(ctx.getStart().getLine());
            nuevaVariable.setColumna(ctx.getStart().getCharPositionInLine());
            nuevaVariable.setCategoria("variable");
            nuevaVariable.setAmbito(esGlobal ? "global" : "local");
            nuevaVariable.setDetalles("inicializado=" + (esGlobal || hayInicializacion));
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
        Id existente = tabla.getSimboloGlobal(nombre);
        TipoDato tipoRetorno = resolverTipo(ctx.getChild(0).getText());
        java.util.List<TipoDato> firma = extraerTiposParametros(ctx.idfunc());

        if (tipoRetorno == null) {
            reportador.error("Error semantico: Tipo de retorno desconocido", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
            errors++;
            return;
        }
        // Si no existe aún, lo registramos como prototipo
        if (existente == null) {
            Funcion nuevaFuncion = new Funcion();
            nuevaFuncion.setNombre(nombre);
            nuevaFuncion.setTipoDato(tipoRetorno);
            nuevaFuncion.setUsado(false);
            nuevaFuncion.setInicializado(true);
            nuevaFuncion.setArgumentos(firma);
            nuevaFuncion.setLinea(ctx.getStart().getLine());
            nuevaFuncion.setColumna(ctx.getStart().getCharPositionInLine());
            nuevaFuncion.setCategoria("prototipo");
            nuevaFuncion.setAmbito("global");
            nuevaFuncion.setDetalles(detalleFuncion(tipoRetorno, firma, "prototipo"));
            tabla.addSimboloGlobal(nombre, nuevaFuncion);
        } else if (existente instanceof Funcion) {
            // Si ya hay una función con el mismo nombre, verificamos compatibilidad básica de tipo
            if (!firmasCompatibles((Funcion) existente, tipoRetorno, firma)) {
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

    @Override
    public void enterIwhile(IwhileContext ctx) {
        super.enterIwhile(ctx);
        profundidadBucles++;
    }

    @Override
    public void exitIwhile(IwhileContext ctx) {
        super.exitIwhile(ctx);
        profundidadBucles = Math.max(0, profundidadBucles - 1);
    }

    @Override
    public void enterIfor(IforContext ctx) {
        super.enterIfor(ctx);
        profundidadBucles++;
    }

    @Override
    public void exitIfor(IforContext ctx) {
        super.exitIfor(ctx);
        profundidadBucles = Math.max(0, profundidadBucles - 1);
    }

    @Override
    public void exitIbreak(IbreakContext ctx) {
        super.exitIbreak(ctx);
        if (profundidadBucles <= 0) {
            reportador.error("Error semantico: break fuera de un bucle", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
            errors++;
        }
    }

    @Override
    public void exitIcontinue(IcontinueContext ctx) {
        super.exitIcontinue(ctx);
        if (profundidadBucles <= 0) {
            reportador.error("Error semantico: continue fuera de un bucle", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
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
        String nombre = ctx.ID() != null ? ctx.ID().getText() : extraerPrimerId(ctx);
        if ("<sin_nombre>".equals(nombre) && ctx.getChildCount() > 1) {
            nombre = ctx.getChild(1).getText();
        }
        if (nombre != null && !"<sin_nombre>".equals(nombre)) {
            Id existente = tabla.getSimboloGlobal(nombre);
            TipoDato tipoRetorno = resolverTipo(ctx.getChild(0).getText());
            java.util.List<TipoDato> firma = extraerTiposParametros(ctx.idfunc());

            if (tipoRetorno == null) {
                reportador.error("Error semantico: Tipo de retorno desconocido", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
                return;
            }
            // se permite double en parámetros

            if (existente == null) {
                // Definición sin prototipo previo
                Funcion nuevaFuncion = new Funcion();
                nuevaFuncion.setNombre(nombre);
                nuevaFuncion.setTipoDato(tipoRetorno);
                nuevaFuncion.setUsado(false);
                nuevaFuncion.setInicializado(true);
                nuevaFuncion.setArgumentos(firma);
                nuevaFuncion.setLinea(ctx.getStart().getLine());
                nuevaFuncion.setColumna(ctx.getStart().getCharPositionInLine());
                nuevaFuncion.setCategoria("funcion");
                nuevaFuncion.setAmbito("global");
                nuevaFuncion.setDetalles(detalleFuncion(tipoRetorno, firma, "definicion"));
                tabla.addSimboloGlobal(nombre, nuevaFuncion);
            } else if (existente instanceof Funcion) {
                // Ya había un prototipo; verificamos que el tipo sea compatible
                if (!firmasCompatibles((Funcion) existente, tipoRetorno, firma)) {
                    reportador.error("Error semantico: Tipo de retorno o firma distinta al prototipo", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                    errors++;
                } else if (((Funcion) existente).getArgumentos() == null) {
                    ((Funcion) existente).setArgumentos(firma);
                }
            } else {
                reportador.error("Error semantico: Identificador ya usado como variable", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            }
        }

        // Validar que funciones no void tengan al menos un return
        if (tipoFuncionActual != null && tipoFuncionActual != TipoDato.VOID && !retornoEncontrado) {
            reportador.error("Error semantico: Funcion sin sentencia return", ctx.getStop().getLine(), ctx.getStop().getCharPositionInLine());
            errors++;
        }

        // Al salir de la función cerramos el contexto de parámetros/variables locales
        tabla.delContexto();
        tipoFuncionActual = null;
        enDeclaracionFuncion = false;
        retornoEncontrado = false;
    }

    /**
     * Se invoca al salir de la regla idfunc y registra parámetros únicamente
     * cuando se está dentro de una declaración de función (no en prototipos).
     */
    @Override
    public void exitIdfunc(IdfuncContext ctx) {
        super.exitIdfunc(ctx);
        if (!enDeclaracionFuncion) {
            return; // Evita registrar parámetros en prototipos u otros usos.
        }
        registrarParametros(ctx);
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
                Id simboloGlobal = tabla.getSimboloGlobal(ctx.ID().getText());
                if (simboloGlobal instanceof Funcion) {
                    reportador.error("Error semantico: No se puede asignar a una funcion", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                    errors++;
                    return;
                }
                // La variable destino debe estar declarada previamente
                reportador.error("Error semantico: Uso de un identificador no declarado", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            } else if (simbolo instanceof Funcion) {
                reportador.error("Error semantico: No se puede asignar a una funcion", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            } else {
                TipoDato tipoDestino = simbolo.getTipoDato();
                TipoDato tipoOrigen = tipos.get(ctx.expresion());
                if (tipoOrigen == null || tipoDestino == null || !puedeAsignar(tipoDestino, tipoOrigen)) {
                    reportador.error("Error semantico: Tipo incompatible en asignacion", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                    errors++;
                }
                simbolo.setInicializado(true);
            }
        }
    }

    @Override
    public void exitExpresion(ExpresionContext ctx) {
        super.exitExpresion(ctx);

        // Caso base: factor
        if (ctx.factor() != null) {
            tipos.put(ctx, tipos.get(ctx.factor()));
            return;
        }

        // Unarios: -expr o !expr
        if (ctx.getChildCount() == 2 && ctx.expresion().size() == 1) {
            TipoDato t = tipos.get(ctx.expresion(0));
            if (!esTipoNumerico(t)) {
                reportador.error("Error semantico: Operador unario aplicado a tipo no numerico", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            }
            tipos.put(ctx, TipoDato.INT); // boolean modelado como int
            return;
        }

        // Binarios
        if (ctx.expresion().size() == 2) {
            TipoDato izq = tipos.get(ctx.expresion(0));
            TipoDato der = tipos.get(ctx.expresion(1));
            String op = ctx.getChild(1).getText();

            if (op.equals("&&") || op.equals("||")) {
                if (!esTipoNumerico(izq) || !esTipoNumerico(der)) {
                    reportador.error("Error semantico: Operador logico requiere operandos numericos", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                    errors++;
                }
                tipos.put(ctx, TipoDato.INT);
                return;
            }

            if (op.equals("==") || op.equals("!=") || op.equals(">") || op.equals("<")
                    || op.equals(">=") || op.equals("<=")) {
                if (!esTipoNumerico(izq) || !esTipoNumerico(der)) {
                    reportador.error("Error semantico: Comparacion requiere operandos numericos", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                    errors++;
                }
                tipos.put(ctx, TipoDato.INT);
                return;
            }

            // Aritmeticos
            if (!esTipoNumerico(izq) || !esTipoNumerico(der)) {
                reportador.error("Error semantico: Operacion aritmetica requiere operandos numericos", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
                tipos.put(ctx, null);
                return;
            }
            tipos.put(ctx, promocionar(izq, der));
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
        TipoDato tipo = null;
        if (ctx.ID() != null) {
            String nombre = ctx.ID().getText();
            Id simbolo = tabla.getSimbolo(nombre);
            if (simbolo == null) {
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
                tipo = simbolo.getTipoDato();
            }
        } else if (ctx.NUMERO() != null) {
            tipo = tipoNumero(ctx.NUMERO().getText());
        } else if (ctx.CHAR_CONST() != null) {
            tipo = TipoDato.CHAR;
        } else if (ctx.TRUE() != null || ctx.FALSE() != null) {
            tipo = TipoDato.BOOL;
        } else if (ctx.expresion() != null) {
            tipo = tipos.get(ctx.expresion());
        } else if (ctx.llamada_expr() != null) {
            tipo = tipos.get(ctx.llamada_expr());
        }

        tipos.put(ctx, tipo);
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
            String nombre = ctx.ID().getText();
            Id simbolo = tabla.getSimbolo(nombre);
            if (simbolo == null) {
                System.out.println("[Escucha] exitFactorfunc ID='" + nombre + "' -> no declarado");
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
            String nombre = ctx.ID().getText();
            Id simbolo = tabla.getSimbolo(nombre);
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
            validarLlamada(ctx.llamada_expr().ID().getText(), ctx.llamada_expr().factorfunc(),
                    ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        } 
    }

    @Override
    public void exitLlamada_expr(Llamada_exprContext ctx) {
        super.exitLlamada_expr(ctx);
        if (ctx.ID() != null) {
            TipoDato retorno = validarLlamada(ctx.ID().getText(), ctx.factorfunc(),
                    ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
            if (retorno == TipoDato.VOID) {
                reportador.error("Error semantico: Funcion void no retorna valor utilizable en expresiones", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            }
            tipos.put(ctx, retorno);
        }
    }

    @Override
    public void exitIreturn(IreturnContext ctx) {
        super.exitIreturn(ctx);
        if (tipoFuncionActual == null) {
            reportador.error("Error semantico: return fuera de una funcion", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
            errors++;
            return;
        }

        TipoDato tipoExpr = ctx.expresion() != null ? tipos.get(ctx.expresion()) : null;
        if (tipoFuncionActual == TipoDato.VOID) {
            if (tipoExpr != null) {
                reportador.error("Error semantico: return con valor en funcion void", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                errors++;
            }
            return;
        }

        if (tipoExpr == null) {
            reportador.error("Error semantico: return sin valor en funcion no void", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
            errors++;
            return;
        }
        if (!puedeAsignar(tipoFuncionActual, tipoExpr)) {
            reportador.error("Error semantico: Tipo de retorno incompatible", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
            errors++;
        }
        retornoEncontrado = true;
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
