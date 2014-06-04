#!/bin/sh

java -cp build:build/expr-grammar.jar:/usr/local/lib/antlr-4.2.2-complete.jar Main $*
