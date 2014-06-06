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

	String inputString

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

	abstract Operand parseAtom()

	void lexToken() {
		def found = lexOperatorToken()
		if (!found) {
			found = parseAtom()
			if (found != null) {
				consumeToken(found)
			} else {
				// skipping whitespace
				assert inputString.substring(0, 1) == ' '
				inputString = inputString.substring(1)
			}
		}
	}

	boolean lexOperatorToken() {
		for (Token token : operators) {
			def matches = inputString =~ token.getRegex()
			if (matches.size() == 1) {
				consumeToken(token)
				inputString = inputString.substring(matches[0].size())
				return true
			}
		}
		return false
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
		inputString = str
		while (inputString.size() > 0) {
			lexToken()
		}
		while (operatorStack.size > 0) {
			doOneOp()
		}
		operandStack[operandStack.size - 1].value
	}
}

/* = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = */

class LogicParser extends LRParser {

	static class Boolean extends LRParser.Operand {
		Boolean(value) { super(value) }
	}

	Operand parseAtom() {
		def retval = null
		def matches = inputString =~ ~"^(TRUE|FALSE)"
		if (matches.size() == 1) {
			def value = matches[0][0]
			retval = new Boolean(value == 'TRUE')
			inputString = inputString.substring(value.size())
		}
		retval
	}

	class BinaryOperator extends LRParser.Operator {
		BinaryOperator(Integer precedence, Pattern regex, Closure op) { super(precedence, regex, op) }
		Object action() {
			def y = operandStack.pop().value
			def x = operandStack.pop().value
			op(x, y)
		}
	}

	class UnaryOperator extends LRParser.Operator {
		UnaryOperator(Integer precedence, Pattern regex, Closure op) { super(precedence, regex, op) }
		Object action() {
			def x = operandStack.pop().value
			op(x)
		}
	}

	void additonalTokens() {
		operators << new BinaryOperator(1, ~"^AND", { x, y -> x && y })
		operators << new BinaryOperator(2, ~"^OR",  { x, y -> x || y })
		operators << new UnaryOperator(3,  ~"^NOT", { x -> !x })
	}
}

assert new LogicParser().parse("TRUE AND TRUE") == true
assert new LogicParser().parse("TRUE AND FALSE OR FALSE AND TRUE") == false
assert new LogicParser().parse("TRUE AND FALSE OR TRUE AND TRUE") == true
assert new LogicParser().parse("TRUE AND TRUE OR FALSE AND TRUE") == true
assert new LogicParser().parse("TRUE AND TRUE OR TRUE AND TRUE") == true
assert new LogicParser().parse("FALSE AND (FALSE OR FALSE)") == false
assert new LogicParser().parse("FALSE AND (FALSE OR TRUE)") == false
assert new LogicParser().parse("FALSE AND (TRUE OR FALSE)") == false
assert new LogicParser().parse("FALSE AND (TRUE OR TRUE)") == false
assert new LogicParser().parse("TRUE AND (FALSE OR FALSE)") == false
assert new LogicParser().parse("TRUE AND (FALSE OR TRUE)") == true
assert new LogicParser().parse("TRUE AND (TRUE OR FALSE)") == true
assert new LogicParser().parse("TRUE AND (TRUE OR TRUE)") == true
assert new LogicParser().parse("TRUE AND NOT TRUE") == false
assert new LogicParser().parse("TRUE AND NOT FALSE") == true
assert new LogicParser().parse("TRUE AND FALSE OR NOT FALSE AND TRUE") == true
assert new LogicParser().parse("TRUE AND FALSE OR NOT TRUE AND TRUE") == false
assert new LogicParser().parse("NOT (TRUE AND NOT TRUE)") == true
assert new LogicParser().parse("NOT (TRUE AND NOT FALSE)") == false
assert new LogicParser().parse("FALSE AND NOT (TRUE AND NOT TRUE)") == false
assert new LogicParser().parse("TRUE AND NOT (TRUE AND NOT TRUE)") == true

// This should not work, but it does. "TRUE NOT" should not be grammatical.
// That's because I don't have a real BNF, just this tinkering with stacks.
assert new LogicParser().parse("TRUE AND TRUE NOT") == false

/* = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = */

class ArithmeticParser extends LRParser {

	static class Number extends LRParser.Operand {
		Number(value) { super(value) }
	}

	Operand parseAtom() {
		def retval = null
		def matches = inputString =~ ~"^[0-9]+(\\.[0-9]*)?"
		if (matches.size() == 1) {
			def value = matches[0][0]
			retval = new Number(Double.parseDouble(value))
			inputString = inputString.substring(value.size())
		}
		retval
	}

	class BinaryOperator extends LRParser.Operator {
		BinaryOperator(Integer precedence, Pattern regex, Closure op) { super(precedence, regex, op) }
		Object action() {
			def y = operandStack.pop().value
			def x = operandStack.pop().value
			op(x, y)
		}
	}

	void additonalTokens() {
		operators << new BinaryOperator(1, ~"^\\+", { x, y -> x + y })
		operators << new BinaryOperator(1, ~"^-",   { x, y -> x - y })
		operators << new BinaryOperator(2, ~"^\\*", { x, y -> x * y })
		operators << new BinaryOperator(2, ~"^/",   { x, y -> x / y })
	}
}

assert new ArithmeticParser().parse("2 * 3 + 4 / 5") == 6.8
assert new ArithmeticParser().parse("2.93 * (3 + 4) / 5") == 4.102

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