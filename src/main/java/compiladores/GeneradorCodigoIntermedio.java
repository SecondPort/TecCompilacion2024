package compiladores;

import org.antlr.v4.runtime.tree.TerminalNode;
import compiladores.compiladoresParser.*;
import java.util.ArrayList;
import java.util.List;

public class GeneradorCodigoIntermedio extends compiladoresBaseVisitor<String> {
    private List<Instruccion> instrucciones = new ArrayList<>();
    private int tempCounter = 0;
    private int labelCounter = 0;

    public List<Instruccion> getInstrucciones() {
        return instrucciones;
    }

    private String newTemp() {
        return "t" + (tempCounter++);
    }

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
        String left = visit(ctx.termino());
        return processExp(ctx.exp(), left);
    }

    private String processExp(ExpContext ctx, String left) {
        if (ctx.getChildCount() == 0) return left;
        
        String op = "";
        if (ctx.SUMA() != null) op = "+";
        else if (ctx.RESTA() != null) op = "-";
        
        String right = visit(ctx.termino());
        String temp = newTemp();
        instrucciones.add(new Instruccion(op, left, right, temp));
        
        return processExp(ctx.exp(), temp);
    }

    @Override
    public String visitTermino(TerminoContext ctx) {
        String left = visit(ctx.factor());
        return processTerm(ctx.term(), left);
    }

    private String processTerm(TermContext ctx, String left) {
        if (ctx.getChildCount() == 0) return left;

        String op = "";
        if (ctx.MULT() != null) op = "*";
        else if (ctx.DIV() != null) op = "/";
        else if (ctx.MOD() != null) op = "%";

        String right = visit(ctx.factor());
        String temp = newTemp();
        instrucciones.add(new Instruccion(op, left, right, temp));
        
        return processTerm(ctx.term(), temp);
    }

    @Override
    public String visitFactor(FactorContext ctx) {
        if (ctx.NUMERO() != null) return ctx.NUMERO().getText();
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
        
        instrucciones.add(new Instruccion("label", null, null, labelStart));
        String cond = visit(ctx.condicion());
        
        instrucciones.add(new Instruccion("if", cond, null, labelTrue));
        instrucciones.add(new Instruccion("goto", null, null, labelEnd));
        
        instrucciones.add(new Instruccion("label", null, null, labelTrue));
        visit(ctx.bloque());
        instrucciones.add(new Instruccion("goto", null, null, labelStart));
        
        instrucciones.add(new Instruccion("label", null, null, labelEnd));
        
        return null;
    }

    @Override
    public String visitIfor(IforContext ctx) {
        // ifor : FOR ciclo bloque ;
        // ciclo : PA declaracion comparacion PYC finfor PC ;
        
        // We need to decompose 'ciclo' manually or visit it
        // Structure: init -> labelStart -> cond -> if true goto labelBody, else goto labelEnd
        // labelBody -> block -> update -> goto labelStart
        // labelEnd
        
        CicloContext ciclo = ctx.ciclo();
        visit(ciclo.declaracion()); // Init
        
        String labelStart = newLabel();
        String labelTrue = newLabel();
        String labelEnd = newLabel();
        
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
        String left = visit(ctx.comparacion());
        return processListaComp(ctx.listacomp(), left);
    }
    
    private String processListaComp(ListacompContext ctx, String left) {
        if (ctx.getChildCount() == 0) return left;
        
        String op = "";
        if (ctx.AND() != null) op = "&&";
        else if (ctx.OR() != null) op = "||";
        
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
}

