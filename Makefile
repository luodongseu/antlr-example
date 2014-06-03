.SUFFIXES: .java .class ;

.java.class:
	javac $(@:.class=.java)

GENERATEDCLASSES=ExprBaseListener.class ExprLexer.class ExprListener.class ExprParser.class
MAINCLASS=Main.class
CLASSES=$(GENERATEDCLASSES) $(MAINCLASS)
GENERATEDJAVAS=$(GENERATEDCLASSES:.class=.java)

run: $(CLASSES)
	java Main

clean:
	rm -f *.class *.tokens Expr*.java

antlr: $(GENERATEDJAVAS)
compile: $(CLASSES)

$(GENERATEDJAVAS): Expr.g4
	java -jar /usr/local/lib/antlr-4.2.2-complete.jar Expr.g4