import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;

public class Parser {

    private Node value = null;
    private ExprParser parser;

    public Parser(String expression) {
        ExprLexer lexer = new ExprLexer(new ANTLRInputStream(expression));
        parser = new ExprParser(new CommonTokenStream(lexer));
    }

    public Parser debug() {
        parser.setTrace(true);
        value = null;
        return this;
    }

    public Parser debug(boolean dbg) {
        parser.setTrace(dbg);
        value = null;
        return this;
    }

    private void go() {
        if (value == null) {
            value = parser.eval().value;
        }
    }

    public Node value() {
        go();
        return value;
    }

    public String parsed() {
        go();
        return "" + value;
    }

    public double evaluate() {
        go();
        return value.evaluate();
    }
}
