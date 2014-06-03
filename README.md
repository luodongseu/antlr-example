Hacking Antlr
==

This is based on two sources. One is the tiny demo at
http://www.antlr.org/ in the tabbed box on the right side of the home page.
The other is first answer to http://stackoverflow.com/questions/1931307.

While this answer is wonderfully detailed, I found I needed to tinker a bit
to get it to work, probably because Antlr has evolved a bit since that
answer was posted. It turns out

```java
		System.out.println(parser.eval());
```

no longer works because `parser.eval` now returns an instance of
`ExprParser.EvalContext`, whose `value` is the final answer to the
arithmetic problem.

Now because I am an ancient fossil, I am using a Makefile instead of
Ant (old and busted) or Maven (new hotness).

Prerequisites
--

You should do these things first.

```bash
(cd /usr/local/lib; sudo curl -O http://www.antlr.org/download/antlr-4.2.2-complete.jar)
export CLASSPATH=".:/usr/local/lib/antlr-4.2.2-complete.jar:$CLASSPATH"
alias antlr4='java -jar /usr/local/lib/antlr-4.2.2-complete.jar'
alias grun='java org.antlr.v4.runtime.misc.TestRig'
```