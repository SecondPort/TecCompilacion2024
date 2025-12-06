package compiladores;

import org.antlr.v4.runtime.tree.TerminalNode;
import compiladores.compiladoresParser.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Generador de código intermedio en forma de instrucciones de tres direcciones.
 * <p>
 * Recorre el árbol sintáctico generado por ANTLR usando el patrón Visitor y
 * construye una lista de {@link Instruccion} que representa asignaciones,
 * expresiones, comparaciones, estructuras de control (if/while/for) y
 * construcciones de alto nivel como llamadas a funciones y sentencias return.
 * </p>
 * <p>
 * También mantiene pilas de etiquetas para soportar correctamente las
 * sentencias <code>break</code> y <code>continue</code> en bucles anidados.
 * </p>
 */
public class GeneradorCodigoIntermedio extends compiladoresBaseVisitor<String> {
    private List<Instruccion> instrucciones = new ArrayList<>();
    private int tempCounter = 0;
    private int labelCounter = 0;
    private List<String> breakLabels = new ArrayList<>();
    private List<String> continueLabels = new ArrayList<>();

    /**
     * Devuelve la lista de instrucciones de tres direcciones generadas.
     *
     * @return lista inmutable de instrucciones generadas durante la visita
     */
    public List<Instruccion> getInstrucciones() {
        return instrucciones;
    }

    /**
     * Crea un nuevo nombre de temporal único (t0, t1, ...).
     *
     * @return identificador del nuevo temporal
     */
    private String newTemp() {
        return "t" + (tempCounter++);
    }

    /**
     * Crea una nueva etiqueta única (l0, l1, ...).
     *
     * @return identificador de la nueva etiqueta
     */
    private String newLabel() {
        return "l" + (labelCounter++);
    }

    @Override
    public String visitAsignacion(AsignacionContext ctx) {
        String id = ctx.ID().getText();
        String val = visit(ctx.expresion());
        instrucciones.add(new Instruccion("=", val, null, id));
        return id;
    }

    @Override
    public String visitExpresion(ExpresionContext ctx) {
        if (ctx == null) {
            return "";
        }

        // Caso unario -expr
        if (ctx.RESTA() != null && ctx.expresion().size() == 1) {
            String valor = visit(ctx.expresion(0));
            String cero = "0";
            String temp = newTemp();
            instrucciones.add(new Instruccion("-", cero, valor, temp));
            return temp;
        }

        // Caso binario: expr op expr
        if (ctx.expresion().size() == 2) {
            String left = visit(ctx.expresion(0));
            String right = visit(ctx.expresion(1));
            String op = ctx.SUMA() != null ? "+" :
                        ctx.RESTA() != null ? "-" :
                        ctx.MULT() != null ? "*" :
                        ctx.DIV() != null ? "/" :
                        ctx.MOD() != null ? "%" :
                        ctx.EQ() != null ? "==" :
                        ctx.UEQ() != null ? "!=" :
                        ctx.MAYOR() != null ? ">" :
                        ctx.MENOR() != null ? "<" :
                        ctx.MAYORIGUAL() != null ? ">=" :
                        ctx.MENORIGUAL() != null ? "<=" :
                        ctx.AND() != null ? "&&" :
                        ctx.OR() != null ? "||" : "";

            String temp = newTemp();
            instrucciones.add(new Instruccion(op, left, right, temp));
            return temp;
        }

        // Caso !expr (unario lógico)
        if (ctx.getChildCount() == 2 && ctx.getChild(0).getText().equals("!")) {
            String valor = visit(ctx.expresion(0));
            String temp = newTemp();
            instrucciones.add(new Instruccion("!", valor, null, temp));
            return temp;
        }

        // Caso base: factor
        if (ctx.factor() != null) {
            return visit(ctx.factor());
        }

        return "";
    }

    @Override
    public String visitFactor(FactorContext ctx) {
        if (ctx.NUMERO() != null) return ctx.NUMERO().getText();
        if (ctx.CHAR_CONST() != null) return ctx.CHAR_CONST().getText();
        if (ctx.ID() != null) return ctx.ID().getText();
        if (ctx.llamada_expr() != null) return visit(ctx.llamada_expr());
        if (ctx.expresion() != null) return visit(ctx.expresion());
        return "";
    }

    @Override
    public String visitIif(IifContext ctx) {
        String cond = visit(ctx.condicion());
        String labelTrue = newLabel();
        String labelFalse = newLabel();
        
        // if cond goto labelTrue
        instrucciones.add(new Instruccion("if", cond, null, labelTrue));
        // goto labelFalse (if condition fails)
        instrucciones.add(new Instruccion("goto", null, null, labelFalse));
        
        instrucciones.add(new Instruccion("label", null, null, labelTrue));
        visit(ctx.bloque());
        
        // Handle else
        if (ctx.ielse() != null && ctx.ielse().getChildCount() > 0) {
            String labelEnd = newLabel();
            instrucciones.add(new Instruccion("goto", null, null, labelEnd));
            instrucciones.add(new Instruccion("label", null, null, labelFalse));
            visit(ctx.ielse());
            instrucciones.add(new Instruccion("label", null, null, labelEnd));
        } else {
            instrucciones.add(new Instruccion("label", null, null, labelFalse));
        }
        
        return null;
    }

    @Override
    public String visitIwhile(IwhileContext ctx) {
        String labelStart = newLabel();
        String labelTrue = newLabel();
        String labelEnd = newLabel();

        breakLabels.add(labelEnd);
        continueLabels.add(labelStart);

        instrucciones.add(new Instruccion("label", null, null, labelStart));
        String cond = visit(ctx.condicion());

        instrucciones.add(new Instruccion("if", cond, null, labelTrue));
        instrucciones.add(new Instruccion("goto", null, null, labelEnd));

        instrucciones.add(new Instruccion("label", null, null, labelTrue));
        visit(ctx.bloque());
        instrucciones.add(new Instruccion("goto", null, null, labelStart));

        instrucciones.add(new Instruccion("label", null, null, labelEnd));

        breakLabels.remove(breakLabels.size() - 1);
        continueLabels.remove(continueLabels.size() - 1);

        return null;
    }

    @Override
    public String visitIfor(IforContext ctx) {
        // ifor : FOR ciclo bloque ;
        // ciclo : PA (declaracion | asignacion) comparacion PYC finfor PC ;
        
        // We need to decompose 'ciclo' manually or visit it
        // Structure: init -> labelStart -> cond -> if true goto labelBody, else goto labelEnd
        // labelBody -> block -> update -> goto labelStart
        // labelEnd
        
        CicloContext ciclo = ctx.ciclo();

        // Inicialización: puede ser una declaración o una asignación
        if (ciclo.declaracion() != null) {
            visit(ciclo.declaracion());
        } else if (ciclo.asignacion() != null) {
            visit(ciclo.asignacion());
        }

        String labelStart = newLabel();
        String labelTrue = newLabel();
        String labelEnd = newLabel();

        breakLabels.add(labelEnd);
        continueLabels.add(labelStart);

        instrucciones.add(new Instruccion("label", null, null, labelStart));

        // Condición general: la parte central del for es una expresion
        String cond = visit(ciclo.expresion());

        instrucciones.add(new Instruccion("if", cond, null, labelTrue));
        instrucciones.add(new Instruccion("goto", null, null, labelEnd));

        instrucciones.add(new Instruccion("label", null, null, labelTrue));
        visit(ctx.bloque());
        visit(ciclo.finfor()); // Update
        instrucciones.add(new Instruccion("goto", null, null, labelStart));

        instrucciones.add(new Instruccion("label", null, null, labelEnd));

        breakLabels.remove(breakLabels.size() - 1);
        continueLabels.remove(continueLabels.size() - 1);

        return null;
    }

    @Override
    public String visitIbreak(IbreakContext ctx) {
        if (!breakLabels.isEmpty()) {
            String destino = breakLabels.get(breakLabels.size() - 1);
            instrucciones.add(new Instruccion("goto", null, null, destino));
        }
        return null;
    }

    @Override
    public String visitIcontinue(IcontinueContext ctx) {
        if (!continueLabels.isEmpty()) {
            String destino = continueLabels.get(continueLabels.size() - 1);
            instrucciones.add(new Instruccion("goto", null, null, destino));
        }
        return null;
    }
    
    // Helper for for-loop update step
    @Override
    public String visitFinfor(FinforContext ctx) {
        // Caso 1: expresión genérica como actualización (se evalúa y se descarta)
        if (ctx.expresion() != null && ctx.ASIGN() == null && ctx.ID() == null) {
            return visit(ctx.expresion());
        }

        // Caso 2: ID++ o ID--
        if (ctx.ID() != null && (ctx.INCREMENTO() != null || ctx.DECREMENTO() != null) && ctx.ASIGN() == null) {
            String id = ctx.ID().getText();
            String op = ctx.INCREMENTO() != null ? "+" : "-";
            instrucciones.add(new Instruccion(op, id, "1", id));
            return id;
        }

        // Caso 3: asignación tipo ID = expresion
        if (ctx.ID() != null && ctx.ASIGN() != null && ctx.expresion() != null) {
            String id = ctx.ID().getText();
            String val = visit(ctx.expresion());
            instrucciones.add(new Instruccion("=", val, null, id));
            return id;
        }

        return "";
    }

    @Override
    public String visitCondicion(CondicionContext ctx) {
        if (ctx == null || ctx.expresion() == null) {
            return "";
        }
        return visit(ctx.expresion());
    }

    @Override
    public String visitLlamadafunc(LlamadafuncContext ctx) {
        // llamadafunc ahora es una sentencia: llamada_expr PYC
        String resultado = visit(ctx.llamada_expr());
        // El valor de retorno queda en 'resultado', pero como sentencia no lo usamos
        return null;
    }

    @Override
    public String visitLlamada_expr(compiladoresParser.Llamada_exprContext ctx) {
        String nombre = ctx.ID().getText();
        String resultado = newTemp();
        String argsTemp = visit(ctx.factorfunc());
        instrucciones.add(new Instruccion("call", nombre, argsTemp, resultado));
        return resultado;
    }

    @Override
    public String visitFactorfunc(FactorfuncContext ctx) {
        // Por simplicidad, devolvemos el último argumento evaluado (o vacío si no hay)
        if (ctx.NUMERO() != null) {
            return ctx.NUMERO().getText();
        }
        if (ctx.ID() != null) {
            return ctx.ID().getText();
        }
        if (ctx.expresion() != null) {
            return visit(ctx.expresion());
        }
        if (ctx.listafactfunc() != null) {
            return visit(ctx.listafactfunc());
        }
        return "";
    }

    @Override
    public String visitListafactfunc(ListafactfuncContext ctx) {
        if (ctx.NUMERO() != null) {
            return ctx.NUMERO().getText();
        }
        if (ctx.ID() != null) {
            return ctx.ID().getText();
        }
        if (ctx.expresion() != null) {
            return visit(ctx.expresion());
        }
        return "";
    }

    @Override
    public String visitIreturn(IreturnContext ctx) {
        String valor = "";
        if (ctx.expresion() != null) {
            valor = visit(ctx.expresion());
        }
        instrucciones.add(new Instruccion("return", valor, null, null));
        return null;
    }
}

