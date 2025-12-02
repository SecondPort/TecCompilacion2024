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

- ✅/⚠️ **Tabla de tokens**
	- Existe un archivo de prueba `entrada/tokens.txt`, lo que indica que en algún momento se generó una tabla/lista de tokens de ejemplo.
	- Sin embargo, **no hay una clase dedicada** a "tabla de tokens" ni un módulo que serialice todos los tokens leídos a un archivo estructurado (CSV/tabla) de forma sistemática.
	- Hoy se podría obtener la secuencia de tokens desde `CommonTokenStream`, pero **no hay una implementación explícita que recorra todos los tokens y escriba una tabla**.

**Qué falta para cumplir totalmente esta sección:**

- Añadir un **ErrorListener léxico** personalizado o extender el actual para:
	- Capturar `LexerNoViableAltException` y errores de canal léxico.
	- Formatear los mensajes (línea, columna, lexema problemático) con el sistema de colores (rojo para error).
- Implementar una **rutina que genere una tabla de tokens** después del análisis léxico:
	- Recorrer `CommonTokenStream`, obtener tipo, lexema, línea, columna.
	- Guardar en un archivo `doc/Tokens.txt` o similar, con formato tabular.

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
			- Si no existe, reportan "Error semantico: Uso de un identificador no declarado" con línea.
			- Si existe pero `getInicializado()==false`, reportan "Uso de un identificador no inicializado".
	- **Asignaciones** (`exitAsignacion`):
		- Verifica si el identificador asignado existe en la tabla.
		- Si no existe, reporta "identificador no declarado".
		- También revisa el `;` final (verificado como error sintáctico adicional).
		- Nota: actualmente exige que el identificador esté inicializado antes de asignar, lo cual es discutible semánticamente (ver pendientes).

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
		- La gramática no incluye todavía una instrucción `return`, por lo que tampoco se verifica el tipo de retorno.

- ⚠️ **Diferenciación entre errores y warnings**
	- El código usa un contador `errors` en `Escucha` y mensajes por `System.out.println`, pero **no hay distinción formal entre error crítico y warning**.
	- El flag `usado` en `Id` existe, pero **no se marca ni se utiliza** para advertir "variable declarada y no usada" (un caso típico de warning).
	- No hay un sistema de severidades ni una estructura de reporte unificada (p. ej. `List<Mensaje>` con tipo `ERROR`/`WARNING`).

**Qué falta para cumplir totalmente esta sección:**

- Implementar un **sistema de tipos** coherente:
	- Mapear `int`, `double`, `char`, `void` a `TipoDato`.
	- Al declarar variables y funciones, guardar `TipoDato` en vez de `String`.
	- Durante el recorrido de expresiones/asignaciones, verificar compatibilidad de tipos y reportar errores semánticos detallados.

- Completar **semántica de funciones**:
	- Durante `exitPrototipofunc` y `exitDeclaracionfunc`, analizar las reglas `idfunc`/`listaidfunc` y llenar la lista de tipos de argumentos en `Funcion`.
	- Al visitar llamadas (`exitLlamadafunc`, `exitFactorfunc`, `exitListafactfunc`), comparar número y tipo de argumentos con la firma de la función.
	- Extender la gramática con `return` y verificar que las funciones no `void` devuelvan un valor compatible.

- Ajustar la lógica de **inicialización** en asignaciones:
	- En `exitAsignacion`, en lugar de exigir que la variable ya esté inicializada, se debería **marcarla como inicializada** después de procesar la asignación correcta.
	- Mantener la verificación "uso sin inicializar" solo en contextos de lectura (como `Factor`).

- Implementar **warnings**:
	- Marcar `usado = true` cada vez que se lee una variable/función.
	- Al final de `exitPrograma`, recorrer la tabla de símbolos y emitir warnings para:
		- Variables declaradas y nunca usadas.
		- Variables declaradas y nunca inicializadas.
	- Distinguir claramente en la salida: `[ERROR] ...` vs `[WARNING] ...`.

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

- ✅ **Estructuras de control: if, while, for**
	- `visitIif` genera:
		- Un temporal para condición.
		- Instrucciones `if cond goto Ltrue`, `goto Lfalse`, `label Ltrue`, `...`, y manejo opcional de `else` con salto final a `Lend`.
	- `visitIwhile` genera bucles con etiquetas `labelStart`, `labelTrue`, `labelEnd` y saltos condicionales.
	- `visitIfor` descompone el `for` en inicialización (`declaracion`), condición (`comparacion`), actualización (`finfor`), y cuerpo (`bloque`), generando código intermedio equivalente.
	- `visitFinfor` maneja `i++`, `i--` y expresiones de actualización generales.

- ⚠️ **Llamadas a funciones y retorno de valores (en tres direcciones)**
	- La gramática soporta llamadas (`llamadafunc`, `factorfunc`, `listafactfunc`), pero **`GeneradorCodigoIntermedio` todavía no implementa métodos específicos para estas reglas**.
	- Por tanto, hoy **no se genera código intermedio para llamadas a funciones** ni para `return` (que tampoco está en la gramática).

- ⚠️ **Integración en el flujo principal**
	- Existe la clase `GeneradorCodigoIntermedio`, pero **`App.java` actualmente no la usa** (está comentado el uso de `Caminante` y no aparece una invocación directa al generador intermedio).
	- No hay escritura a archivo tipo `doc/CodigoIntermedio.txt` de la lista de instrucciones generadas.

**Qué falta para cumplir totalmente esta sección:**

- Integrar `GeneradorCodigoIntermedio` en el flujo de `App.java`:
	- Después del análisis semántico (y solo si no hay errores críticos), crear una instancia, llamar `visit(tree)` y obtener `List<Instruccion>`.
	- Volcar esa lista a un archivo, por ejemplo `doc/CodigoIntermedio.txt`.

- Completar generación para **llamadas a funciones**:
	- Implementar `visitLlamadafunc`, `visitFactorfunc`, `visitListafactfunc` en `GeneradorCodigoIntermedio`:
		- Generar instrucciones para pasar parámetros (por ejemplo, `param x` o pushes temporales).
		- Instrucción de llamada (`call f`) modelada como `Instruccion` (convención propia) y retorno en un temporal.

- (Futuro) Extender la gramática con `return` y generar las instrucciones intermedias necesarias (`return tX`).

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

- ⚠️ **Integración de optimizador en el pipeline**
	- `Optimizador` está implementado pero no parece ser invocado desde `App.java` ni desde otra clase orquestadora.
	- No se genera un archivo de código intermedio optimizado distinto del original.

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

- ⚠️ **Archivos de código intermedio y optimizado**
	- Existe `doc/CodigoIntermedio.txt`, pero no hay código que lo llene automáticamente; parece ser un archivo previsto para esta fase.
	- `GeneradorCodigoIntermedio` produce una `List<Instruccion>`, pero no hay código que la vuelque a archivo.
	- `Optimizador` devuelve otra lista, pero tampoco se escribe un archivo de salida optimizada.

- ✅ **Salida de código ensamblador**
	- `GeneradorAssembler` genera código ensamblador NASM y lo escribe a un archivo (por ejemplo `salida/programa.asm`, según README).
	- Esto supera parcialmente el requisito, ya que se tiene un backend real.

- ⚠️ **Sistema de reporte de errores/warnings con colores**
	- Actualmente los mensajes de error y debug se imprimen con `System.out.println` de texto plano.
	- No hay categorización formal (`[OK]`, `[WARNING]`, `[ERROR]`) ni colores ANSI.
	- No hay resumen consolidado (lista ordenada) de errores y warnings al final de la compilación, aunque `Escucha` lleva un contador `errors`.

**Qué falta para cumplir totalmente esta sección:**

- Implementar salida a archivo para:
	- **Código intermedio original** (por ejemplo `doc/CodigoIntermedio.txt`).
	- **Código intermedio optimizado** (por ejemplo `doc/CodigoIntermedio_Optimizado.txt`).

- Centralizar y estandarizar el **sistema de reporte**:
	- Crear una estructura `Mensaje` con campos: tipo (`ERROR`, `WARNING`, `INFO`), texto, línea, columna, fase (léxico/sintáctico/semántico/optimización).
	- Acumular todos los mensajes en una lista global o en un `Reportador` central.
	- Al final de la compilación, imprimirlos con colores ANSI (cuando se ejecute en consola que los soporte) o con prefijos claros.
	- Ejemplo de colores en consola Java (para terminal que soporte ANSI):
		- Rojo: `"\u001B[31m"`
		- Amarillo: `"\u001B[33m"`
		- Verde: `"\u001B[32m"`
		- Reset: `"\u001B[0m"`

---

## 7. Cobertura del Subconjunto de C++ solicitado

**Requisito de lenguaje:**
- Tipos de datos: `int`, `char`, `double`, `void` (para funciones).
- Estructuras de control: `if-else`, `for`, `while`, `break`, `continue`.
- Elementos: declaraciones de variables, funciones, expresiones aritméticas y lógicas, llamadas a funciones, retorno de valores, asignaciones.

**Estado actual de la gramática y semántica:**

- ✅ **Tipos `int`, `double`, `void` (para funciones)**
	- La gramática define tokens `INT`, `DOUBLE`, `VOID`.
	- Se usan en reglas `tipo` y `tipofunc`.
	- Falta soporte explícito de `char` en la gramática actual.

- ⚠️ **Tipo `char`**
	- No hay token ni regla para constantes tipo `char` (ej: `'a'`).
	- El enum `TipoDato` tampoco tiene `CHAR`.

- ✅ **Estructuras de control `if-else`, `for`, `while`**
	- Gramática tiene reglas `iif`, `ielse`, `iwhile`, `ifor`, `ciclo`, `finfor`.
	- `GeneradorCodigoIntermedio` y `GeneradorAssembler` implementan traducción de estas estructuras.

- ⚠️ **Sentencias `break` y `continue`**
	- La gramática no define tokens ni reglas para `break` o `continue`.
	- En consecuencia, no hay soporte semántico ni de generación de código para estas sentencias.

- ✅ **Declaración de variables y funciones**
	- Soportadas en gramática y en la tabla de símbolos.
	- Generación de código para variables `int`/`double` está resuelta en `GeneradorAssembler`.

- ✅/⚠️ **Expresiones aritméticas y lógicas**
	- Gramática soporta `+ - * / %`, comparaciones `== != > <`, operadores lógicos `&& ||`.
	- Código intermedio y ensamblador cubren las operaciones aritméticas y comparaciones básicas.
	- La semántica de corto circuito para `&&`/`||` no está claramente implementada en generación de ensamblador (se hace a nivel de tres direcciones).

- ⚠️ **Llamadas a funciones y retorno de valores**
	- Gramática soporta llamadas, pero:
		- El generador de código intermedio no implementa aún el recorrido para `llamadafunc`/`factorfunc`.
		- El generador de ensamblador no implementa todavía la convención de llamadas (push de parámetros, call, return), aunque el comentario de clase indica que esa es la meta.
	- No existe soporte en la gramática para `return`.

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

