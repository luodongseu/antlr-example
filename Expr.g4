grammar Expr;

eval returns [Node value]
    :    exp=additionExp {$value = $exp.value;}
    ;

additionExp returns [Node value]
    :    m1=multiplyExp       {$value = $m1.value;}
         ( '+' m2=multiplyExp {$value = new Node(Node.ADD, $value, $m2.value);}
         | '-' m2=multiplyExp {$value = new Node(Node.SUBTRACT, $value, $m2.value);}
         )* 
    ;

multiplyExp returns [Node value]
    :    a1=atomExp             {$value = $a1.value;}
         ( '*'       a2=atomExp {$value = new Node(Node.MULTIPLY, $value, $a2.value);}
         | 'x'       a2=atomExp {$value = new Node(Node.MULTIPLY, $value, $a2.value);}
         | '/'       a2=atomExp {$value = new Node(Node.DIVIDE, $value, $a2.value);}
         )* 
    ;

atomExp returns [Node value]
    :    n=Number                {$value = new Node(Node.NUMBER, Double.parseDouble($n.text));}
    |    '(' exp=additionExp ')' {$value = $exp.value;}
    ;

Number
    :    ('0'..'9')+ ('.' ('0'..'9')+)?
    ;

WS
    :    (' '|'\t')   {skip();}
    ;