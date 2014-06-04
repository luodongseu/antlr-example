import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;

public class Main {

	public static void main(String[] args) throws Exception {
        int argp = 0;
        boolean debug = false, showParsed = false;
        String expression = "12 * (5 - 6)";

        while (argp < args.length) {
            if ("-parse".equals(args[argp])) {
                showParsed = true;
                argp++;
            }
            else if ("-debug".equals(args[argp])) {
                debug = true;
                argp++;
            }
            else {
                break;
            }
        }

        if (argp < args.length) {
            expression = "";
            while (argp < args.length) {
                expression += " " + args[argp++];
            }
        }

        Node value = new Parser(expression).debug(debug).value();
        if (showParsed)
            System.out.println(value);
        else
            System.out.println(value.evaluate());
    }
}
