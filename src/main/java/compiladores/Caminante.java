package compiladores;

import org.antlr.v4.runtime.tree.TerminalNode;

import compiladores.compiladoresParser.AsignacionContext;
import compiladores.compiladoresParser.ProgramaContext;

public class Caminante extends compiladoresBaseVisitor<String> {

    private Integer asignaciones = 0;
    
    @Override
    public String visitPrograma(ProgramaContext ctx) {
        String ret = super.visitPrograma(ctx);
        System.out.println("Se realizaron " + asignaciones + " asignaciones");
        return ret;
    }

    @Override
    public String visitAsignacion(AsignacionContext ctx) {
        asignaciones++;
        System.out.println("Soy una hoja y tengo " + ctx.getChildCount() + " hijos");
        System.out.println("    -> asignando valor a " + ctx.getChild(0).getText());
        return super.visitAsignacion(ctx);
    }

    @Override
    public String visitTerminal(TerminalNode node) {
        return super.visitTerminal(node);
    }
}