package compiladores;

/**
 * Representa una instrucción de código intermedio en forma de tres direcciones.
 * <p>
 * Cada instrucción modela operaciones aritméticas, lógicas, asignaciones,
 * saltos, etiquetas o llamadas de función a partir de los campos:
 * <ul>
 *   <li>{@code op}: operador u operación (p.ej. "+", "-", "=", "goto", "label", "call")</li>
 *   <li>{@code arg1}: primer operando</li>
 *   <li>{@code arg2}: segundo operando (opcional)</li>
 *   <li>{@code result}: destino de la operación o nombre de etiqueta</li>
 * </ul>
 * </p>
 */
public class Instruccion {
    /** Operación u operador de la instrucción. */
    public String op;
    /** Primer operando de la instrucción. */
    public String arg1;
    /** Segundo operando de la instrucción (puede ser {@code null}). */
    public String arg2;
    /** Resultado o destino de la instrucción (o nombre de etiqueta). */
    public String result;

    /**
     * Crea una nueva instrucción de tres direcciones.
     *
     * @param op operación u operador
     * @param arg1 primer operando
     * @param arg2 segundo operando (puede ser {@code null})
     * @param result destino o resultado de la operación
     */
    public Instruccion(String op, String arg1, String arg2, String result) {
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
    }

    @Override
    public String toString() {
        if (op.equals("label")) {
            return result + ":";
        }
        
        // Asignación simple: result = arg1
        if (op.equals("=")) {
            return result + " = " + arg1;
        }

        // Saltos incondicionales: goto result
        if (op.equals("goto")) {
            return "goto " + result;
        }

        // Saltos condicionales: if arg1 goto result (arg2 suele ser nulo en if simple, o usado para relacionales)
        // Formato común: if arg1 relop arg2 goto result
        if (op.startsWith("if")) {
             if (op.equals("if")) {
                 return "if " + arg1 + " goto " + result;
             }
             return "if " + arg1 + " " + op.substring(3) + " " + arg2 + " goto " + result; // hacky handling if op is "if<"
        }
        
        // Operaciones aritméticas/lógicas binarias: result = arg1 op arg2
        if (arg2 != null && !arg2.isEmpty()) {
            return result + " = " + arg1 + " " + op + " " + arg2;
        }
        
        // Operaciones unarias: result = op arg1
        if (arg1 != null && !arg1.isEmpty()) {
             return result + " = " + op + " " + arg1;
        }

        return op + " " + result;
    }
}

