# Estado actual del compilador (TecCompilacion2024)

Última revisión: 09/12/2025

Este archivo resume el grado de cumplimiento frente a la consigna y define los próximos pasos mínimos para cerrarla.

## Resumen rápido
- Léxico/sintaxis: gramática ANTLR4 operativa; listeners léxico y sintáctico personalizados (`LexerErrorListener`, `ParserErrorListener`) envían errores al `Reportador` con colores. No se construye AST propio, se trabaja sobre el ParseTree impreso en consola.
- Semántica: tabla de símbolos con ámbitos, detección de no declarados/no inicializados/doble declaración. Sistema de tipos `TipoDato` en variables y funciones; firmas se almacenan y se valida compatibilidad prototipo/definición y cantidad de argumentos. Persisten parámetros implícitos cuando falta declaración, no se valida tipo de argumentos ni tipo de `return`.
- Código intermedio: tres direcciones para expresiones y control (if/while/for/break/continue, return). Las llamadas `call` conservan todos los argumentos en un string (sin temporales por argumento).
- Optimización: propagación de constantes, constant folding, eliminación de subexpresiones comunes y eliminación de código muerto sobre temporales; iteran hasta punto fijo y se escribe `salida/codigo_optimizado.txt`.
- Backend asm: NASM x86 para int/char/double; convención simple con prólogo/epílogo (ebp) y paso de argumentos por pila estilo cdecl; retorno en `eax` (int/char) o `st0` (double). `double` se maneja con x87.
- Reportes: mensajes centralizados en `Reportador` con colores ANSI; tabla de tokens en `doc/Tokens.txt`.

## Estado por requisito de la consigna

1) Análisis léxico
- Cumplido: gramática de tokens; tabla de tokens generada (`doc/Tokens.txt`); listener léxico personalizado reporta lexema y usa colores vía `Reportador`.
- Pendiente: nada crítico.

2) Análisis sintáctico
- Cumplido: parser ANTLR4, impresión de ParseTree (`toStringTree`), listener sintáctico personalizado envía errores al `Reportador`; validaciones básicas de `;` y `}` en `Escucha`.
- Pendiente: AST explícito (si lo exige la cátedra).

3) Análisis semántico
- Cumplido: tabla de símbolos con scopes; errores por uso no declarado/no inicializado y doble declaración; warnings por no usado/no inicializado; mensajes centralizados con colores. Sistema de tipos con `TipoDato`; prototipos/definiciones almacenan firmas y se valida compatibilidad básica y cantidad de argumentos en llamadas.
- Pendiente: inferencia y coerciones en expresiones/asignaciones/comparaciones; validar tipos de argumentos vs firma y tipo de `return`; remover registro de parámetros implícitos cuando falta declaración.

4) Código intermedio (3 direcciones)
- Cumplido: asignaciones, expresiones, if/while/for, break/continue, return; llamadas `call` conservan todos los argumentos; archivos `salida/codigo_intermedio.txt` y optimizado.
- Pendiente: temporales/orden para argumentos si se quiere backend real; marcar valor de retorno de llamadas de forma compatible con convención.

5) Optimización
- Cumplido (≥3): propagación de constantes, constant folding, eliminación de subexpresiones comunes, eliminación de código muerto sobre temporales.
- Pendiente: optimizaciones de bucles (opcional) y mayor liveness si se quiere eliminar no temporales.

6) Salidas y reportes
- Cumplido: intermedio, optimizado, asm en `salida/programa.asm`, tokens en `doc/Tokens.txt`; mensajes con colores ANSI.
- Pendiente: opcional volcar mensajes a archivo.

7) Subconjunto C++
- Cubierto: tipos declarados en gramática; control `if/while/for/break/continue`; declaraciones, asignaciones, expresiones, llamadas y return.
- Limitaciones: soporte de `double` básico con x87; chequeo de tipos aún débil en expresiones y llamadas; CI de llamadas no separa argumentos en temporales.

## Próximas tareas mínimas para cumplir la consigna

4) Optimización / salidas (opcional)
- Liveness completo para eliminar asignaciones no usadas en variables no temporales; volcar mensajes a archivo si se requiere trazabilidad.

## Cómo validar
- `mvn -q clean compile exec:java "-Dexec.mainClass=compiladores.App" "-Dexec.args=entrada/programa.txt"`
- Revisar `salida/codigo_intermedio.txt`, `salida/codigo_optimizado.txt`, `salida/programa.asm`, y la sección "Mensajes del compilador" para confirmar cero errores y warnings esperados.

