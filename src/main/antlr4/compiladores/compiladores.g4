grammar compiladores;

fragment LETRA : [A-Za-z];
fragment DIGITO : [0-9];


PYC : ';';
PA : '(';
PC : ')';
CA : '[';
CC : ']';
LLA : '{';
LLC : '}';
ASIGN : '=';
COMA : ',';
SUMA : '+';
RESTA : '-';
MULT : '*';
DIV : '/';
MOD : '%';
EQ : '==';
UEQ : '!=';
MAYOR : '>';
MENOR : '<';
INCREMENTO : '++';
DECREMENTO : '--';
AND : '&&';
OR : '||';
INT : 'int';
DOUBLE : 'double';
CHAR : 'char';
VOID : 'void';
IF : 'if';
ELSE : 'else';
WHILE : 'while';
FOR : 'for';
BREAK : 'break';
CONTINUE : 'continue';
RETURN : 'return';

NUMERO : DIGITO+ ;

CHAR_CONST : '\'' . '\'' ;

ID : (LETRA | '_')(LETRA | DIGITO | '_')* ;

WS : [ \t\n\r] -> skip ;

programa : instrucciones EOF ;

instrucciones : instruccion instrucciones
|
;

instruccion : declaracion 
|             asignacion
|             bloque
|             iif
|             iwhile
|             ifor
|             ibreak
|             icontinue
|             prototipofunc
|             declaracionfunc
|             llamadafunc
|             ireturn
;

declaracion : tipo ID inicializacion listaid PYC ;

tipo : INT
|      DOUBLE
|      CHAR
;

inicializacion : ASIGN NUMERO
|                ASIGN CHAR_CONST
|
;

listaid : COMA ID inicializacion listaid
|
;

asignacion : ID ASIGN expresion PYC ;

expresion : termino exp ;

termino : factor term ;

exp : SUMA termino exp
|     RESTA termino exp
|
;

term : MULT factor term
|      DIV factor term
|      MOD factor term
|
;

factor : NUMERO
|        CHAR_CONST
|        ID
|        PA expresion PC
;

bloque : LLA instrucciones LLC ;

iif : IF condicion bloque ielse;

ielse : ELSE bloque
|       ELSE IF condicion bloque
|
;

iwhile : WHILE condicion bloque ;

ibreak : BREAK PYC ;

icontinue : CONTINUE PYC ;

condicion : PA comparacion listacomp PC ;

comparacion : factor comp factor ;

comp : EQ
|      UEQ
|      MAYOR
|      MENOR
;

listacomp : AND comparacion listacomp 
|           OR comparacion listacomp
|
;

ifor : FOR ciclo bloque ;

ciclo : PA declaracion comparacion PYC finfor PC ;

finfor : expresion
|        ID INCREMENTO
|        ID DECREMENTO
;

prototipofunc : tipofunc ID PA idfunc PC PYC ;

declaracionfunc : tipofunc ID PA idfunc PC bloque ;

llamadafunc : ID PA factorfunc PC PYC ;

ireturn : RETURN expresion? PYC ;

tipofunc : INT
|          DOUBLE 
|          VOID
;

idfunc : tipo ID listaidfunc 
|        tipo listaidfunc
|
;

listaidfunc : COMA tipo ID listaidfunc
|             COMA tipo listaidfunc
|
;

factorfunc : NUMERO listafactfunc
|            ID listafactfunc
|            PA expresion PC listafactfunc
;

listafactfunc: COMA NUMERO listafactfunc
|              COMA ID listafactfunc
|              COMA PA expresion PC listafactfunc
|
;