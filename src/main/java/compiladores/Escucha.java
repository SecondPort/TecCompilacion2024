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
    // private List<String> erroresAcumulados1 = new ArrayList<>();

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
        boolean errorEncontrado = false;
    
        String nombre = ctx.ID().getText();
        // Validación semántica para evitar doble declaración
        if (!tabla.contieneSimboloLocal(nombre)) {
            Variable nuevaVariable = new Variable();
            String tipo = ctx.getChild(0).getText();  // Obtención del tipo de la variable
    
            nuevaVariable.setNombre(nombre);
            nuevaVariable.setTipoDato(tipo);
            nuevaVariable.setInicializado(!ctx.getChild(2).getText().isBlank());
            nuevaVariable.setUsado(false);
    
            tabla.addSimbolo(nombre, nuevaVariable);
        } else {
            erroresAcumulados.add("Error semantico: Doble declaracion del mismo identificador (Linea: " + ctx.getStart().getLine() + ")");
            errorEncontrado = true;
        }
    
        // Validación sintáctica del punto y coma al final de la declaración
        Token lastToken = ctx.getStop();
        if (lastToken == null || !lastToken.getText().equals(";")) {
            erroresAcumulados.add("Error sintáctico: se esperaba ';' al final de la declaración (Línea: " + ctx.getStop().getLine() + ")");
            errorEncontrado = true;
        }
    
        // Incrementamos el contador de errores solo si se encontró alguno.
        if (errorEncontrado) {
            errors++;
        }
    
        // Imprimir todos los errores acumulados al final de la ejecución o aquí mismo.
        if (!erroresAcumulados.isEmpty()) {
            for (String error : erroresAcumulados) {
                System.out.println(error);
            }
            erroresAcumulados.clear();  // Limpiar la lista después de imprimir los errores.
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
        boolean errorEncontrado = false;

        // Primero realizamos la validación semántica de la asignación.
        if (ctx.ID() != null) {
            Id simbolo = tabla.getSimbolo(ctx.ID().getText());
            if (simbolo == null) {
                erroresAcumulados.add("Error semantico: Uso de un identificador no declarado (Linea: " + ctx.getStart().getLine() + ")");
                errorEncontrado = true;           
            }
            else if (!simbolo.getInicializado()) {
                erroresAcumulados.add("Error semantico: Uso de un identificador no inicializado (Linea: " + ctx.getStart().getLine() + ")");
                errorEncontrado = true;
            }
        }

        // Luego verificamos la correcta terminación sintáctica con un punto y coma.
        Token lastToken = ctx.getStop();
        if (lastToken == null || !lastToken.getText().equals(";")) {
            erroresAcumulados.add("Error sintáctico: se esperaba ';' al final de la asignación (Línea: " + ctx.getStop().getLine() + ")");
            errorEncontrado = true;
        }

        // Incrementamos el contador de errores solo si se encontró alguno.
        if (errorEncontrado) {
            errors++;
        }

        // Imprimir todos los errores acumulados al final de la ejecución o aquí mismo.
        if (!erroresAcumulados.isEmpty()) {
            for (String error : erroresAcumulados) {
                System.out.println(error);
            }
            erroresAcumulados.clear();  // Limpiar la lista después de imprimir los errores.
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
