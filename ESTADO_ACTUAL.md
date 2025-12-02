# Estado actual del compilador (TecCompilacion2024)

Este documento describe, con el mayor detalle posible, **qué funcionalidades del enunciado ya están implementadas** en el proyecto y **qué falta por hacer** para cumplir todo el requerimiento de un compilador para un subconjunto de C++.

La descripción se organiza por secciones del enunciado.

---

## 1. Análisis Léxico

**Requisito:**
- Implementar analizador léxico con ANTLR4.
- Identificar y reportar errores léxicos.
- Generar una tabla de tokens.

**Estado actual:**

- ✅ **Analizador léxico con ANTLR4**
	- Definido en `src/main/java/compiladores/compiladores.g4` (tokens como `INT`, `DOUBLE`, `VOID`, `IF`, `WHILE`, `FOR`, `ID`, `NUMERO`, operadores aritméticos y lógicos, paréntesis, llaves, etc.).
	- El `pom.xml` incluye la dependencia de ANTLR4 y el plugin `antlr4-maven-plugin` que genera automáticamente `compiladoresLexer.java`.
	- En `App.java` se crea el `compiladoresLexer` a partir de un `CharStream` asociado al archivo `entrada/programa.txt`.

- ⚠️ **Errores léxicos**
	- ANTLR, por defecto, genera errores léxicos (caracteres no reconocidos), pero **no hay un `ErrorListener` léxico propio** ni una lógica específica que clasifique y reporte errores léxicos con formato unificado (colores, tipo de error, etc.).
	- Los errores típicamente se imprimen en `stderr` con el formato estándar de ANTLR.

- ✅ **Tabla de tokens**
	- `App.java` recorre explícitamente el `CommonTokenStream` después de crear el parser y genera una tabla con **tipo de token, lexema, línea y columna**.
	- La tabla se guarda automáticamente en el archivo `doc/Tokens.txt` cada vez que se ejecuta el compilador.
	- El tipo de token se obtiene del vocabulario del parser (`getVocabulary().getSymbolicName(type)`), y los saltos de línea en lexemas se escapan para mantener el archivo legible.

**Qué falta para cumplir totalmente esta sección:**

- Añadir un **ErrorListener léxico** personalizado o extender el actual para:
	- Capturar `LexerNoViableAltException` y errores de canal léxico.
	- Formatear los mensajes (línea, columna, lexema problemático) con el sistema de colores (rojo para error).

---

## 2. Análisis Sintáctico

**Requisito:**
- Implementar analizador sintáctico con ANTLR4.
- Construir un árbol de sintaxis abstracta (AST).
- Identificar y reportar errores sintácticos.
- Visualizar el árbol sintáctico generado.

**Estado actual:**

- ✅ **Analizador sintáctico con ANTLR4**
	- La gramática `compiladores.g4` define reglas para: `programa`, `instrucciones`, `declaracion`, `asignacion`, `bloque`, `iif` (if/else), `iwhile`, `ifor`, `prototipofunc`, `declaracionfunc`, `llamadafunc`, expresiones aritméticas/lógicas, etc.
	- El plugin ANTLR genera `compiladoresParser.java` y las clases base (`compiladoresBaseListener`, `compiladoresBaseVisitor`).
	- `App.java` crea el parser, llama a la regla inicial `programa()` y obtiene un `ParseTree`.

- ✅ **Visualización del árbol sintáctico (árbol de parseo)**
	- En `App.java` se imprime el árbol con `tree.toStringTree(parser)`, lo que permite ver la estructura del parseo.
	- Además, se han generado archivos como `parseTree.png` (en el root del proyecto), lo que indica que en algún momento se visualizó el árbol con herramientas externas (por ejemplo, plugins de ANTLR para IntelliJ/VSCode o ANTLRWorks).

- ⚠️ **AST (árbol de sintaxis abstracta) vs. árbol de parseo**
	- Actualmente se trabaja con el **árbol de parseo (ParseTree)** generado por ANTLR.
	- **No hay una estructura de AST propia** (clases nodos simplificadas como `NodoExpresion`, `NodoIf`, etc.).
	- Los visitantes (`Caminante`, `GeneradorCodigoIntermedio`, `GeneradorAssembler`) trabajan directamente sobre el `ParseTree` (contextos de ANTLR), que equivale más a un árbol concreto que a un AST simplificado.

- ✅/⚠️ **Errores sintácticos**
	- ANTLR detecta errores sintácticos de manera automática.
	- Además, el listener `Escucha` implementa **chequeos sintácticos adicionales**:
		- En `exitDeclaracion` verifica que la declaración termine en `PYC` (`;`), si no, agrega un mensaje: "Error sintáctico: se esperaba ';' al final de la declaración" con número de línea.
		- En `exitAsignacion` hace lo mismo para asignaciones.
		- En `exitBloque` verifica que el último token del bloque sea `}` y, si no, reporta "Error sintáctico: se esperaba '}' al final del bloque".
	- Sin embargo, **no existe aún un `ErrorListener` sintáctico centralizado** (sólo el de ANTLR por defecto + los checks manuales en el listener).

**Qué falta para cumplir totalmente esta sección:**

- (Opcional pero recomendado) Definir un **AST propio** si el docente lo exige como requisito estricto:
	- Crear clases de nodos (`AstNodo`, `AstExpresionBinaria`, `AstAsignacion`, `AstIf`, `AstWhile`, etc.).
	- Añadir un `Visitor` que recorra el `ParseTree` y construya este AST.
	- Ajustar la generación de código intermedio y ensamblador para usar este AST en lugar del `ParseTree` directo.
- Implementar un **ErrorListener sintáctico** personalizado:
	- Registrar el listener en `parser.removeErrorListeners()` + `parser.addErrorListener(...)`.
	- Recolectar errores sintácticos en una lista común (para mostrarlos ordenados al final) y con sistema de colores.

---

## 3. Análisis Semántico

**Requisito:**
- Implementar analizador semántico.
- Construir y mantener tabla de símbolos.
- Verificar tipos de datos y compatibilidad en operaciones.
- Verificar ámbito de variables y funciones.
- Reportar errores semánticos con detalle.
- Distinguir entre errores (críticos) y warnings (no críticos).

**Estado actual:**

- ✅ **Tabla de símbolos con ámbitos** (`TablaSimbolos.java`)
	- Implementa un singleton (`getInstancia()`), con una **pila de contextos**: `List<Map<String, Id>> ts`.
	- Métodos clave:
		- `addContexto()` / `delContexto()` → manejan la creación/eliminación de ámbitos (por ejemplo: programa global, bloques `{}` y/o funciones).
		- `addSimbolo(String nombre, Id id)` → inserta un símbolo en el contexto actual si no existe.
		- `getSimbolo(String nombre)` → búsqueda desde el ámbito más interno hacia afuera.
		- `contieneSimbolo(String nombre)` / `contieneSimboloLocal(String nombre)` → búsqueda global vs. local.
	- Clases asociadas:
		- `abstract class Id` con `nombre`, `tipoDato`, `inicializado`, `usado`.
		- `class Variable extends Id` y `class Funcion extends Id` (esta última tiene un campo `List<TipoDato> argumentos;` aún no usado).
		- `enum TipoDato { VOID, INT, DOUBLE }` (no está plenamente integrado con la gramática todavía, ya que se usan `String` directamente para el tipo).

- ✅ **Integración de tabla de símbolos con el listener** (`Escucha.java`)
	- En `enterPrograma` y `enterBloque` se hace `tabla.addContexto()`.
	- En `exitPrograma` y `exitBloque` se hace `tabla.delContexto()`, garantizando el correcto manejo de ámbitos anidados.

- ✅ **Chequeos semánticos ya implementados**
	- **Declaraciones de variables** (`exitDeclaracion`):
		- Detecta **doble declaración en el mismo ámbito** usando `contieneSimboloLocal(nombre)`.
		- Al declarar, crea un `Variable`, asigna `tipo` tomando el primer hijo de la regla (`int`/`double`), marca `inicializado` según haya o no inicialización y `usado = false`.
		- Registra la variable en la tabla de símbolos.
		- Guarda mensajes de error en una lista `erroresAcumulados` y los imprime (con línea).
	- **Declaraciones y prototipos de función** (`exitPrototipofunc`, `exitDeclaracionfunc`):
		- Verifican doble declaración de función.
		- Crean un objeto `Funcion` con `nombre`, `tipoDato` y `usado=false`.
		- Agregan la función a la tabla de símbolos.
	- **Uso de identificadores en expresiones y otros contextos**:
		- `exitFactor`, `exitFinfor`, `exitFactorfunc`, `exitListafactfunc`, `exitLlamadafunc`:
			- Si hay `ID()`, buscan en la tabla de símbolos.
			- Si no existe, registran un **error semántico**: uso de identificador no declarado.
			- Si existe pero `getInicializado()==false`, registran un **error semántico**: uso de identificador no inicializado.
			- Cuando el identificador existe e inicialmente está correctamente declarado e inicializado, se marca `usado = true` para poder generar warnings posteriores.
	- **Asignaciones** (`exitAsignacion`):
		- Verifica si el identificador asignado existe en la tabla; si no existe, registra error.
		- Marca la variable como `inicializado = true` cuando la asignación es válida.
		- También revisa el `;` final (verificado como error sintáctico adicional).

- ⚠️ **Verificación de tipos y compatibilidad de operaciones**
	- Aunque la gramática distingue tipos `int`, `double`, `void`, y existe el enum `TipoDato`, **no hay una verificación sistemática de tipos**:
		- No se evalúa si `int + double` está permitido y a qué tipo se promociona.
		- No se valida que el lado derecho de una asignación tenga un tipo compatible con la variable del lado izquierdo.
		- No se revisan los tipos en comparaciones (`<`, `==`, etc.).
	- El campo `tipoDato` en `Id` se maneja como `String`, sin lógica de coerción o chequeo fuerte de tipos.


- ⚠️ **Verificación semántica de funciones (parámetros, retorno)**
	- La gramática soporta:
		- Prototipos (`prototipofunc`).
		- Declaraciones de funciones (`declaracionfunc`).
		- Listas de parámetros (`idfunc`, `listaidfunc`).
		- Llamadas (`llamadafunc`, `factorfunc`, `listafactfunc`).
	- Sin embargo, en la implementación actual:
		- La clase `Funcion` no llena ni usa la lista de `argumentos`.
		- No se verifica el número ni tipo de parámetros en llamadas a funciones.
		- Aunque la gramática ya incluye la instrucción `return` y se genera código intermedio para ella, todavía no se verifica semánticamente que el tipo del valor devuelto coincida con el tipo declarado de la función ni que todas las rutas de una función no-`void` devuelvan un valor.

- ✅ **Diferenciación entre errores y warnings, y warnings de uso/inicialización**
	- Se implementó una infraestructura de mensajes centralizada basada en dos clases nuevas:
		- `Mensaje`: encapsula tipo (`INFO`, `WARNING`, `ERROR`), texto, línea y columna.
		- `Reportador`: singleton que acumula todos los mensajes generados por el compilador y permite imprimirlos al final.
	- `Escucha` ya no usa `System.out.println` para reportar errores o información del análisis; en su lugar llama a `Reportador.info(...)`, `Reportador.warning(...)` o `Reportador.error(...)`.
	- En `exitPrograma`, después de cerrar el ámbito global, se recorre toda la tabla de símbolos y se generan **warnings automáticos** para:
		- Identificadores declarados pero **no usados** (`usado == false`).
		- Identificadores declarados pero **no inicializados** (`inicializado == false`).
	- El listener también registra mensajes informativos al inicio y al final de la compilación (por ejemplo, conteo de nodos y tokens visitados).

**Qué falta para cumplir totalmente esta sección:**

- Implementar un **sistema de tipos** coherente:
	- Mapear `int`, `double`, `char`, `void` a `TipoDato`.
	- Al declarar variables y funciones, guardar `TipoDato` en vez de `String`.
	- Durante el recorrido de expresiones/asignaciones, verificar compatibilidad de tipos y reportar errores semánticos detallados.

- Completar **semántica de funciones**:
	- Durante `exitPrototipofunc` y `exitDeclaracionfunc`, analizar las reglas `idfunc`/`listaidfunc` y llenar la lista de tipos de argumentos en `Funcion`.
	- Al visitar llamadas (`exitLlamadafunc`, `exitFactorfunc`, `exitListafactfunc`), comparar número y tipo de argumentos con la firma de la función.
	- Extender la gramática con `return` y verificar que las funciones no `void` devuelvan un valor compatible.

- Completar el **sistema de tipos** y la **semántica de funciones** descritos arriba (tipos de retorno, parámetros, compatibilidad de tipos), usando el sistema de mensajes ya unificado para reportar tanto errores como warnings adicionales.

---

## 4. Generación de Código Intermedio

**Requisito:**
- Implementar generador de código de tres direcciones.
- Manejar expresiones aritméticas y lógicas.
- Manejar estructuras de control (if-else, bucles).
- Manejar llamadas a funciones y retorno de valores.

**Estado actual:**

- ✅ **Infraestructura de código intermedio**
	- Clase `Instruccion` representa una instrucción de tres direcciones con campos `op`, `arg1`, `arg2`, `result` y un `toString()` que formatea:
		- Asignaciones `result = arg1` (`op = "="`).
		- Operaciones binarias `result = arg1 op arg2`.
		- Saltos incondicionales (`goto label`).
		- Saltos condicionales (`if cond goto label`).
		- Etiquetas (`label:`).
	- Clase `GeneradorCodigoIntermedio` extiende `compiladoresBaseVisitor<String>` y recorre el `ParseTree` para producir una lista `List<Instruccion>`.

- ✅ **Expresiones aritméticas**
	- `visitAsignacion` genera código intermedio tipo:
		- `t0 = ...` (resultado de expresión),
		- `x = t0` para la asignación final.
	- `visitExpresion`, `visitTermino`, `visitFactor`, y métodos auxiliares `processExp`, `processTerm` generan instrucciones:
		- `tN = left + right`, `tN = left - right`, etc., usando temporales `t0`, `t1`, ...

- ✅ **Condiciones y expresiones lógicas**
	- `visitComparacion` genera instrucciones como `tX = left < right`, `tX = left == right`, etc.
	- `visitCondicion` y `processListaComp` generan código con operadores lógicos `&&`, `||` combinando resultados de comparaciones.

- ✅ **Estructuras de control: if, while, for, break, continue**
	- `visitIif` genera:
		- Un temporal para condición.
		- Instrucciones `if cond goto Ltrue`, `goto Lfalse`, `label Ltrue`, `...`, y manejo opcional de `else` con salto final a `Lend`.
	- `visitIwhile` genera bucles con etiquetas `labelStart`, `labelTrue`, `labelEnd` y saltos condicionales. Ahora mantiene pilas de etiquetas de bucle para soportar `break` y `continue` anidados.
	- `visitIfor` descompone el `for` en inicialización (`declaracion`), condición (`comparacion`), actualización (`finfor`), y cuerpo (`bloque`), generando código intermedio equivalente. También registra etiquetas de inicio y fin de bucle para `break`/`continue`.
	- `visitIbreak` y `visitIcontinue` emiten instrucciones `goto` hacia la etiqueta de fin de bucle actual (`break`) o de inicio/continuación (`continue`), usando pilas para manejar bucles anidados.
	- `visitFinfor` maneja `i++`, `i--` y expresiones de actualización generales.

- ✅/⚠️ **Llamadas a funciones y retorno de valores (en tres direcciones)**
	- La gramática soporta llamadas (`llamadafunc`, `factorfunc`, `listafactfunc`) y la instrucción `return` (`ireturn`).
	- `GeneradorCodigoIntermedio` implementa métodos específicos:
		- `visitLlamadafunc`/`visitFactorfunc`/`visitListafactfunc` generan una instrucción de tres direcciones tipo `call nombre, arg, tResultado` (modelo simplificado de uno o pocos argumentos).
		- `visitIreturn` genera una instrucción `return valor` (o `return` sin valor) que modela la salida de una función.
	- Limitación actual: el modelo de argumentos es simplificado (no hay una representación estructurada de múltiples parámetros) y todavía no se enlazan directamente estas instrucciones con una semántica de tipos de retorno.

- ⚠️ **Integración en el flujo principal**
	- Existe la clase `GeneradorCodigoIntermedio`, pero **`App.java` actualmente no la usa** (está comentado el uso de `Caminante` y no aparece una invocación directa al generador intermedio).
	- No hay escritura a archivo tipo `doc/CodigoIntermedio.txt` de la lista de instrucciones generadas.

**Qué falta para cumplir totalmente esta sección:**

- Refinar la generación para **llamadas a funciones**:
	- Definir una convención más explícita para múltiples argumentos (por ejemplo, secuencia de `param` o pushes individuales) y reflejarla en las instrucciones intermedias.
	- Relacionar las instrucciones `call`/`return` con información de tipos para facilitar comprobaciones posteriores.

- (Futuro) Integrar estas instrucciones con un sistema de tipos más fuerte para validar coherencia entre firmas de funciones y usos.

---

## 5. Optimización de Código

**Requisito:**
- Implementar al menos tres técnicas de optimización:
	- Propagación de constantes.
	- Eliminación de código muerto.
	- Simplificación de expresiones.
	- Eliminación de subexpresiones comunes.
	- Optimización de bucles.

**Estado actual:**

- ✅ **Clase de optimización implementada** (`Optimizador.java`)
	- Recibe `List<Instruccion>` y devuelve una nueva lista optimizada.
	- Trabaja por iteraciones hasta que no hay más cambios (`while (cambios) { ... }`).
	- Maneja resets de análisis en fronteras de bloques básicos (labels, `if`, `goto`).

- ✅ **Propagación de constantes**
	- Usa un mapa `constantes` (variable → valor constante) para reemplazar argumentos cuando se sabe que son constantes.
	- Cuando encuentra una instrucción `result = cte` (`op = "="` y `arg1` numérico), agrega `result` a `constantes`.
	- Cuando una variable cambia de valor no constante, la elimina de `constantes`.

- ✅ **Simplificación de expresiones (constant folding)**
	- Cuando ambos argumentos son numéricos o uno solo para unaria, utiliza `calcular(op, arg1, arg2)`:
		- Opera `+`, `-`, `*`, `/`, `%`, comparaciones (`<`, `>`, `==`, `!=`).
		- Reemplaza la instrucción por una asignación directa `result = valorConstante` (`op = "="`, `arg1 = valor`).

- ✅ **Eliminación de subexpresiones comunes**
	- Usa un mapa `expresiones` donde la llave es `"op,arg1,arg2"` y el valor es el temporal que ya contiene ese resultado.
	- Si se vuelve a encontrar la misma operación con los mismos operandos, reemplaza por una asignación desde el resultado existente.

- ⚠️ **Eliminación de código muerto**
	- Actualmente **no hay un análisis de "liveness"** que detecte temporales o variables cuyo resultado nunca se usa.
	- No se hace barrido posterior para eliminar instrucciones cuyas `result` no son leídas más adelante.

- ⚠️ **Optimización de bucles**
	- No hay optimizaciones específicas de bucles aún (como mover cómputos invariantes fuera del loop, "strength reduction", etc.).

**Integración de optimizador en el pipeline**
	- `Optimizador` está integrado en `App.java`:
		- Tras generar el código intermedio, se llama a `optimizador.optimizar(instrucciones)`.
		- El resultado se escribe en `salida/codigo_optimizado.txt`, separado del código intermedio original.

**Qué falta para cumplir totalmente esta sección:**

- **Integrar el optimizador en el flujo**:
	- Tras generar la lista de instrucciones de `GeneradorCodigoIntermedio`, pasarla por `Optimizador.optimizar(...)`.
	- Escribir el resultado optimizado en un archivo separado, por ejemplo `doc/CodigoIntermedio_Optimizado.txt`.

- (Opcional pero alineado al enunciado) Implementar **eliminación de código muerto**:
	- Análisis hacia atrás para detectar resultados que nunca afectan la salida.
	- Eliminar instrucciones cuya `result` nunca es usada.

- (Opcional) Añadir alguna **optimización de bucles** básica si se quiere cumplir explícitamente ese punto del enunciado.

Con las técnicas ya implementadas (propagación de constantes, constant folding, subexpresiones comunes), **ya se cumplen al menos tres optimizaciones**, pero falta integrarlas y documentarlas como parte estándar de la compilación.

---

## 6. Salidas del Compilador

**Requisito:**
- Generar archivos de salida para código intermedio y optimizado.
- Implementar sistema de reporte de errores y warnings con colores (verde éxito, amarillo warnings, rojo errores).

**Estado actual:**

- ✅ **Archivos de código intermedio y optimizado**
	- `GeneradorCodigoIntermedio` produce una `List<Instruccion>` que se vuelca automáticamente desde `App.java` al archivo `salida/codigo_intermedio.txt`.
	- `Optimizador` devuelve otra lista que se escribe en `salida/codigo_optimizado.txt`, representando el código intermedio optimizado.
	- El archivo `doc/CodigoIntermedio.txt` puede considerarse histórico o auxiliar; la salida estándar actualmente se ubica bajo `salida/`.

- ✅ **Salida de código ensamblador**
	- `GeneradorAssembler` genera código ensamblador NASM y lo escribe a un archivo (por ejemplo `salida/programa.asm`, según README).
	- Esto supera parcialmente el requisito, ya que se tiene un backend real.

- ✅/⚠️ **Sistema de reporte de errores/warnings**
	- Se implementó un **sistema centralizado de mensajes** basado en `Mensaje` y `Reportador`.
	- Durante el análisis (principalmente en `Escucha`), todos los errores, warnings e información se registran en el `Reportador` en lugar de imprimirse directamente en consola.
	- Al final de la ejecución de `App`, se imprime una sección "Mensajes del compilador" que lista ordenadamente todos los mensajes acumulados.
	- Aún no se aplican colores ANSI en consola, pero sí existe una categorización explícita por tipo (`INFO`, `WARNING`, `ERROR`) y un lugar único donde extender el formato de salida.

**Qué falta para cumplir totalmente esta sección:**

- (Opcional) Implementar salida adicional a archivo para:
	- **Tabla de mensajes** (por ejemplo `doc/Mensajes.txt`), reutilizando el `Reportador`.

- Extender el sistema de reporte para **incluir colores ANSI** en consola:
	- Asociar colores a cada tipo de mensaje (verde para éxito/INFO relevante, amarillo para `WARNING`, rojo para `ERROR`).
	- Mantener la misma estructura centralizada para que el cambio afecte a todas las fases del compilador.

---

## 7. Cobertura del Subconjunto de C++ solicitado

**Requisito de lenguaje:**
- Tipos de datos: `int`, `char`, `double`, `void` (para funciones).
- Estructuras de control: `if-else`, `for`, `while`, `break`, `continue`.
- Elementos: declaraciones de variables, funciones, expresiones aritméticas y lógicas, llamadas a funciones, retorno de valores, asignaciones.

**Estado actual de la gramática y semántica:**

- ✅ **Tipos `int`, `double`, `char`, `void` (para funciones)**
	- La gramática define tokens `INT`, `DOUBLE`, `CHAR`, `VOID`.
	- Se usan en reglas `tipo` y `tipofunc`.
	- El enum `TipoDato` en `TablaSimbolos` ahora incluye `CHAR`.
	- Se admiten literales de carácter mediante `CHAR_CONST` (por ejemplo `'a'`).

- ✅ **Estructuras de control `if-else`, `for`, `while`**
	- Gramática tiene reglas `iif`, `ielse`, `iwhile`, `ifor`, `ciclo`, `finfor`.
	- `GeneradorCodigoIntermedio` y `GeneradorAssembler` implementan traducción de estas estructuras.

- ✅ **Sentencias `break` y `continue`**
	- La gramática define tokens y reglas `ibreak` e `icontinue` para `break;` y `continue;`.
	- El generador de código intermedio mantiene pilas de etiquetas por bucle y traduce `break`/`continue` a saltos hacia la etiqueta de fin o de inicio del bucle actual.
	- El generador de ensamblador hace lo mismo a nivel NASM, generando instrucciones `jmp` a las etiquetas correctas para `while` y `for`.

- ✅ **Declaración de variables y funciones**
	- Soportadas en gramática y en la tabla de símbolos.
	- Generación de código para variables `int`/`double` está resuelta en `GeneradorAssembler`.

- ✅/⚠️ **Expresiones aritméticas y lógicas (incluyendo `char`)**
	- Gramática soporta `+ - * / %`, comparaciones `== != > <`, operadores lógicos `&& ||`.
	- Código intermedio y ensamblador cubren las operaciones aritméticas y comparaciones básicas.
	- Los literales `char` se manejan como enteros (códigos ASCII) tanto en código intermedio (como lexema de `CHAR_CONST`) como en ensamblador (carga del ASCII en `EAX` y almacenamiento en variables `char`).
	- La semántica de corto circuito para `&&`/`||` no está claramente implementada en generación de ensamblador (se hace a nivel de tres direcciones).

- ⚠️ **Llamadas a funciones y retorno de valores**
	- Gramática soporta llamadas y ya incluye la instrucción `return`.
	- El generador de código intermedio ahora implementa:
		- Recorrido para `llamadafunc`/`factorfunc`/`listafactfunc`, generando instrucciones tipo `call nombre, arg, tResultado` (modelo simplificado de argumentos).
		- Instrucciones `return valor` (o `return` sin valor) a partir de `ireturn`.
	- El generador de ensamblador implementa una convención de llamadas básica:
		- `declaracionfunc` genera una etiqueta por función, emite su cuerpo y finaliza con `mov eax, 0` y `ret` si no hay `return` explícito.
		- `llamadafunc` evalúa un argumento (si existe), lo pasa por pila (`push eax`), ejecuta `call nombre` y luego limpia la pila (`add esp, 4`), asumiendo el valor de retorno en `eax`.
	- Limitaciones actuales:
		- Modelo simplificado: solo se maneja de forma explícita un argumento, no hay manejo general de múltiples parámetros ni prólogo/epílogo completo con `ebp`/variables locales.

---

## 8. Resumen global del estado actual

- El proyecto ya tiene:
	- ✔ Un **léxico y parser ANTLR4** bien definidos para un lenguaje tipo C.
	- ✔ Un **listener semántico** (`Escucha`) con tabla de símbolos y varios chequeos importantes (declaración, ámbito, uso sin declarar, uso sin inicializar, doble declaración).
	- ✔ Un **generador de código intermedio** basado en instrucciones de tres direcciones, que cubre expresiones, condiciones y estructuras de control básicas.
	- ✔ Un **optimizador** con tres técnicas claras: propagación de constantes, simplificación de expresiones (folding) y eliminación de subexpresiones comunes.
	- ✔ Un **generador de código ensamblador NASM** funcional para int/double, if/while/for.

- Lo que falta para alinear completamente con el enunciado:
	- Integrar de forma ordenada el **pipeline completo** en `App.java`:
		1) Análisis léxico + tabla de tokens.
		2) Análisis sintáctico + árbol (y opcionalmente AST).
		3) Análisis semántico (tipos, funciones, ámbitos, warnings).
		4) Generación de código intermedio.
		5) Optimización de código intermedio.
		6) Generación de ensamblador (opcional pero ya existente).
	- Implementar un **sistema unificado de reporte de errores y warnings con colores**.
	- Completar soporte del subconjunto de C++ pedido:
		- Tipo `char`.
		- `break` y `continue`.
		- `return` y manejo completo de funciones.
	- Escribir sistemáticamente:
		- Un archivo con la **tabla de tokens**.
		- Un archivo con **código intermedio**.
		- Un archivo con **código intermedio optimizado**.

Este documento refleja el estado actual a la fecha indicada y sirve como guía de trabajo para las siguientes iteraciones del proyecto.

