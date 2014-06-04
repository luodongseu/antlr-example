import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;

public class Node {

    public static final int NUMBER = 0;
    public static final int ADD = 1;
    public static final int SUBTRACT = 2;
    public static final int MULTIPLY = 3;
    public static final int DIVIDE = 4;

    private int type = NUMBER;
    private double value = 0.0;
    protected Node leftArg, rightArg;

    public Node(int type, double value) {
        if (type != NUMBER) {
            throw new RuntimeException("Expected type NUMBER");
        }
        this.type = NUMBER;
        this.value = value;
    }

    public Node(int type, Node left, Node right) {
        this.type = type;
        this.leftArg = left;
        this.rightArg = right;
    }

    public double evaluate() {
        switch (type) {
            default:
            case NUMBER:
                return value;
            case ADD:
                return leftArg.evaluate() + rightArg.evaluate();
            case SUBTRACT:
                return leftArg.evaluate() - rightArg.evaluate();
            case MULTIPLY:
                return leftArg.evaluate() * rightArg.evaluate();
            case DIVIDE:
                return leftArg.evaluate() / rightArg.evaluate();
        }
    }

    private String toStringWithOperator(String op) {
        return leftArg + " " + rightArg + " " + op;   // RPN, easy to implement
    }

    public String toString() {
        switch (type) {
            default:
            case NUMBER:
                return "" + value;
            case ADD:
                return toStringWithOperator("+");
            case SUBTRACT:
                return toStringWithOperator("-");
            case MULTIPLY:
                return toStringWithOperator("*");
            case DIVIDE:
                return toStringWithOperator("/");
        }

    }

}
