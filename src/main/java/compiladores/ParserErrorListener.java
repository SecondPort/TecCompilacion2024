package compiladores;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

/**
 * ErrorListener sintáctico personalizado que redirige los errores del parser
 * al sistema centralizado de mensajes {@link Reportador}.
 */
public class ParserErrorListener extends BaseErrorListener {

    private final Reportador reportador = Reportador.getInstancia();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        String lexema = "";
        if (offendingSymbol instanceof Token) {
            Token t = (Token) offendingSymbol;
            lexema = t.getText();
        }

        String detalle;
        if (msg.contains("mismatched input")) {
            detalle = "Entrada no coincidente";
        } else if (msg.contains("no viable alternative")) {
            detalle = "Alternativa sintáctica no viable";
        } else if (msg.contains("missing")) {
            detalle = "Token faltante";
        } else if (msg.contains("extraneous input")) {
            detalle = "Token extra no esperado";
        } else {
            detalle = "Error sintáctico";
        }

        String mensaje = detalle +
                (lexema != null && !lexema.isEmpty() ? " cerca de '" + lexema + "'" : "") +
                ": " + msg;

        reportador.error(mensaje, line, charPositionInLine);
    }
}
