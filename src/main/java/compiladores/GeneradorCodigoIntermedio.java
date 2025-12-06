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
        if (ctx == null || ctx.termino() == null) {
            return "";
        }
        String left = visit(ctx.termino());
        return processExp(ctx.exp(), left);
    }

    private String processExp(ExpContext ctx, String left) {
        if (ctx == null || ctx.getChildCount() == 0) return left;
        
        String op = "";
        if (ctx.SUMA() != null) op = "+";
        else if (ctx.RESTA() != null) op = "-";
        
        if (ctx.termino() == null) return left;
        String right = visit(ctx.termino());
        String temp = newTemp();
        instrucciones.add(new Instruccion(op, left, right, temp));
        
        return processExp(ctx.exp(), temp);
    }

    @Override
    public String visitTermino(TerminoContext ctx) {
        if (ctx == null || ctx.factor() == null) {
            return "";
        }
        String left = visit(ctx.factor());
        return processTerm(ctx.term(), left);
    }

    private String processTerm(TermContext ctx, String left) {
        if (ctx == null || ctx.getChildCount() == 0) return left;

        String op = "";
        if (ctx.MULT() != null) op = "*";
        else if (ctx.DIV() != null) op = "/";
        else if (ctx.MOD() != null) op = "%";

        if (ctx.factor() == null) return left;
        String right = visit(ctx.factor());
        String temp = newTemp();
        instrucciones.add(new Instruccion(op, left, right, temp));
        
        return processTerm(ctx.term(), temp);
    }

    @Override
    public String visitFactor(FactorContext ctx) {
        if (ctx.NUMERO() != null) return ctx.NUMERO().getText();
        if (ctx.CHAR_CONST() != null) return ctx.CHAR_CONST().getText();
        if (ctx.ID() != null) return ctx.ID().getText();
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

        // Condición (comparacion)
        // comparacion returns a temp
        String cond = visit(ciclo.comparacion());

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
        if (ctx.expresion() != null) {
            return visit(ctx.expresion());
        }
        // ID INCREMENTO | ID DECREMENTO
        String id = ctx.ID().getText();
        String op = ctx.INCREMENTO() != null ? "+" : "-";
        // x = x + 1
        instrucciones.add(new Instruccion(op, id, "1", id));
        return id;
    }

    @Override
    public String visitCondicion(CondicionContext ctx) {
        if (ctx == null || ctx.comparacion() == null) {
            return "";
        }
        String left = visit(ctx.comparacion());
        return processListaComp(ctx.listacomp(), left);
    }
    
    private String processListaComp(ListacompContext ctx, String left) {
        if (ctx == null || ctx.getChildCount() == 0) return left;
        
        String op = "";
        if (ctx.AND() != null) op = "&&";
        else if (ctx.OR() != null) op = "||";
        
        if (ctx.comparacion() == null) return left;
        String right = visit(ctx.comparacion());
        String temp = newTemp();
        instrucciones.add(new Instruccion(op, left, right, temp));
        
        return processListaComp(ctx.listacomp(), temp);
    }

    @Override
    public String visitComparacion(ComparacionContext ctx) {
        String left = visit(ctx.factor(0));
        String right = visit(ctx.factor(1));
        String op = ctx.comp().getText();
        
        String temp = newTemp();
        instrucciones.add(new Instruccion(op, left, right, temp));
        return temp;
    }

    @Override
    public String visitLlamadafunc(LlamadafuncContext ctx) {
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

