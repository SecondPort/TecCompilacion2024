package compiladores;

public class Instruccion {
    public String op;
    public String arg1;
    public String arg2;
    public String result;

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

