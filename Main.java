import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;

public class Main {

	public static final boolean DEBUG = false;
	
	public static void main(String[] args) throws Exception {
		ANTLRInputStream in = new ANTLRInputStream("12*(5-6)");
        ExprLexer lexer = new ExprLexer(in);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExprParser parser = new ExprParser(tokens);
        if (DEBUG) {
            parser.addParseListener(new ParseTreeListener() {
    			public void visitTerminal(@NotNull TerminalNode node) {}
    			public void visitErrorNode(@NotNull ErrorNode node) {}
    			public void enterEveryRule(@NotNull ParserRuleContext ctx) {}
    			public void exitEveryRule(@NotNull ParserRuleContext ctx) {}
            });
            parser.setTrace(true);
        }
        System.out.println(parser.eval().value);
    }
}
