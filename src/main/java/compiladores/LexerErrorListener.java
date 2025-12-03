package compiladores;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

/**
 * ErrorListener léxico personalizado que redirige los errores del lexer
 * al sistema centralizado de mensajes {@link Reportador}.
 */
public class LexerErrorListener extends BaseErrorListener {

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
        String mensaje = "Error léxico" +
                (lexema != null && !lexema.isEmpty() ? " en '" + lexema + "'" : "") +
                ": " + msg;
        reportador.error(mensaje, line, charPositionInLine);
    }
}
