/*
 *  Groovy parser+lexer for simple arithmetic expressions
 */

interface Token {
	String getRegex()
}

class Operand implements Token {
	String getRegex() {
		""
	}
	Object value
	Operand(value) {
		this.value = value
	}
}

class Number extends Operand {
	String toString() { "" + value }
	static String getRegex() { ~"^[0-9.]+" }
	Number(value) {
		super(value)
	}
}

abstract class Operator implements Token {
	abstract int getPrecedence()
	abstract Object action(x, y)
}

abstract class LRParser {
	def tokens = []
	def operandStack = []
	def operatorStack = []

	abstract String lexToken(String str)

	def lexOperatorToken(string) {
		for (Token token : tokens) {
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
		def y = operandStack.pop().value
		def op2 = operatorStack.pop()
		operandStack << new Operand(op2.action(operandStack.pop().value, y))
	}

	def consumeToken(tkn) {
		if (tkn instanceof Operator) {
			while (operatorStack.size > 0 && 
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

class ArithmeticParser extends LRParser {

	String lexToken(String string) {
		def (found, str) = lexOperatorToken(string)
		if (!found) {
			def matches = str =~ Number.getRegex()
			if (matches.size() == 1) {
				consumeToken(new Number(Integer.parseInt(matches[0])))
				str = str.substring(matches[0].size())
			} else {
				// skipping whitespace
				assert str.substring(0, 1) == ' '
				str = str.substring(1)
			}
		}
		str
	}

	def ArithmeticParser() {
		tokens = [
			new Operator() {
				int getPrecedence() { 1 }
				String getRegex() { ~/^\+/ }
				Object action(x, y) { x + y }
			},
			new Operator() {
				int getPrecedence() { 1 }
				String getRegex() { ~/^\-/ }
				Object action(x, y) { x - y }
			},
			new Operator() {
				int getPrecedence() { 2 }
				String getRegex() { ~/^\*/ }
				Object action(x, y) { x * y }
			},
			new Operator() {
				int getPrecedence() { 2 }
				String getRegex() { ~/^\// }
				Object action(x, y) { x / y }
			}
		]
	}
}


class CompilingArithmeticParser extends ArithmeticParser {

	class BinaryOp {
		Object left, right
		String name
		String toString() {
			'(' + left + ' ' + name + ' ' + right + ')'
		}
		BinaryOp(x, y, op) {
			left = x
			right = y
			name = op
		}
	}

	def CompilingArithmeticParser() {
		tokens = [
			new Operator() {
				String toString() { "+" }
				String getRegex() { ~/^\+/ }
				int getPrecedence() { 1 }
				Object action(x, y) { new BinaryOp(x, y, '+') }
			},
			new Operator() {
				String toString() { "-" }
				String getRegex() { ~/^\-/ }
				int getPrecedence() { 1 }
				Object action(x, y) { new BinaryOp(x, y, '-') }
			},
			new Operator() {
				String toString() { "*" }
				String getRegex() { ~/^\*/ }
				int getPrecedence() { 2 }
				Object action(x, y) { new BinaryOp(x, y, '*') }
			},
			new Operator() {
				String toString() { "/" }
				String getRegex() { ~/^\// }
				int getPrecedence() { 2 }
				Object action(x, y) { new BinaryOp(x, y, '/') }
			}
		]
	}
}


def p = new CompilingArithmeticParser();
// def p = new ArithmeticParser();

println(p.parse("2*3 + 4/5"))