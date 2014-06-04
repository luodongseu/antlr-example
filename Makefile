.SUFFIXES: .java .class

.java.class:
	mkdir -p build
	javac -cp build:/usr/local/lib/antlr-4.2.2-complete.jar -sourcepath build -d build $(@:.class=.java)

JARFILE=expr-grammar.jar
CLASSPATH=build:build/$(JARFILE):/usr/local/lib/antlr-4.2.2-complete.jar

GENERATEDCLASSES=build/ExprBaseListener.class build/ExprLexer.class build/ExprListener.class build/ExprParser.class
GENERATEDJAVAS=build/ExprBaseListener.java build/ExprLexer.java build/ExprListener.java build/ExprParser.java

jar: build/$(JARFILE)

run: compile
	java -cp $(CLASSPATH) Main

compile: build/$(JARFILE) build/Main.class

clean:
	rm -rf build

antlr: $(GENERATEDJAVAS)

generated: build/Node.class $(GENERATEDCLASSES) build/Parser.class

build/$(JARFILE):
	make generated
	(cd build; jar cf $(JARFILE) Expr*.class Node.class Parser.class)
	rm -f build/Expr* build/Node.class build/Parser.class   # clean up after creating jar

build/Main.class: src/Main.java
	mkdir -p build
	javac -cp $(CLASSPATH) -d build src/Main.java

build/Node.class: src/Node.java
	mkdir -p build
	javac -cp $(CLASSPATH) -d build src/Node.java

build/Parser.class: src/Parser.java
	mkdir -p build
	javac -cp $(CLASSPATH) -d build src/Parser.java

$(GENERATEDJAVAS): Expr.g4
	java -jar /usr/local/lib/antlr-4.2.2-complete.jar Expr.g4
	mkdir -p build
	mv Expr*.java build
	mv *.tokens build