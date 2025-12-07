# Estado actual del compilador (TecCompilacion2024)

Última revisión: 07/12/2025

Este archivo resume el grado de cumplimiento frente a la consigna y define los próximos pasos mínimos para cerrarla.

## Resumen rápido
- Léxico/sintaxis: gramática ANTLR4 operativa; errores léxicos/sintácticos se canalizan por listeners personalizados. No se construye AST propio, se trabaja sobre el ParseTree.
- Semántica: tabla de símbolos con ámbitos, detección de no declarados/no inicializados/doble declaración. Sistema de tipos basado en `TipoDato` (int/double/char/void) aplicado a variables y funciones; firmas de funciones almacenadas y se valida compatibilidad prototipo/definición y cantidad de argumentos en llamadas. Warnings de no usado/no inicializado siguen activos.
- Código intermedio: se generan tres direcciones para expresiones y control (if/while/for/break/continue). Llamadas a función ahora conservan todos los argumentos en la instrucción `call`.
- Optimización: propagación de constantes, constant folding, eliminación de subexpresiones comunes y eliminación de código muerto sobre temporales; iteran hasta punto fijo y se escribe `salida/codigo_optimizado.txt`.
- Backend asm: NASM x86 funcional para int/char; `double` se trata como 32-bit (sin FPU/SSE real). Funciones sin prólogo/epílogo real; llamadas asumen un argumento y retorno en eax.
- Reportes: mensajes centralizados en `Reportador` con colores ANSI (azul INFO, amarillo WARNING, rojo ERROR); tokens se vuelcan a `doc/Tokens.txt`.

## Estado por requisito de la consigna

1) Análisis léxico
- Cumplido: gramática de tokens; tabla de tokens generada (`doc/Tokens.txt`).
- Pendiente: listener léxico con mensajes coloreados y lexema problemático.

2) Análisis sintáctico
- Cumplido: parser ANTLR4, impresión de ParseTree (`toStringTree`), parseTree.png existente.
- Pendiente: AST explícito (si lo exige la cátedra) y listener sintáctico con colores; actualmente se valida `;` y `}` desde `Escucha`.

3) Análisis semántico
- Cumplido: tabla de símbolos con scopes; errores por uso no declarado/no inicializado y doble declaración; warnings por no usado/no inicializado; mensajes centralizados con colores ANSI. Sistema de tipos con `TipoDato`, prototipos/definiciones almacenan firmas y se valida compatibilidad básica y cantidad de argumentos en llamadas.
- Pendiente: inferencia y coerciones en expresiones/asignaciones/comparaciones; validar tipos de argumentos versus firma (falta conocer el tipo de cada expresión); validar `return` vs tipo declarado; eliminar parámetros implícitos que se siguen aceptando.

4) Código intermedio (3 direcciones)
- Cumplido: asignaciones, expresiones, if/while/for, break/continue, return; llamadas `call` conservan todos los argumentos; archivos `salida/codigo_intermedio.txt` y optimizado.
- Pendiente: manejo formal de valores de retorno/convención y temporales para cada argumento si se requiere backend.

5) Optimización
- Cumplido (≥3): propagación de constantes, constant folding, eliminación de subexpresiones comunes, eliminación de código muerto sobre temporales.
- Pendiente: optimizaciones de bucles (opcional) y mayor liveness si se quiere eliminar no temporales.

6) Salidas y reportes
- Cumplido: intermedio, optimizado, asm en `salida/programa.asm`, tokens en `doc/Tokens.txt`; mensajes con colores ANSI.
- Pendiente: opcional dump de mensajes a archivo.

7) Subconjunto C++
- Cubierto: tipos declarados en gramática; control `if/while/for/break/continue`; declaraciones, asignaciones, expresiones, llamadas y return. Limitación: `double` sin soporte real en backend y sin chequeo fuerte de tipos.

## Próximas tareas mínimas para cumplir la consigna
1) Tipos y funciones (crítico)
- Inferir tipos en expresiones y validar asignaciones/comparaciones contra `TipoDato`; chequear tipos de argumentos según firma (hoy solo se valida cantidad) y validar `return` vs tipo declarado. Eliminar parámetros implícitos automáticos.

2) Reportes y errores (medio)
- Listener léxico y sintáctico personalizados conectados a `Reportador` (línea/columna/lexema) y soporte opcional para volcar mensajes a archivo.

3) Backend/CI para funciones (medio)
- Backend: prólogo/epílogo simple (stack frame), pasar argumentos por pila, limpiar pila, respetar retorno en eax; al menos documentar limitaciones si no se implementa completo.

4) Tipos numéricos (medio)
- Definir política para `double`: implementar FPU/SSE básico o limitar formalmente a `int/char` y reflejarlo en la gramática/consigna.

5) AST / visualización (si lo exigen)
- Construir AST ligero y guardar DOT/PNG; o justificar uso directo de ParseTree.

6) Optimización (opcional para plus)
- Liveness completo para eliminar asignaciones no usadas en variables no temporales; alguna optimización simple de bucles (invariante fuera del loop).

## Cómo validar
- `mvn -q clean compile exec:java "-Dexec.mainClass=compiladores.App" "-Dexec.args=entrada/programa.txt"`
- Revisar `salida/codigo_intermedio.txt`, `salida/codigo_optimizado.txt`, `salida/programa.asm`, y la sección "Mensajes del compilador" para confirmar cero errores y warnings esperados.

