package compiladores;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    
    /**
     * Offset actual en la pila para asignación de variables locales.
     * Se decrementa en múltiplos de 4 bytes (tamaño de int/double simplificado).
     */
    private int offsetActual;
    
    /**
     * Ruta del archivo de salida donde se escribirá el código ensamblador.
     */
    private String archivoSalida;

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
        this.contadorEtiquetas = 0;
        this.offsetsVariables = new HashMap<>();
        this.offsetActual = 0;
        this.archivoSalida = archivoSalida;
        
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
        String tipo = ctx.tipo().getText();
        
        // Reservar espacio en la sección de datos
        if (tipo.equals("int") || tipo.equals("double")) {
            seccionDatos.append("    ").append(nombre).append(": resd 1  ; ")
                       .append(tipo).append("\n");
            offsetsVariables.put(nombre, offsetActual);
            offsetActual += 4; // 4 bytes por variable
        }
        
        // Si tiene inicialización, generar código de asignación
        if (ctx.inicializacion() != null && ctx.inicializacion().NUMERO() != null) {
            String valor = ctx.inicializacion().NUMERO().getText();
            codigo.append("    ; Inicialización de ").append(nombre).append("\n");
            codigo.append("    mov dword [").append(nombre).append("], ").append(valor).append("\n");
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
        
        codigo.append("\n    ; Asignación a ").append(variable).append("\n");
        
        // Evaluar la expresión del lado derecho
        visitExpresion(ctx.expresion());
        
        // El resultado está en EAX, moverlo a la variable
        codigo.append("    mov [").append(variable).append("], eax\n");
        
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
        // Evaluar el término principal
        visitTermino(ctx.termino());
        
        // Evaluar el resto de la expresión (suma/resta)
        visitExp(ctx.exp());
        
        return "";
    }

    /**
     * Procesa un término (multiplicación/división) en una expresión.
     * <p>
     * Los términos tienen mayor precedencia que las sumas y restas.
     * </p>
     *
     * @param ctx el contexto del nodo {@code termino} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitTermino(TerminoContext ctx) {
        // Evaluar el factor principal
        visitFactor(ctx.factor());
        
        // Evaluar operaciones de multiplicación/división
        visitTerm(ctx.term());
        
        return "";
    }

    /**
     * Procesa operaciones de suma y resta en una expresión.
     * <p>
     * Genera código para operaciones binarias usando la pila para preservar
     * el operando izquierdo mientras se evalúa el derecho.
     * </p>
     *
     * @param ctx el contexto del nodo {@code exp} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitExp(ExpContext ctx) {
        if (ctx.SUMA() != null) {
            // Guardar el resultado actual en la pila
            codigo.append("    push eax\n");
            
            // Evaluar el término derecho
            visitTermino(ctx.termino());
            
            // Recuperar operando izquierdo y sumar
            codigo.append("    mov ebx, eax\n");
            codigo.append("    pop eax\n");
            codigo.append("    add eax, ebx  ; suma\n");
            
            // Continuar con el resto de la expresión
            visitExp(ctx.exp());
        } else if (ctx.RESTA() != null) {
            // Guardar el resultado actual en la pila
            codigo.append("    push eax\n");
            
            // Evaluar el término derecho
            visitTermino(ctx.termino());
            
            // Recuperar operando izquierdo y restar
            codigo.append("    mov ebx, eax\n");
            codigo.append("    pop eax\n");
            codigo.append("    sub eax, ebx  ; resta\n");
            
            // Continuar con el resto de la expresión
            visitExp(ctx.exp());
        }
        
        return "";
    }

    /**
     * Procesa operaciones de multiplicación, división y módulo.
     * <p>
     * Genera código para operaciones de mayor precedencia usando
     * instrucciones específicas de x86.
     * </p>
     *
     * @param ctx el contexto del nodo {@code term} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitTerm(TermContext ctx) {
        if (ctx.MULT() != null) {
            codigo.append("    push eax\n");
            visitFactor(ctx.factor());
            codigo.append("    mov ebx, eax\n");
            codigo.append("    pop eax\n");
            codigo.append("    imul eax, ebx  ; multiplicación\n");
            visitTerm(ctx.term());
        } else if (ctx.DIV() != null) {
            codigo.append("    push eax\n");
            visitFactor(ctx.factor());
            codigo.append("    mov ebx, eax\n");
            codigo.append("    pop eax\n");
            codigo.append("    cdq            ; extender signo para división\n");
            codigo.append("    idiv ebx       ; división\n");
            visitTerm(ctx.term());
        } else if (ctx.MOD() != null) {
            codigo.append("    push eax\n");
            visitFactor(ctx.factor());
            codigo.append("    mov ebx, eax\n");
            codigo.append("    pop eax\n");
            codigo.append("    cdq            ; extender signo\n");
            codigo.append("    idiv ebx       ; división\n");
            codigo.append("    mov eax, edx   ; módulo (resto en EDX)\n");
            visitTerm(ctx.term());
        }
        
        return "";
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
        if (ctx.NUMERO() != null) {
            // Cargar número literal en EAX
            String numero = ctx.NUMERO().getText();
            codigo.append("    mov eax, ").append(numero).append("\n");
        } else if (ctx.ID() != null) {
            // Cargar valor de variable en EAX
            String variable = ctx.ID().getText();
            codigo.append("    mov eax, [").append(variable).append("]\n");
        } else if (ctx.expresion() != null) {
            // Expresión entre paréntesis
            visitExpresion(ctx.expresion());
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
        
        return "";
    }

    /**
     * Procesa una estructura for y genera código de bucle con inicialización.
     * <p>
     * Traduce el bucle for a una estructura equivalente con inicialización,
     * condición de continuación y expresión de actualización.
     * </p>
     *
     * @param ctx el contexto del nodo {@code ifor} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitIfor(IforContext ctx) {
        String etiquetaInicio = generarEtiqueta();
        String etiquetaFin = generarEtiqueta();
        
        codigo.append("\n    ; Estructura FOR\n");
        
        // Visitar la declaración/inicialización del for
        visitCiclo(ctx.ciclo());
        
        codigo.append(etiquetaInicio).append(":\n");
        
        // La condición ya fue procesada en ciclo
        // Saltar al final si es falsa
        codigo.append("    je ").append(etiquetaFin).append("  ; salir si falso\n");
        
        // Cuerpo del for
        visitBloque(ctx.bloque());
        
        // Volver al inicio
        codigo.append("    jmp ").append(etiquetaInicio).append("  ; repetir bucle\n");
        codigo.append(etiquetaFin).append(":\n");
        
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
        visitComparacion(ctx.comparacion());
        // Las comparaciones adicionales (AND/OR) se procesarían aquí
        return "";
    }

    /**
     * Procesa una comparación entre dos valores.
     * <p>
     * Genera código que compara dos operandos y establece los flags apropiados
     * del procesador para instrucciones de salto condicional.
     * </p>
     * <p>
     * <b>Operadores soportados:</b>
     * <ul>
     *   <li>== (igual)</li>
     *   <li>!= (diferente)</li>
     *   <li>&gt; (mayor que)</li>
     *   <li>&lt; (menor que)</li>
     * </ul>
     * </p>
     *
     * @param ctx el contexto del nodo {@code comparacion} del árbol sintáctico
     * @return cadena vacía
     */
    @Override
    public String visitComparacion(ComparacionContext ctx) {
        // Evaluar el factor izquierdo
        visitFactor((FactorContext) ctx.getChild(0));
        codigo.append("    push eax\n");
        
        // Evaluar el factor derecho
        visitFactor((FactorContext) ctx.getChild(2));
        codigo.append("    mov ebx, eax\n");
        codigo.append("    pop eax\n");
        
        // Realizar la comparación
        codigo.append("    cmp eax, ebx  ; comparación\n");
        
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
            // Escribir la sección de datos primero
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

