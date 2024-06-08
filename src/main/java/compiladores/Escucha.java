package compiladores;

import java.util.ArrayList;
import java.util.List;

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
import compiladores.compiladoresParser.ListafactfuncContext;
import compiladores.compiladoresParser.LlamadafuncContext;
import compiladores.compiladoresParser.ProgramaContext;
import compiladores.compiladoresParser.PrototipofuncContext;
/**
 * This class is a listener that extends the `compiladoresBaseListener` class.
 * It is responsible for listening to events during the parsing process and performing semantic analysis.
 * It keeps track of the number of nodes, tokens, and errors encountered during the compilation process.
 * It also manages a symbol table for variable and function declarations.
 */
public class Escucha extends compiladoresBaseListener {
    private Integer nodos = 0;
    private Integer tokens = 0;
    private Integer errors = 0;
    private TablaSimbolos tabla = TablaSimbolos.getInstancia();
    private List<String> erroresAcumulados = new ArrayList<>();

    @Override
    public void enterPrograma(final ProgramaContext ctx) {
        System.out.println("Comienza el parsing...");
        
        tabla.addContexto();
        super.enterPrograma(ctx);
    }

    @Override
    public void exitPrograma(final ProgramaContext ctx) {
        super.exitPrograma(ctx);
        tabla.delContexto();

        System.out.println("Fin de la compilacion");
        System.out.println("Se visitaron " + nodos + " nodos");
        System.out.println("Hay " + tokens + " tokens");
    }

    @Override
    public void enterBloque(BloqueContext ctx) {
        tabla.addContexto();
        super.enterBloque(ctx);
    }

    @Override
    public void exitBloque(BloqueContext ctx) {
        super.exitBloque(ctx);
        // Validación de llaves correctas al finalizar un bloque
        Token lastToken = ctx.getStop();
        if (lastToken == null || !lastToken.getText().equals("}")) {
            System.out.println("Error sintáctico: se esperaba '}' al final del bloque (Línea: " + ctx.getStop().getLine() + ")");
            errors++;
        }
        tabla.delContexto();
    }

    @Override
    public void exitDeclaracion(DeclaracionContext ctx) {
        super.exitDeclaracion(ctx);

        String nombre = ctx.ID().getText();

        if (tabla.contieneSimboloLocal(nombre) == false) {
            Variable nuevaVariable = new Variable();

            String tipo = ctx.getChild(0).getText();

            nuevaVariable.setNombre(nombre);
            nuevaVariable.setTipoDato(tipo);
            if (ctx.getChild(2).getText().isBlank()) {
                nuevaVariable.setInicializado(false);
            }
            else {
                nuevaVariable.setInicializado(true);
            }
            nuevaVariable.setUsado(false);

            tabla.addSimbolo(nombre, nuevaVariable);
        }
        else {
            System.out.println("Error semantico: Doble declaracion del mismo identificador (Linea: " + ctx.getStart().getLine() + ")");
            errors++;
        }
    }

    @Override
    public void exitPrototipofunc(PrototipofuncContext ctx) {
        super.exitPrototipofunc(ctx);

        String nombre = ctx.ID().getText();

        if (tabla.contieneSimboloLocal(nombre) == false) {
            Funcion nuevaFuncion = new Funcion();

            String tipo = ctx.getChild(0).getText();

            nuevaFuncion.setNombre(nombre);
            nuevaFuncion.setTipoDato(tipo);
            nuevaFuncion.setUsado(false);

            tabla.addSimbolo(nombre, nuevaFuncion);
        }
        else {
            System.out.println("Error semantico: Doble declaracion del mismo identificador (Linea: " + ctx.getStart().getLine() + ")");
            errors++;
        }
    }

    @Override
    public void exitDeclaracionfunc(DeclaracionfuncContext ctx) {
        super.exitDeclaracionfunc(ctx);

        String nombre = ctx.ID().getText();

        if (tabla.contieneSimboloLocal(nombre) == false) {
            Funcion nuevaFuncion = new Funcion();

            String tipo = ctx.getChild(0).getText();

            nuevaFuncion.setNombre(nombre);
            nuevaFuncion.setTipoDato(tipo);
            nuevaFuncion.setUsado(false);

            tabla.addSimbolo(nombre, nuevaFuncion);
        }
        else {
            System.out.println("Error semantico: Doble declaracion del mismo identificador (Linea: " + ctx.getStart().getLine() + ")");
            errors++;
        }
    }

    @Override
    public void exitAsignacion(AsignacionContext ctx) {
        super.exitAsignacion(ctx);
        if (ctx.ID() != null) {
            Id simbolo = tabla.getSimbolo(ctx.ID().getText());
            if (simbolo == null) {
                System.out.println("Error semantico: Uso de un identificador no declarado (Linea: " + ctx.getStart().getLine() + ")");
                errors++;             
            }
            else if (simbolo != null && simbolo.getInicializado() == false) {
                System.out.println("Error semantico: Uso de un identificador no inicializado (Linea: " + ctx.getStart().getLine() + ")");
                errors++;
            }
        }
    }

    @Override
    public void exitFactor(FactorContext ctx) {
        super.exitFactor(ctx);
        if (ctx.ID() != null) {
            Id simbolo = tabla.getSimbolo(ctx.ID().getText());
            if (simbolo == null) {
                System.out.println("Error semantico: Uso de un identificador no declarado (Linea: " + ctx.getStart().getLine() + ")");
                errors++;             
            }
            else if (simbolo != null && simbolo.getInicializado() == false) {
                System.out.println("Error semantico: Uso de un identificador no inicializado (Linea: " + ctx.getStart().getLine() + ")");
                errors++;
            }
        }  
    }
    
    @Override
    public void exitFinfor(FinforContext ctx) {
        super.exitFinfor(ctx);
       if (ctx.ID() != null) {
            Id simbolo = tabla.getSimbolo(ctx.ID().getText());
            if (simbolo == null) {
                System.out.println("Error semantico: Uso de un identificador no declarado (Linea: " + ctx.getStart().getLine() + ")");
                errors++;             
            }
            else if (simbolo != null && simbolo.getInicializado() == false) {
                System.out.println("Error semantico: Uso de un identificador no inicializado (Linea: " + ctx.getStart().getLine() + ")");
                errors++;
            }
        } 
    }

    @Override
    public void exitFactorfunc(FactorfuncContext ctx) {
        super.exitFactorfunc(ctx);
        if (ctx.ID() != null) {
            Id simbolo = tabla.getSimbolo(ctx.ID().getText());
            if (simbolo == null) {
                System.out.println("Error semantico: Uso de un identificador no declarado (Linea: " + ctx.getStart().getLine() + ")");
                errors++;             
            }
            else if (simbolo != null && simbolo.getInicializado() == false) {
                System.out.println("Error semantico: Uso de un identificador no inicializado (Linea: " + ctx.getStart().getLine() + ")");
                errors++;
            }
        } 
    }

    @Override
    public void exitListafactfunc(ListafactfuncContext ctx) {
        super.exitListafactfunc(ctx);
        if (ctx.ID() != null) {
            Id simbolo = tabla.getSimbolo(ctx.ID().getText());
            if (simbolo == null) {
                System.out.println("Error semantico: Uso de un identificador no declarado (Linea: " + ctx.getStart().getLine() + ")");
                errors++;             
            }
            else if (simbolo != null && simbolo.getInicializado() == false) {
                System.out.println("Error semantico: Uso de un identificador no inicializado (Linea: " + ctx.getStart().getLine() + ")");
                errors++;
            }
        } 
    }

    @Override
    public void exitLlamadafunc(LlamadafuncContext ctx) {
        super.exitLlamadafunc(ctx);
       if (ctx.ID() != null) {
            Id simbolo = tabla.getSimbolo(ctx.ID().getText());
            if (simbolo == null) {
                System.out.println("Error semantico: Uso de un identificador no declarado (Linea: " + ctx.getStart().getLine() + ")");
                errors++;             
            }
            else if (simbolo != null && simbolo.getInicializado() == false) {
                System.out.println("Error semantico: Uso de un identificador no inicializado (Linea: " + ctx.getStart().getLine() + ")");
                errors++;
            }
        } 
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        nodos++;
        super.enterEveryRule(ctx);
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
        errors++;
        super.visitErrorNode(node);
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        tokens++;
        super.visitTerminal(node);
    } 
    
}
