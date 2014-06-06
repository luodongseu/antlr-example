/*
 *  Groovy parser+lexer for simple arithmetic expressions
 */

import java.util.regex.Pattern;

abstract class LRParser {

	interface Token {}

	static class Operand implements Token {
		Object value
		Operand(value) { this.value = value }
	}

	static abstract class Operator implements Token {
		Integer precedence
		String regex
		Closure op
		Operator(Integer precedence, Pattern regex, Closure op) {
			this.precedence = precedence
			this.regex = regex
			this.op = op
		}
		abstract Object action()
	}

	class SpecialCaseOperator extends Operator {
		SpecialCaseOperator(Pattern regex) { super(0, regex, null) }
		Object action() { }
	}

	def operators = []
	def operandStack = []
	def operatorStack = []
	def leftParen = new SpecialCaseOperator(~"^\\(")
	def rightParen = new SpecialCaseOperator(~"^\\)")

	LRParser() {
		operators << leftParen
		operators << rightParen
		additonalTokens()
	}

	void additonalTokens() {
		// overload me
	}

	/**
	 *   Parse things that aren't operators.
	 */
	abstract String lexToken(String str)

	def lexOperatorToken(string) {
		for (Token token : operators) {
			def matches = string =~ token.getRegex()
			if (matches.size() == 1) {
				consumeToken(token)
				string = string.substring(matches[0].size())
				return [true, string]
			}
		}
		return [false, string]
	}

	def doOneOp() {
		if (operandStack.size == 0) {
			assert operatorStack.size == 0
			return
		}
		Operator op = operatorStack.pop()
		operandStack << new Operand(op.action())
	}

	def consumeToken(tkn) {
		if (tkn == leftParen) {
			operatorStack << leftParen
		} else if (tkn == rightParen) {
			while (operatorStack.size > 0 &&
					operatorStack[operatorStack.size() - 1] != leftParen) {
				doOneOp()
			}
			operatorStack.pop()
		} else if (tkn instanceof Operator) {
			while (operatorStack.size > 0 &&
					operatorStack[operatorStack.size() - 1] != leftParen &&
					operatorStack[operatorStack.size() - 1].getPrecedence() >= tkn.getPrecedence()) {
				doOneOp()
			}
			operatorStack << tkn
		} else {
			operandStack << tkn
		}
		assert operatorStack.size() > 0 || operandStack.size() > 0
	}

	def parse(str) {
		while (str.size() > 0) {
			str = lexToken(str)
		}
		while (operatorStack.size > 0) {
			doOneOp()
		}
		operandStack[operandStack.size - 1].value
	}
}

/* = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = */

class ArithmeticParser extends LRParser {

	static class Number extends LRParser.Operand {
		Number(value) { super(value) }
	}

	class BinaryOperator extends LRParser.Operator {
		BinaryOperator(Integer precedence, Pattern regex, Closure op) { super(precedence, regex, op) }
		Object action() {
			def y = operandStack.pop().value
			def x = operandStack.pop().value
			op(x, y)
		}
	}

	String lexToken(String string) {
		def (found, str) = lexOperatorToken(string)
		if (!found) {
			def matches = str =~ ~"^[0-9.]+"
			if (matches.size() == 1) {
				consumeToken(new Number(Double.parseDouble(matches[0])))
				str = str.substring(matches[0].size())
			} else {
				// skipping whitespace
				assert str.substring(0, 1) == ' '
				str = str.substring(1)
			}
		}
		str
	}

	void additonalTokens() {
		operators << new BinaryOperator(1, ~"^\\+", { x, y -> x + y })
		operators << new BinaryOperator(1, ~"^-",   { x, y -> x - y })
		operators << new BinaryOperator(2, ~"^\\*", { x, y -> x * y })
		operators << new BinaryOperator(2, ~"^/",   { x, y -> x / y })
	}
}

assert new ArithmeticParser().parse("2 * 3 + 4 / 5") == 6.8
assert new ArithmeticParser().parse("2 * (3 + 4) / 5") == 2.8

/* = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = */

class CompilingArithmeticParser extends ArithmeticParser {

	class BinaryOperator extends ArithmeticParser.BinaryOperator {
		BinaryOperator(int precedence, Pattern regex, String name) {
			super(precedence, regex, {x, y ->
				ParseTreeNode node = new ParseTreeNode()
				node.left = x
				node.right = y
				node.name = name
				node
			})
		}

		static class ParseTreeNode {
			Object left, right
			String name
			String toString() { '(' + left + ' ' + name + ' ' + right + ')' }
		}
	}

	void additonalTokens() {
		operators << new BinaryOperator(1, ~"^\\+", "+")
		operators << new BinaryOperator(1, ~"^-",   "-")
		operators << new BinaryOperator(2, ~"^\\*", "*")
		operators << new BinaryOperator(2, ~"^/",   "/")
	}
}


assert new CompilingArithmeticParser().parse("2 * 3 + 4 / 5").toString() ==
	'((2.0 * 3.0) + (4.0 / 5.0))'
assert new CompilingArithmeticParser().parse("2 * (3 + 4) / 5").toString() ==
	'((2.0 * (3.0 + 4.0)) / 5.0)'