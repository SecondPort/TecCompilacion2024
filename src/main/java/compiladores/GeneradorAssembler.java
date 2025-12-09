package compiladores;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

import compiladores.compiladoresParser.*;

/**
 * Generador de código ensamblador para arquitectura x86 en sintaxis NASM.
 * <p>
 * Esta clase implementa el patrón <b>Visitor</b> de ANTLR4 para recorrer el árbol sintáctico
 * y generar código ensamblador equivalente al código fuente. Produce código compatible con
 * NASM que puede ser ensamblado y ejecutado en sistemas x86/x86-64.
 * </p>
 * <p>
 * <b>Responsabilidades principales:</b>
 * <ul>
 *   <li>Traducción de declaraciones de variables a directivas de memoria</li>
 *   <li>Generación de código para expresiones aritméticas usando pila</li>
 *   <li>Traducción de estructuras de control (if, while, for) a saltos condicionales</li>
 *   <li>Manejo de funciones (prólogo, epílogo, llamadas)</li>
 *   <li>Gestión de etiquetas únicas para flujo de control</li>
 * </ul>
 * <p>
 * <b>Estrategia de generación:</b>
 * <ul>
 *   <li>Variables almacenadas en la sección .bss (data no inicializada)</li>
 *   <li>Expresiones evaluadas usando registros EAX, EBX como acumuladores</li>
 *   <li>Uso de la pila para operaciones complejas</li>
 *   <li>Etiquetas únicas para cada estructura de control</li>
 * </ul>
 *
 * @author Compiladores 2024
 * @version 1.0
 * @since 1.0
 * @see compiladoresBaseVisitor
 * @see TablaSimbolos
 */
public class GeneradorAssembler extends compiladoresBaseVisitor<String> {

    /** Tipos simples manejados por el backend. */
    private enum SimpleType {
        INT,
        DOUBLE,
        CHAR;

        static SimpleType fromTipoLexema(String lex) {
            if (lex == null) {
                return INT;
            }
            lex = lex.trim();
            if ("double".equalsIgnoreCase(lex)) return DOUBLE;
            if ("char".equalsIgnoreCase(lex)) return CHAR;
            return INT;
        }
    }
    
    /**
     * StringBuilder que acumula el código ensamblador generado.
     * Permite construcción eficiente de strings grandes.
     */
    private StringBuilder codigo;
    
    /**
     * StringBuilder para la sección de datos (variables declaradas).
     */
    private StringBuilder seccionDatos;
    
    /**
     * Contador para generar etiquetas únicas en estructuras de control.
     * Se incrementa cada vez que se necesita una nueva etiqueta.
     */
    private int contadorEtiquetas;
    
    /**
     * Mapa que asocia nombres de variables con sus offsets en memoria.
     * Facilita el acceso a variables durante la generación de código.
     */
    private Map<String, Integer> offsetsVariables;

    /** Tipos declarados de variables (int/char/double) para seleccionar el camino de generación. */
    private Map<String, SimpleType> tiposVariables;
    /** Tipos de retorno de funciones conocidas. */
    private Map<String, SimpleType> tiposFunciones;
    /** Contenedor de constantes inicializadas (.data) para literales double. */
    private StringBuilder seccionDatosInit;
    /** Contador de etiquetas de constante double. */
    private int contadorConstantes = 0;
    
    /**
     * Offset actual en la pila para asignación de variables locales.
     * Se decrementa en múltiplos de 4 bytes (tamaño de int/double simplificado).
     */
    private int offsetActual;

    /** Nombre de la función actualmente en generación. */
    private String funcionActual = null;

    /** Etiqueta de salida de la función actual para soportar returns múltiples. */
    private String etiquetaFinFuncion = null;
    /** Cantidad de bytes de argumentos apilados en la llamada actual. */
    private int bytesArgumentosActual = 0;
    
    /**
     * Ruta del archivo de salida donde se escribirá el código ensamblador.
     */
    private String archivoSalida;
    
    /**
     * Conjunto de variables ya declaradas en la sección .bss para evitar duplicados.
     * Previene errores de ensamblador cuando variables con el mismo nombre se declaran múltiples veces.
     */
    private Set<String> variablesDeclaradas;

    private java.util.Deque<String> pilaBreak = new java.util.ArrayDeque<>();
    private java.util.Deque<String> pilaContinue = new java.util.ArrayDeque<>();

    /**
     * Constructor que inicializa el generador de código ensamblador.
     * <p>
     * Prepara todas las estructuras de datos necesarias para la generación
     * de código y establece el archivo de salida.
     * </p>
     *
     * @param archivoSalida ruta del archivo donde se guardará el código ensamblador generado
     */
    public GeneradorAssembler(String archivoSalida) {
        this.codigo = new StringBuilder();
        this.seccionDatos = new StringBuilder();
        this.seccionDatosInit = new StringBuilder();
        this.contadorEtiquetas = 0;
        this.offsetsVariables = new HashMap<>();
        this.tiposVariables = new HashMap<>();
        this.tiposFunciones = new HashMap<>();
        this.offsetActual = 0;
        this.archivoSalida = archivoSalida;
        this.variablesDeclaradas = new HashSet<>();
        
        inicializarCodigo();
    }

    /**
     * Inicializa el código ensamblador con las directivas y secciones necesarias.
     * <p>
     * Genera el encabezado del archivo NASM incluyendo:
     * <ul>
     *   <li>Formato de 32 bits</li>
     *   <li>Declaración de función _start como punto de entrada</li>
     *   <li>Sección de texto (.text) para código ejecutable</li>
     * </ul>
     */
    private void inicializarCodigo() {
        codigo.append("; Código ensamblador generado automáticamente\n");
        codigo.append("; Compilador: TecCompilacion2024\n");
        codigo.append("; Sintaxis: NASM (x86)\n\n");
        codigo.append("bits 32\n");
        codigo.append("global _start\n\n");
        
        seccionDatosInit.append("section .data\n");
        seccionDatosInit.append("    ; Constantes inicializadas\n");
        seccionDatos.append("section .bss\n");
        seccionDatos.append("    ; Variables globales\n");
    }

    /**
     * Genera una nueva etiqueta única para estructuras de control.
     * <p>
     * Las etiquetas siguen el formato "L{número}" donde el número se incrementa
     * secuencialmente para garantizar unicidad.
     * </p>
     *
     * @return una cadena con la etiqueta única generada
     */
    private String generarEtiqueta() {
        return "L" + (contadorEtiquetas++);
    }

    /** Devuelve etiqueta de constante double en .data. */
    private String constanteDouble(String valorLexema) {
        String nombre = "_cd" + (contadorConstantes++);
        seccionDatosInit.append("    ").append(nombre).append(" dq ").append(valorLexema).append("\n");
        return nombre;
    }

    /** Registra tipos de parámetros de una función en el mapa de variables. */
    private void registrarParametrosTipos(IdfuncContext idfunc) {
        if (idfunc == null) return;
        if (idfunc.tipo() != null && idfunc.ID() != null) {
            tiposVariables.put(idfunc.ID().getText(), SimpleType.fromTipoLexema(idfunc.tipo().getText()));
        }
        ListaidfuncContext lista = idfunc.listaidfunc();
        while (lista != null && lista.getChildCount() > 0) {
            if (lista.tipo() != null && lista.ID() != null) {
                tiposVariables.put(lista.ID().getText(), SimpleType.fromTipoLexema(lista.tipo().getText()));
            }
            lista = lista.listaidfunc();
        }
    }

    /**
     * Materializa el resultado actual en ST0 o EAX dentro de una variable double destino.
     */
    private void materializarDoubleDesdeTipo(SimpleType tipoExpr, String destino) {
        if (tipoExpr == SimpleType.DOUBLE) {
            codigo.append("    fstp qword [").append(destino).append("]\n");
        } else {
            codigo.append("    push eax\n");
            codigo.append("    fild dword [esp]\n");
            codigo.append("    add esp, 4\n");
            codigo.append("    fstp qword [").append(destino).append("]\n");
        }
    }

    /**
     * Visita el nodo raíz del programa e inicia la generación de código.
     * <p>
     * Este método establece la estructura principal del programa ensamblador,
     * incluyendo el punto de entrada _start y la finalización del programa.
     * </p>
     *
     * @param ctx el contexto del nodo {@code programa} del árbol sintáctico
     * @return cadena vacía (el resultado se acumula en el StringBuilder interno)
     */
    @Override
    public String visitPrograma(ProgramaContext ctx) {
        codigo.append("section .text\n");
        codigo.append("_start:\n");
        codigo.append("    ; Inicio del programa\n");
        
        // Visitar todas las instrucciones del programa
        super.visitPrograma(ctx);
        
        // Finalizar el programa con syscall exit
        codigo.append("\n    ; Fin del programa\n");
        codigo.append("    mov eax, 1        ; syscall: exit\n");
        codigo.append("    xor ebx, ebx      ; código de salida: 0\n");
        codigo.append("    int 0x80          ; llamada al sistema\n");
        
        // Guardar el código generado en el archivo
        guardarArchivo();
        
        return "";
    }

    /**
     * Procesa la declaración completa de una función y genera su etiqueta y cuerpo.
     * <p>
     * Para cada función definida en el código fuente se genera una etiqueta con su
     * nombre, se traduce el bloque asociado y, si no hay un {@code return} explícito
     * que termine la función, se emite un retorno por defecto que coloca 0 en
     * {@code eax} y ejecuta {@code ret}.
     * </p>
     * <p>
     * <b>Nota:</b> el manejo de prólogo/epílogo y variables locales es simplificado;
     * no se utiliza {@code ebp} ni un frame de pila completo.
     * </p>
     *
     * @param ctx el contexto del nodo {@code declaracionfunc} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitDeclaracionfunc(DeclaracionfuncContext ctx) {
        String nombre = ctx.ID().getText();
        SimpleType tipoRet = SimpleType.fromTipoLexema(ctx.tipofunc().getText());
        tiposFunciones.put(nombre, tipoRet);
        registrarParametrosTipos(ctx.idfunc());
        funcionActual = nombre;
        etiquetaFinFuncion = nombre + "_end";
        codigo.append("\n").append(nombre).append(":\n");
        codigo.append("    push ebp\n");
        codigo.append("    mov ebp, esp\n");
        codigo.append("    ; cuerpo de función\n");
        visitBloque(ctx.bloque());
        // Si no hubo return explícito, devolver 0
        codigo.append(etiquetaFinFuncion).append(":\n");
        codigo.append("    mov esp, ebp\n");
        codigo.append("    pop ebp\n");
        codigo.append("    mov eax, 0\n");
        codigo.append("    ret\n");
        etiquetaFinFuncion = null;
        funcionActual = null;
        return "";
    }

    /**
     * Procesa una declaración de variable y genera código ensamblador.
     * <p>
     * Reserva espacio en la sección .bss para la variable y, si está inicializada,
     * genera código para asignarle su valor inicial.
     * </p>
     * <p>
     * <b>Generación para declaración:</b>
     * <pre>
     * int x = 5;  →  x: resd 1          ; en sección .bss
     *                 mov dword [x], 5   ; en sección .text
     * </pre>
     * </p>
     *
     * @param ctx el contexto del nodo {@code declaracion} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitDeclaracion(DeclaracionContext ctx) {
        String nombre = ctx.ID().getText();
        String tipoLexema = ctx.tipo().getText();
        SimpleType tipo = SimpleType.fromTipoLexema(tipoLexema);
        
        // Reservar espacio en la sección de datos solo si no ha sido declarada antes
        if (!variablesDeclaradas.contains(nombre)) {
            if (tipo == SimpleType.CHAR) {
                seccionDatos.append("    ").append(nombre).append(": resb 1  ; char\n");
            } else if (tipo == SimpleType.DOUBLE) {
                seccionDatos.append("    ").append(nombre).append(": resq 1  ; double\n");
            } else {
                seccionDatos.append("    ").append(nombre).append(": resd 1  ; ")
                           .append(tipoLexema).append("\n");
            }
            offsetsVariables.put(nombre, offsetActual);
            offsetActual += (tipo == SimpleType.DOUBLE ? 8 : 4);
            variablesDeclaradas.add(nombre);
            tiposVariables.put(nombre, tipo);
        }
        
        // Si tiene inicialización, generar código de asignación evaluando la expresion
        if (ctx.inicializacion() != null && ctx.inicializacion().expresion() != null) {
            codigo.append("    ; Inicialización de ").append(nombre).append("\n");
            SimpleType tipoExpr = visitExpresionConTipo(ctx.inicializacion().expresion());
            if (tipo == SimpleType.DOUBLE) {
                materializarDoubleDesdeTipo(tipoExpr, nombre);
            } else if (tipo == SimpleType.CHAR) {
                if (tipoExpr == SimpleType.DOUBLE) {
                    codigo.append("    sub esp, 4\n");
                    codigo.append("    fistp dword [esp]\n");
                    codigo.append("    pop eax\n");
                }
                codigo.append("    mov byte [").append(nombre).append("], al\n");
            } else {
                if (tipoExpr == SimpleType.DOUBLE) {
                    codigo.append("    fistp dword [").append(nombre).append("]\n");
                } else {
                    codigo.append("    mov dword [").append(nombre).append("], eax\n");
                }
            }
        }
        
        return super.visitDeclaracion(ctx);
    }

    /**
     * Procesa una asignación y genera el código correspondiente.
     * <p>
     * Evalúa la expresión del lado derecho y almacena el resultado en la variable
     * del lado izquierdo.
     * </p>
     * <p>
     * <b>Generación para asignación:</b>
     * <pre>
     * x = 10;  →  mov eax, 10
     *             mov [x], eax
     * </pre>
     * </p>
     *
     * @param ctx el contexto del nodo {@code asignacion} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitAsignacion(AsignacionContext ctx) {
        String variable = ctx.ID().getText();
        SimpleType tipoDestino = tiposVariables.getOrDefault(variable, SimpleType.INT);
        
        codigo.append("\n    ; Asignación a ").append(variable).append("\n");
        
        // Evaluar la expresión del lado derecho
        SimpleType tipoExpr = visitExpresionConTipo(ctx.expresion());

        if (tipoDestino == SimpleType.DOUBLE) {
            materializarDoubleDesdeTipo(tipoExpr, variable);
        } else {
            if (tipoExpr == SimpleType.DOUBLE) {
                codigo.append("    sub esp, 4\n");
                codigo.append("    fistp dword [esp]\n");
                codigo.append("    pop eax\n");
            }
            if (tipoDestino == SimpleType.CHAR) {
                codigo.append("    mov byte [").append(variable).append("], al\n");
            } else {
                codigo.append("    mov [").append(variable).append("], eax\n");
            }
        }
        
        return "";
    }

    /**
     * Procesa una expresión aritmética y genera código para evaluarla.
     * <p>
     * Las expresiones se evalúan usando el registro EAX como acumulador principal
     * y la pila para operandos temporales.
     * </p>
     *
     * @param ctx el contexto del nodo {@code expresion} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitExpresion(ExpresionContext ctx) {
        visitExpresionConTipo(ctx);
        return "";
    }

    /** Evalúa expresión y devuelve su tipo, dejando el resultado en eax (int/char) o ST0 (double). */
    private SimpleType visitExpresionConTipo(ExpresionContext ctx) {
        if (ctx == null) {
            return SimpleType.INT;
        }

        // unario -expr
        if (ctx.RESTA() != null && ctx.expresion().size() == 1) {
            SimpleType t = visitExpresionConTipo(ctx.expresion(0));
            if (t == SimpleType.DOUBLE) {
                codigo.append("    fchs\n");
            } else {
                codigo.append("    neg eax\n");
            }
            return t;
        }

        // !expr
        if (ctx.getChildCount() == 2 && ctx.getChild(0).getText().equals("!")) {
            SimpleType t = visitExpresionConTipo(ctx.expresion(0));
            if (t == SimpleType.DOUBLE) {
                codigo.append("    fldz\n");
                codigo.append("    fcomip st0, st1\n");
                codigo.append("    fstp st0\n");
                codigo.append("    mov eax, 0\n");
                codigo.append("    sete al\n");
            } else {
                codigo.append("    cmp eax, 0\n");
                codigo.append("    mov eax, 0\n");
                codigo.append("    sete al\n");
            }
            return SimpleType.INT;
        }

        // binario expr op expr
        if (ctx.expresion().size() == 2) {
            SimpleType tIzq = visitExpresionConTipo(ctx.expresion(0));
            // Materializar izq si es double en la pila
            if (tIzq == SimpleType.DOUBLE) {
                codigo.append("    sub esp, 8\n");
                codigo.append("    fstp qword [esp]\n");
            } else {
                // Guardar int/char en la pila para recuperarlo después
                codigo.append("    push eax\n");
            }

            SimpleType tDer = visitExpresionConTipo(ctx.expresion(1));

            boolean usaDouble = (tIzq == SimpleType.DOUBLE) || (tDer == SimpleType.DOUBLE);

            if (usaDouble) {
                // Cargar izquierda en ST0, derecha en ST1
                if (tDer != SimpleType.DOUBLE) {
                    // convertir der int->double en ST0
                    codigo.append("    push eax\n");
                    codigo.append("    fild dword [esp]\n");
                    codigo.append("    add esp, 4\n");
                }
                if (tIzq == SimpleType.DOUBLE) {
                    codigo.append("    fld qword [esp]\n");
                    codigo.append("    add esp, 8\n");
                } else {
                    codigo.append("    pop eax\n");
                    codigo.append("    push eax\n");
                    codigo.append("    fild dword [esp]\n");
                    codigo.append("    add esp, 4\n");
                }

                // Ahora ST0 = izq, ST1 = der si izq fue cargado al final; ajustemos para no conmutativos
                if (ctx.SUMA() != null) {
                    codigo.append("    faddp st1, st0\n");
                } else if (ctx.RESTA() != null) {
                    codigo.append("    fxch st1\n");
                    codigo.append("    fsubp st1, st0\n");
                } else if (ctx.MULT() != null) {
                    codigo.append("    fmulp st1, st0\n");
                } else if (ctx.DIV() != null) {
                    codigo.append("    fxch st1\n");
                    codigo.append("    fdivp st1, st0\n");
                } else if (ctx.MOD() != null) {
                    codigo.append("    ; MOD no soportado para double, resultado 0\n");
                    codigo.append("    fstp st0\n");
                    codigo.append("    fstp st0\n");
                    codigo.append("    fldz\n");
                } else if (ctx.EQ() != null || ctx.UEQ() != null || ctx.MAYOR() != null || ctx.MENOR() != null
                        || ctx.MAYORIGUAL() != null || ctx.MENORIGUAL() != null) {
                    codigo.append("    fcomip st0, st1\n");
                    codigo.append("    fstp st0\n");
                    codigo.append("    mov eax, 0\n");
                    if (ctx.EQ() != null) {
                        codigo.append("    sete al\n");
                    } else if (ctx.UEQ() != null) {
                        codigo.append("    setne al\n");
                    } else if (ctx.MAYOR() != null) {
                        codigo.append("    seta al\n");
                    } else if (ctx.MENOR() != null) {
                        codigo.append("    setb al\n");
                    } else if (ctx.MAYORIGUAL() != null) {
                        codigo.append("    setae al\n");
                    } else if (ctx.MENORIGUAL() != null) {
                        codigo.append("    setbe al\n");
                    }
                    return SimpleType.INT;
                }
                return SimpleType.DOUBLE;
            }

            // Camino INT/CHAR
            codigo.append("    mov ebx, eax\n");
            codigo.append("    pop eax\n");

            if (ctx.SUMA() != null) {
                codigo.append("    add eax, ebx\n");
            } else if (ctx.RESTA() != null) {
                codigo.append("    sub eax, ebx\n");
            } else if (ctx.MULT() != null) {
                codigo.append("    imul eax, ebx\n");
            } else if (ctx.DIV() != null) {
                codigo.append("    cdq\n");
                codigo.append("    idiv ebx\n");
            } else if (ctx.MOD() != null) {
                codigo.append("    cdq\n");
                codigo.append("    idiv ebx\n");
                codigo.append("    mov eax, edx\n");
            } else if (ctx.EQ() != null || ctx.UEQ() != null || ctx.MAYOR() != null || ctx.MENOR() != null
                    || ctx.MAYORIGUAL() != null || ctx.MENORIGUAL() != null) {
                codigo.append("    cmp eax, ebx\n");
                codigo.append("    mov eax, 0\n");
                if (ctx.EQ() != null) {
                    codigo.append("    sete al\n");
                } else if (ctx.UEQ() != null) {
                    codigo.append("    setne al\n");
                } else if (ctx.MAYOR() != null) {
                    codigo.append("    setg al\n");
                } else if (ctx.MENOR() != null) {
                    codigo.append("    setl al\n");
                } else if (ctx.MAYORIGUAL() != null) {
                    codigo.append("    setge al\n");
                } else if (ctx.MENORIGUAL() != null) {
                    codigo.append("    setle al\n");
                }
                return SimpleType.INT;
            } else if (ctx.AND() != null) {
                codigo.append("    cmp eax, 0\n");
                codigo.append("    setne al\n");
                codigo.append("    movzx eax, al\n");
                codigo.append("    cmp ebx, 0\n");
                codigo.append("    setne bl\n");
                codigo.append("    movzx ebx, bl\n");
                codigo.append("    and eax, ebx\n");
                return SimpleType.INT;
            } else if (ctx.OR() != null) {
                codigo.append("    cmp eax, 0\n");
                codigo.append("    setne al\n");
                codigo.append("    movzx eax, al\n");
                codigo.append("    cmp ebx, 0\n");
                codigo.append("    setne bl\n");
                codigo.append("    movzx ebx, bl\n");
                codigo.append("    or eax, ebx\n");
                return SimpleType.INT;
            }

            return SimpleType.INT;
        }

        // Caso base: factor
        if (ctx.factor() != null) {
            return visitFactorConTipo(ctx.factor());
        }

        return SimpleType.INT;
    }

    /**
     * Procesa un factor (elemento básico de una expresión).
     * <p>
     * Los factores pueden ser:
     * <ul>
     *   <li>Números literales</li>
     *   <li>Identificadores (variables)</li>
     *   <li>Expresiones entre paréntesis</li>
     * </ul>
     * </p>
     *
     * @param ctx el contexto del nodo {@code factor} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitFactor(FactorContext ctx) {
        visitFactorConTipo(ctx);
        return "";
    }

    private SimpleType visitFactorConTipo(FactorContext ctx) {
        if (ctx.NUMERO() != null) {
            String numero = ctx.NUMERO().getText();
            if (numero.contains(".")) {
                String constLabel = constanteDouble(numero);
                codigo.append("    fld qword [").append(constLabel).append("]\n");
                return SimpleType.DOUBLE;
            }
            codigo.append("    mov eax, ").append(numero).append("\n");
            return SimpleType.INT;
        } else if (ctx.CHAR_CONST() != null) {
            String lexema = ctx.CHAR_CONST().getText();
            char c = lexema.charAt(1);
            int ascii = (int) c;
            codigo.append("    mov eax, ").append(ascii).append("\n");
            return SimpleType.CHAR;
        } else if (ctx.ID() != null) {
            String variable = ctx.ID().getText();
            SimpleType t = tiposVariables.getOrDefault(variable, SimpleType.INT);
            if (t == SimpleType.DOUBLE) {
                codigo.append("    fld qword [").append(variable).append("]\n");
            } else if (t == SimpleType.CHAR) {
                codigo.append("    movzx eax, byte [").append(variable).append("]\n");
            } else {
                codigo.append("    mov eax, [").append(variable).append("]\n");
            }
            return t;
        } else if (ctx.llamada_expr() != null) {
            SimpleType t = visitLlamadaExprConTipo(ctx.llamada_expr());
            return t;
        } else if (ctx.expresion() != null) {
            return visitExpresionConTipo(ctx.expresion());
        }
        return SimpleType.INT;
    }

    /**
     * Extrae los argumentos de factorfunc como lista de strings.
     */
    private List<String> extraerArgumentos(FactorfuncContext ctx) {
        List<String> args = new ArrayList<>();
        if (ctx == null) {
            return args;
        }
        if (ctx.NUMERO() != null) {
            args.add(ctx.NUMERO().getText());
        }
        if (ctx.ID() != null) {
            args.add(ctx.ID().getText());
        }
        if (ctx.expresion() != null) {
            // Evaluamos la expresión en EAX y la materializamos en un temporal anónimo en la pila
            visitExpresion(ctx.expresion());
            codigo.append("    push eax\n");
            args.add("[esp]");
        }
        if (ctx.listafactfunc() != null) {
            args.addAll(extraerArgumentos(ctx.listafactfunc()));
        }
        return args;
    }

    private List<String> extraerArgumentos(ListafactfuncContext ctx) {
        List<String> args = new ArrayList<>();
        if (ctx == null) {
            return args;
        }
        if (ctx.NUMERO() != null) {
            args.add(ctx.NUMERO().getText());
        }
        if (ctx.ID() != null) {
            args.add(ctx.ID().getText());
        }
        if (ctx.expresion() != null) {
            visitExpresion(ctx.expresion());
            codigo.append("    push eax\n");
            args.add("[esp]");
        }
        if (ctx.listafactfunc() != null) {
            args.addAll(extraerArgumentos(ctx.listafactfunc()));
        }
        return args;
    }

    /**
     * Procesa una llamada a función y genera el código correspondiente.
     * <p>
     * Evalúa opcionalmente un argumento (modelo simplificado de un solo
     * parámetro), lo pasa por la pila con {@code push eax}, invoca la
     * función mediante {@code call nombre} y, tras el retorno, limpia la
     * pila con {@code add esp, 4}. El valor de retorno se asume en
     * {@code eax} según la convención utilizada.
     * </p>
     *
     * @param ctx el contexto del nodo {@code llamadafunc} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitLlamadafunc(compiladoresParser.LlamadafuncContext ctx) {
        // llamadafunc ahora es llamada_expr PYC
        visit(ctx.llamada_expr());
        return "";
    }

    @Override
    public String visitLlamada_expr(compiladoresParser.Llamada_exprContext ctx) {
        visitLlamadaExprConTipo(ctx);
        return "";
    }

    private java.util.List<Object> gatherArgs(FactorfuncContext ctx) {
        java.util.List<Object> list = new java.util.ArrayList<>();
        if (ctx == null) return list;
        list.add(ctx);
        ListafactfuncContext tail = ctx.listafactfunc();
        while (tail != null && tail.getChildCount() > 0) {
            list.add(tail);
            tail = tail.listafactfunc();
        }
        return list;
    }

    private SimpleType evaluarArgumento(Object nodo) {
        org.antlr.v4.runtime.tree.TerminalNode num = null;
        org.antlr.v4.runtime.tree.TerminalNode id = null;
        ExpresionContext expr = null;
        if (nodo instanceof FactorfuncContext) {
            FactorfuncContext ctx = (FactorfuncContext) nodo;
            num = ctx.NUMERO();
            id = ctx.ID();
            expr = ctx.expresion();
        } else if (nodo instanceof ListafactfuncContext) {
            ListafactfuncContext ctx = (ListafactfuncContext) nodo;
            num = ctx.NUMERO();
            id = ctx.ID();
            expr = ctx.expresion();
        }

        if (num != null) {
            String numero = num.getText();
            if (numero.contains(".")) {
                String constLabel = constanteDouble(numero);
                codigo.append("    fld qword [").append(constLabel).append("]\n");
                return SimpleType.DOUBLE;
            }
            codigo.append("    mov eax, ").append(numero).append("\n");
            return SimpleType.INT;
        }
        if (id != null) {
            String nombre = id.getText();
            SimpleType t = tiposVariables.getOrDefault(nombre, SimpleType.INT);
            if (t == SimpleType.DOUBLE) {
                codigo.append("    fld qword [").append(nombre).append("]\n");
            } else if (t == SimpleType.CHAR) {
                codigo.append("    movzx eax, byte [").append(nombre).append("]\n");
            } else {
                codigo.append("    mov eax, [").append(nombre).append("]\n");
            }
            return t;
        }
        if (expr != null) {
            return visitExpresionConTipo(expr);
        }
        return SimpleType.INT;
    }

    private SimpleType visitLlamadaExprConTipo(compiladoresParser.Llamada_exprContext ctx) {
        String nombre = ctx.ID().getText();
        java.util.List<Object> args = gatherArgs(ctx.factorfunc());
        bytesArgumentosActual = 0;
        // Push args de derecha a izquierda
        for (int i = args.size() - 1; i >= 0; i--) {
            SimpleType tArg = evaluarArgumento(args.get(i));
            if (tArg == SimpleType.DOUBLE) {
                codigo.append("    sub esp, 8\n");
                codigo.append("    fstp qword [esp]\n");
                bytesArgumentosActual += 8;
            } else {
                codigo.append("    push eax\n");
                bytesArgumentosActual += 4;
            }
        }

        codigo.append("    call ").append(nombre).append("\n");
        if (bytesArgumentosActual > 0) {
            codigo.append("    add esp, ").append(bytesArgumentosActual).append("\n");
        }
        bytesArgumentosActual = 0;

        SimpleType ret = tiposFunciones.getOrDefault(nombre, SimpleType.INT);
        return ret;
    }

    @Override
    public String visitPrototipofunc(PrototipofuncContext ctx) {
        String nombre = ctx.ID().getText();
        tiposFunciones.put(nombre, SimpleType.fromTipoLexema(ctx.tipofunc().getText()));
        registrarParametrosTipos(ctx.idfunc());
        return super.visitPrototipofunc(ctx);
    }

    @Override
    public String visitIreturn(IreturnContext ctx) {
        SimpleType tipoRet = tiposFunciones.getOrDefault(funcionActual, SimpleType.INT);
        SimpleType tipoExpr = SimpleType.INT;
        if (ctx.expresion() != null) {
            tipoExpr = visitExpresionConTipo(ctx.expresion());
        } else {
            if (tipoRet == SimpleType.DOUBLE) {
                codigo.append("    fldz\n");
            } else {
                codigo.append("    mov eax, 0\n");
            }
        }
        if (tipoRet == SimpleType.DOUBLE && tipoExpr != SimpleType.DOUBLE) {
            codigo.append("    push eax\n");
            codigo.append("    fild dword [esp]\n");
            codigo.append("    add esp, 4\n");
        }
        if (etiquetaFinFuncion != null) {
            codigo.append("    jmp ").append(etiquetaFinFuncion).append("\n");
        } else {
            codigo.append("    ret\n");
        }
        return "";
    }

    /**
     * Procesa una estructura if-else y genera saltos condicionales.
     * <p>
     * Genera etiquetas para el bloque else y el final del if, implementando
     * saltos basados en la evaluación de la condición.
     * </p>
     * <p>
     * <b>Estructura generada:</b>
     * <pre>
     * if (x > 5) { ... } else { ... }
     * →
     *     cmp [x], 5
     *     jle else_label
     *     ; bloque if
     *     jmp end_if
     * else_label:
     *     ; bloque else
     * end_if:
     * </pre>
     * </p>
     *
     * @param ctx el contexto del nodo {@code iif} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitIif(IifContext ctx) {
        String etiquetaElse = generarEtiqueta();
        String etiquetaFin = generarEtiqueta();
        
        codigo.append("\n    ; Estructura IF\n");
        
        // Evaluar condición
        visitCondicion(ctx.condicion());
        
        // Saltar a else si la condición es falsa
        codigo.append("    je ").append(etiquetaElse).append("  ; saltar si falso\n");
        
        // Bloque del if
        visitBloque(ctx.bloque());
        codigo.append("    jmp ").append(etiquetaFin).append("  ; saltar al final\n");
        
        // Etiqueta del else
        codigo.append(etiquetaElse).append(":\n");
        
        // Visitar el bloque else si existe
        if (ctx.ielse() != null) {
            visitIelse(ctx.ielse());
        }
        
        codigo.append(etiquetaFin).append(":\n");
        
        return "";
    }

    /**
     * Procesa una estructura while y genera código de bucle.
     * <p>
     * Implementa el bucle usando etiquetas para el inicio y fin,
     * con salto condicional basado en la evaluación de la condición.
     * </p>
     * <p>
     * <b>Estructura generada:</b>
     * <pre>
     * while (x < 10) { ... }
     * →
     * loop_start:
     *     cmp [x], 10
     *     jge loop_end
     *     ; cuerpo del bucle
     *     jmp loop_start
     * loop_end:
     * </pre>
     * </p>
     *
     * @param ctx el contexto del nodo {@code iwhile} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitIwhile(IwhileContext ctx) {
        String etiquetaInicio = generarEtiqueta();
        String etiquetaFin = generarEtiqueta();

        pilaContinue.push(etiquetaInicio);
        pilaBreak.push(etiquetaFin);

        codigo.append("\n    ; Estructura WHILE\n");
        codigo.append(etiquetaInicio).append(":\n");

        // Evaluar condición
        visitCondicion(ctx.condicion());

        // Saltar al final si la condición es falsa
        codigo.append("    je ").append(etiquetaFin).append("  ; salir si falso\n");

        // Cuerpo del while
        visitBloque(ctx.bloque());

        // Volver al inicio del bucle
        codigo.append("    jmp ").append(etiquetaInicio).append("  ; repetir bucle\n");
        codigo.append(etiquetaFin).append(":\n");

        pilaBreak.pop();
        pilaContinue.pop();

        return "";
    }

    /**
     * Procesa una estructura for y genera código de bucle con inicialización.
     * <p>
     * Traduce el bucle for a una estructura equivalente con inicialización,
     * condición de continuación y expresión de actualización.
     * </p>
     * <p>
     * <b>Estructura generada:</b>
     * <pre>
     * for (int i = 0; i < 5; i++) { ... }
     * →
     *     mov dword [i], 0      ; inicialización
     * loop_start:
     *     cmp [i], 5            ; condición
     *     jge loop_end          ; salir si falso
     *     ; cuerpo del bucle
     *     inc dword [i]         ; actualización
     *     jmp loop_start
     * loop_end:
     * </pre>
     * </p>
     *
     * @param ctx el contexto del nodo {@code ifor} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitIfor(IforContext ctx) {
        String etiquetaInicio = generarEtiqueta();
        String etiquetaFin = generarEtiqueta();

        pilaContinue.push(etiquetaInicio);
        pilaBreak.push(etiquetaFin);

        codigo.append("\n    ; Estructura FOR\n");
        
        // Obtener el contexto del ciclo: PA declaracion comparacion PYC finfor PC
        CicloContext ciclo = ctx.ciclo();
        
        // 1. Procesar inicialización (declaracion)
        if (ciclo.declaracion() != null) {
            visitDeclaracion(ciclo.declaracion());
        }
        
        // 2. Etiqueta de inicio del bucle (ANTES de la condición)
        codigo.append(etiquetaInicio).append(":\n");
        
        // 3. Evaluar condición general (la parte central es una expresion)
        if (ciclo.expresion() != null) {
            visitExpresion(ciclo.expresion());
        }
        
        // 4. Saltar al final si la condición es falsa
        codigo.append("    je ").append(etiquetaFin).append("  ; salir si falso\n");
        
        // 5. Cuerpo del for
        visitBloque(ctx.bloque());
        
        // 6. Procesar actualización (finfor: i++, i--, o expresion)
        if (ciclo.finfor() != null) {
            visitFinfor(ciclo.finfor());
        }
        
        // 7. Volver al inicio del bucle
        codigo.append("    jmp ").append(etiquetaInicio).append("  ; repetir bucle\n");
        codigo.append(etiquetaFin).append(":\n");

        pilaBreak.pop();
        pilaContinue.pop();

        return "";
    }

    @Override
    public String visitIbreak(IbreakContext ctx) {
        if (!pilaBreak.isEmpty()) {
            String destino = pilaBreak.peek();
            codigo.append("    ; break\n");
            codigo.append("    jmp ").append(destino).append("\n");
        }
        return "";
    }

    @Override
    public String visitIcontinue(IcontinueContext ctx) {
        if (!pilaContinue.isEmpty()) {
            String destino = pilaContinue.peek();
            codigo.append("    ; continue\n");
            codigo.append("    jmp ").append(destino).append("\n");
        }
        return "";
    }
    
    /**
     * Procesa la actualización del bucle for (incremento, decremento o expresión).
     * <p>
     * Maneja las tres formas posibles de actualización en un for:
     * <ul>
     *   <li>ID++ (incremento)</li>
     *   <li>ID-- (decremento)</li>
     *   <li>expresion (asignación general)</li>
     * </ul>
     * </p>
     *
     * @param ctx el contexto del nodo {@code finfor} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitFinfor(FinforContext ctx) {
        if (ctx.INCREMENTO() != null && ctx.ID() != null) {
            // Caso: i++
            String variable = ctx.ID().getText();
            codigo.append("    ; Incremento de ").append(variable).append("\n");
            codigo.append("    inc dword [").append(variable).append("]\n");
        } else if (ctx.DECREMENTO() != null && ctx.ID() != null) {
            // Caso: i--
            String variable = ctx.ID().getText();
            codigo.append("    ; Decremento de ").append(variable).append("\n");
            codigo.append("    dec dword [").append(variable).append("]\n");
        } else if (ctx.expresion() != null) {
            // Caso: expresión general (ej: i = i + 1)
            codigo.append("    ; Actualización por expresión\n");
            visitExpresion(ctx.expresion());
        }
        return "";
    }
    
    /**
     * Procesa el ciclo del for sin generar código adicional.
     * <p>
     * Este método existe para evitar que el visitor por defecto procese
     * el ciclo de manera incorrecta. El procesamiento real se hace en visitIfor.
     * </p>
     *
     * @param ctx el contexto del nodo {@code ciclo} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitCiclo(CicloContext ctx) {
        // No hacer nada aquí - el procesamiento se hace en visitIfor
        return "";
    }

    /**
     * Procesa la condición de una estructura de control.
     * <p>
     * Evalúa la comparación y establece flags para saltos condicionales.
     * El resultado se refleja en los flags del procesador para uso de
     * instrucciones de salto condicional (je, jne, jg, jl, etc.).
     * </p>
     *
     * @param ctx el contexto del nodo {@code condicion} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitCondicion(CondicionContext ctx) {
        if (ctx != null && ctx.expresion() != null) {
            visitExpresion(ctx.expresion());
        }
        return "";
    }

    /**
     * Procesa un bloque de código delimitado por llaves.
     * <p>
     * Los bloques crean un nuevo ámbito para variables locales en el
     * análisis semántico, pero en la generación de código simplemente
     * se procesan las instrucciones contenidas.
     * </p>
     *
     * @param ctx el contexto del nodo {@code bloque} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitBloque(BloqueContext ctx) {
        codigo.append("    ; Inicio de bloque\n");
        super.visitBloque(ctx);
        codigo.append("    ; Fin de bloque\n");
        return "";
    }

    /**
     * Guarda el código ensamblador generado en el archivo especificado.
     * <p>
     * Combina la sección de datos (.bss) con la sección de código (.text)
     * y escribe el resultado completo en el archivo de salida.
     * </p>
     * <p>
     * <b>Gestión de errores:</b> Imprime un mensaje de error si no se puede
     * escribir el archivo y retorna sin lanzar excepción.
     * </p>
     */
    private void guardarArchivo() {
        try (FileWriter writer = new FileWriter(archivoSalida)) {
            // Sección .data (constantes inicializadas)
            writer.write(seccionDatosInit.toString());
            writer.write("\n");

            // Sección .bss (variables sin inicializar)
            writer.write(seccionDatos.toString());
            writer.write("\n");
            
            // Luego la sección de código
            writer.write(codigo.toString());
            
            System.out.println("\n===========================================");
            System.out.println("Código ensamblador generado exitosamente:");
            System.out.println("Archivo: " + archivoSalida);
            System.out.println("===========================================\n");
            
        } catch (IOException e) {
            System.err.println("Error al guardar el archivo de ensamblador: " + e.getMessage());
        }
    }

    /**
     * Obtiene el código ensamblador generado como cadena.
     * <p>
     * Útil para testing o cuando se necesita el código en memoria
     * sin escribirlo a un archivo.
     * </p>
     *
     * @return el código ensamblador completo (sección de datos + código)
     */
    public String obtenerCodigo() {
        return seccionDatos.toString() + "\n" + codigo.toString();
    }
}

