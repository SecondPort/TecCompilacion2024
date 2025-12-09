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
MAYORIGUAL : '>=';
MENORIGUAL : '<=';
INCREMENTO : '++';
DECREMENTO : '--';
AND : '&&';
OR : '||';
INT : 'int';
DOUBLE : 'double';
CHAR : 'char';
BOOL : 'bool';
TRUE : 'true';
FALSE : 'false';
VOID : 'void';
IF : 'if';
ELSE : 'else';
WHILE : 'while';
FOR : 'for';
BREAK : 'break';
CONTINUE : 'continue';
RETURN : 'return';

NUMERO : DIGITO+ ('.' DIGITO+)? ;

CHAR_CONST : '\'' . '\'' ;

ID : (LETRA | '_')(LETRA | DIGITO | '_')* ;

LINE_COMMENT : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
WS : [ \t\n\r]+ -> skip ;

// Un programa al estilo C: solo declaraciones/prototipos/definiciones a nivel global.
programa : toplevel* EOF ;

toplevel : declaracion      // variables globales
		 | prototipofunc    // prototipos globales
		 | declaracionfunc  // definiciones de función (solo globales)
		 ;

// Instrucciones permitidas dentro de funciones/bloques (no se permiten funciones anidadas).
instrucciones : instruccion* ;

instruccion : declaracion 
			| asignacion
			| bloque
			| iif
			| iwhile
			| ifor
			| ibreak
			| icontinue
			| llamadafunc
			| ireturn
			;

declaracion : tipo ID dimension? inicializacion listaid PYC ;

tipo : INT
|      DOUBLE
|      CHAR
|      BOOL
;

inicializacion : ASIGN expresion
|
;

listaid : COMA ID dimension? inicializacion listaid
|
;

asignacion : ID dimensionAcceso? ASIGN expresion PYC ;

expresion
	: expresion SUMA expresion
	| expresion RESTA expresion
	| expresion MULT expresion
	| expresion DIV expresion
	| expresion MOD expresion
	| expresion EQ expresion
	| expresion UEQ expresion
	| expresion MAYOR expresion
	| expresion MENOR expresion
	| expresion MAYORIGUAL expresion
	| expresion MENORIGUAL expresion
	| expresion AND expresion
	| expresion OR expresion
	| RESTA expresion           // unario -
	| '!' expresion            // unario !
	| factor
	;

factor
	: NUMERO
	| CHAR_CONST
	| TRUE
	| FALSE
	| ID
	| ID CA expresion CC
	| PA expresion PC
	| llamada_expr
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

condicion : PA expresion PC ;

ifor : FOR ciclo bloque ;

// Permite tanto una declaración como una asignación inicial en el for
// y una expresión general (estilo C) en la parte central
ciclo : PA (declaracion | asignacion) expresion PYC finfor PC ;

finfor : expresion
|        ID INCREMENTO
|        ID DECREMENTO
|        ID ASIGN expresion
;

prototipofunc : tipofunc ID PA idfunc PC PYC ;

declaracionfunc : tipofunc ID PA idfunc PC bloque ;

// Llamada usada como sentencia (termina en ';')
llamadafunc : llamada_expr PYC ;

// Llamada usada como expresión/factor (sin ';')
llamada_expr : ID PA factorfunc PC ;

ireturn : RETURN expresion? PYC ;

tipofunc : INT
|          DOUBLE 
|          CHAR
|          BOOL
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

dimension : CA NUMERO CC ;
dimensionAcceso : CA expresion CC ;

listafactfunc: COMA NUMERO listafactfunc
|              COMA ID listafactfunc
|              COMA PA expresion PC listafactfunc
|
;