# Compilador de subconjunto C++ 

## Portada
- **Título**: Compilador de subconjunto C++
- **Integrantes**: Lucas Manuel Moyano Gómez, Ignacio Jesús Olariaga Oliveto
- **Materia**: Técnicas de Compilación
- **Profesores**: Maximiliano Andrés Eschoyez, Francisco Ameri Lopez Lozano
- **Fecha**: 11/12/2025

## Introducción
Compilador académico construido con ANTLR4 (v4.13.1) y Java 17 para un subconjunto de C++ procedimental. Implementa análisis léxico, sintáctico, semántico, generación de código intermedio, optimización y backend NASM x86. Objetivos: ejercitar el pipeline completo de compilación, soportar tipos básicos (int, char, double), control de flujo y funciones con llamadas anidadas.

## Análisis del Problema (subconjunto de C++)
- Tipos: `int`, `char`, `double`, `void`.
- Estructuras: declaraciones, asignaciones, expresiones aritméticas/lógicas, `if/else`, `while`, `for`, `break`, `continue`, `return`.
- Funciones: prototipos y definiciones; llamadas con argumentos por valor; retornos `int/char/double/void`.
- Sin clases, plantillas, herencia, ni manejo de memoria dinámica. IO no contemplada; el backend solo emite asm.

## Diseño de la Solución
La solución se estructuró siguiendo el pipeline clásico de compilación, pero adaptado a las restricciones del trabajo práctico.

- **Arquitectura general**: gramática ANTLR4 → lexer/parser generados → listener semántico (`Escucha`) → visitor de CI (`GeneradorCodigoIntermedio`) → optimizador (`Optimizador`) → backend NASM (`GeneradorAssembler`) → archivos en `salida/`.
- **Fases de compilación**: léxico, sintáctico, semántico, código intermedio, optimización y generación de código (resumidas también en el anexo).

### Decisiones de diseño principales

- **Uso de ANTLR4 con listener + visitor**: se eligió ANTLR4 para concentrar el esfuerzo en las fases altas (semántica, CI, optimización, backend) en lugar de escribir a mano el parser. Se usa el patrón *listener* (`Escucha`) para el análisis semántico (más natural para construir/actualizar la tabla de símbolos a medida que se recorren reglas) y el patrón *visitor* (`GeneradorCodigoIntermedio`) para generar código de tres direcciones de forma controlada y expresiva.

- **Representación intermedia en tres direcciones**: en lugar de emitir ensamblador directamente desde el árbol, se introdujo un nivel de código intermedio con temporales (`tN`) y etiquetas (`lN`). Esto permite:
    - Simplificar la implementación de optimizaciones (propagación de constantes, folding, CSE, liveness).
    - Separar preocupaciones: primero se garantiza corrección semántica y estructural; luego se mejora el código y, recién al final, se decide cómo mapearlo a x86.

- **Backend x86 de 32 bits y x87 para `double`**: se optó por un backend NASM de 32 bits con convención cdecl simple (prólogo/epílogo basado en `ebp`, retorno en `eax` para `int/char` y en `st0` para `double`). El uso de la FPU x87 para `double` se eligió porque:
    - Simplifica la manipulación de `double` comparado con SSE en este contexto educativo.
    - Permite implementar operaciones aritméticas en punto flotante con un conjunto reducido de instrucciones (`fld`, `fstp`, `fadd`, etc.).
  Las constantes de punto flotante se ubican en `.data` y las variables en `.bss`, siguiendo la separación estándar de secciones.

- **Tipado de llamadas y separación CI/backend**: el código intermedio mantiene las llamadas con una notación de argumentos “compacta” (como string), mientras que el backend consulta la tabla de símbolos y el árbol para conocer los tipos reales de los parámetros y de retorno. Esta separación se tomó para no sobrecargar la representación intermedia con detalles de bajo nivel (tamaños en bytes, registros concretos), manteniendo:
    - El CI relativamente independiente de la arquitectura destino.
    - La lógica de tamaños y convenciones de llamada encapsulada en `GeneradorAssembler`.

- **Optimización basada en CFG y liveness completo**: se decidió implementar optimizaciones sobre el CI construyendo un grafo de flujo de control (CFG) explícito. Esto permite:
    - Aplicar propagación de constantes y folding teniendo en cuenta el orden real de ejecución.
    - Implementar eliminación de subexpresiones comunes por bloque.
    - Aplicar un análisis de vida (*liveness*) que distinga variables vivas y muertas tanto para temporales como para no temporales, reduciendo código muerto sin alterar el comportamiento del programa.

- **Subconjunto de C++ estrictamente acotado**: se restringió el lenguaje a tipos básicos (`int`, `char`, `double`, `void`) y estructuras de control esenciales (`if/else`, `while`, `for`, `break`, `continue`) más funciones y expresiones aritméticas/lógicas. Esta decisión se tomó para:
    - Mantener el foco en las fases del compilador, no en la complejidad del lenguaje completo.
    - Poder construir una implementación completa (con optimizaciones y backend) dentro de los tiempos de la materia.

- **Sistema de reporte unificado**: se centralizó la emisión de mensajes en `Reportador`, que aplica colores y niveles (info/warning/error). Esto unifica la salida de las distintas fases y facilita tanto la depuración como la experiencia de usuario en consola.

## Implementación
- **Detalles técnicos**: Java 17, Maven; ANTLR4 plugin genera lexer/parser en `target/generated-sources/antlr4/` (no editar). `Reportador` centraliza mensajes coloreados.
- **Gramática ANTLR4**: `src/main/antlr4/compiladores/compiladores.g4` define tokens (NUMERO, ID, operadores, palabras clave) y reglas (programa, instrucciones, expresiones, control, funciones, llamadas). Genera `compiladoresLexer`, `compiladoresParser`, visitors/listeners base.
- **Tabla de símbolos**: `TablaSimbolos` (singleton) con pila de contextos; guarda variables y funciones con tipo (`TipoDato`), inicialización, ámbito y firmas. `Escucha` gestiona alta/baja de contextos y chequeos (no declarado, doble declaración, no inicializado, firmas compatibles, conteo de argumentos).
- **Algoritmos por fase**:
    - Léxico/sintáctico: LL(*) de ANTLR sobre la gramática.
    - Semántico: recorridos en `Escucha` con reglas de ámbito y tipo; validaciones básicas de argumentos y retorno.
    - CI: `GeneradorCodigoIntermedio` emite tres direcciones (temporales `tN`, etiquetas `lN`) para expresiones, control y llamadas.
    - Optimización: `Optimizador` aplica propagación de constantes, constant folding, CSE intra-bloque, y eliminación de código muerto vía liveness (CFG con etiquetas/if/goto).
    - Backend: `GeneradorAssembler` visita el árbol con tipado simple (`int/char/double`), evalúa factores/expresiones, maneja llamadas, convierte int→double en retornos cuando es necesario y emite NASM.
- **Técnicas de optimización implementadas**: propagación de constantes, constant folding, eliminación de subexpresiones comunes, eliminación de código inalcanzable tras `goto` y `return`, eliminación de asignaciones redundantes del tipo `x = x;` y liveness completo para eliminar asignaciones no usadas (temporales y no temporales).

## Ejemplos y Pruebas
- **Casos**: `entrada/programa.txt` (flujo completo sin errores), `entrada/programa_errores.txt` (muestra reportes semánticos). Hay más ejemplos en `entrada/` (if/else, while, for, aritmética).
- **Salidas**: 
    - CI: `salida/codigo_intermedio.txt`.
    - CI optimizado: `salida/codigo_optimizado.txt`.
    - ASM: `salida/programa.asm`.
- **Código de prueba principal (`programa.txt`)**:
```c
int f(int x);int sumar(int a, int b);
int max_int(int a, int b);
double promedio3(int a, int b, int c);
void contar_hasta(int n);
int factorial(int n);
int prueba_break_continue(int limite);
char siguiente_char(char c);

int main() {
    int x = 5;
    int y = 10;
    int z;
    double d1 = 3.5;
    double d2 = 2.0;
    char c1 = 'A';
    char c2;

    z = x + y;
    z = z - 3 * 2;
    z = z / 3;

    d1 = d1 + d2 * 2.0;
    d1 = d1 / 2.5;

    int condicion1 = (x < y);
    int condicion2 = (z == 3);
    int condicion3 = (x > y);
    int condicion4 = (condicion1 && condicion2);
    int condicion5 = (condicion3 || condicion2);
    int condicion6 = !condicion3;

    if (condicion4) {
        z = z + 1;
    } else {
        z = z - 1;
    }

    if (condicion3) {
        z = -1;
    } else {
        z = z + 2;
    }

    int suma = sumar(x, y);
    int maximo = max_int(x, y);
    double prom = promedio3(3, 4, 5);
    int fact_5 = factorial(5);
    int resultado_bc = prueba_break_continue(10);

    int contador = 0;
    while (contador < 5) {
        contador = contador + 1;
    }

    int suma_for = 0;
    int i;
    for (i = 0; i < 5; i = i + 1) {
        suma_for = suma_for + i;
    }

    int suma_pares = 0;
    for (i = 0; i < 10; i = i + 1) {
        if (i % 2 != 0) {
            continue;
        }
        if (i > 6) {
            break;
        }
        suma_pares = suma_pares + i;
    }

    c2 = siguiente_char(c1);
    char c3 = 'z';
    char c4 = siguiente_char(c3);

    return 0;
}

int sumar(int a, int b) {
    int r = a + b;
    return r;
}

int max_int(int a, int b) {
    if (a > b) {
        return a;
    } else {
        return b;
    }
}

double promedio3(int a, int b, int c) {
    int suma = a + b + c;
    double prom = suma / 3;
    return prom;
}

void contar_hasta(int n) {
    int i = 0;
    while (i < n) {
        i = i + 1;
    }
}

int factorial(int n) {
    int res = 1;
    int i;
    for (i = 1; i <= n; i = i + 1) {
        res = res * i;
    }
    return res;
}

int prueba_break_continue(int limite) {
    int i = 0;
    int suma = 0;

    while (i < limite) {
        i = i + 1;

        if (i == 3) {
            continue;
        }

        if (i == 7) {
            break;
        }

        suma = suma + i;
    }

    return suma;
}

char siguiente_char(char c) {
    char r = c + 1;
    return r;
}
```
- **Código con errores (`programa_errores.txt`)**:
```c
int sumar(int a, int b);
int sinReturn(int x);
void g();
int h(int a);
void testBreak();
void testContinue();

int main() {
    int a = 1;
    int b;
    int a;
    x = 5;
    int c = d + 1;
    double d1 = 2.5;
    double d2 = 1;
    int e = d1;
    char ch = 'A';
    int r1 = sumar(a, d1);
    int r2 = sumar(a);
    int r3 = sinReturn(10);
    if (z > 0) {
        int y;
        y = y + 1;
    }
    for (i = 0; i < 10; i = i + 1) {
        break;
    }
    return 0.5;
}

int sumar(int a, int b) {
    return a + b;
}

int sinReturn(int x) {
    int y = x + 1;
}

void g() {
    return 1;
}

int h(int a) {
    return a;
}

double h(int a) {
    return 1.0;
}

void testBreak() {
    break;
}

void testContinue() {
    continue;
}
```
- **Análisis**: `programa.txt` compila sin errores y genera ASM coherente con operaciones int/double y control de flujo; `programa_errores.txt` reporta dobles declaraciones, no declarados, no inicializados y firmas incompatibles.

## Dificultades Encontradas y Soluciones Aplicadas
- **Integración incremental de fases**: al principio el compilador solo hacía análisis léxico/sintáctico; al agregar semántica, CI, optimización y backend aparecieron inconsistencias entre fases (por ejemplo, cambios en la gramática que rompían generación de CI o ensamblador). Se resolvió estabilizando un flujo claro de etapas y usando archivos intermedios en `salida/` para depurar cada fase por separado.
- **Manejo de `double` y x87**: el backend original solo contemplaba enteros. Añadir `double` implicó definir layout en `.data`, decidir convención de retorno (`st0`) y cuándo convertir entre `int` y `double`. Se resolvió usando instrucciones x87 (`fld`, `fstp`, operaciones aritméticas) y reglas explícitas de conversión en retornos/asignaciones.
- **Tipado de llamadas y paso de parámetros**: el CI representa llamadas con argumentos en un string, lo que generó dificultades para decidir en el backend cuántos bytes empujar y cómo tratar `double`. La solución fue que el backend recupere tipos directamente del árbol/tabla de símbolos, respetando firmas de función y tamaños (4/8 bytes).
- **Liveness y eliminación de código muerto**: la primera versión de la optimización podía eliminar asignaciones aún necesarias, especialmente sobre variables no temporales. Se rediseñó el análisis de vida construyendo un CFG basado en etiquetas/if/goto y propagando conjuntos IN/OUT hasta alcanzar un punto fijo, dejando fuera de la eliminación aquellas escrituras que todavía se usan más adelante.
- **Sincronización entre gramática y semántica**: al extender el subconjunto de C++ (por ejemplo, con `for`, `break`, `continue` y operadores lógicos), fue necesario ajustar tanto la gramática como `Escucha`, el CI y el backend. Una parte del trabajo consistió en mantener estos componentes alineados, agregando casos faltantes y corrigiendo reportes de error para que fueran consistentes.

## Conclusiones
Se completó un pipeline funcional de compilación para un subconjunto de C++: lexer/parser ANTLR, semántica con tabla de símbolos, CI, optimización y backend NASM con soporte de `double`. Quedan como mejoras posibles: tipado más estricto en llamadas/expresiones en CI, generación de temporales por argumento, y optimizaciones adicionales de bucles.

## Referencias Bibliográficas
- Aho, Lam, Sethi, Ullman. *Compilers: Principles, Techniques, and Tools*.
- Dick Grune et al. *Modern Compiler Design* (para referencia complementaria).
- Alfred V. Aho, Jeffrey D. Ullman. *Construcción de Compiladores*.

## Anexo

### Manual de Usuario

#### Requisitos e instalación
- Java 17 instalado y disponible en la variable `PATH`.
- Maven instalado (el proyecto incluye `pom.xml`).
- Clonar o descargar el repositorio y ubicarse en la carpeta raíz del proyecto.

Para compilar el proyecto y generar el lexer/parser de ANTLR4:

```powershell
mvn clean compile
```

#### Ejecución básica del compilador
Desde la raíz del proyecto, para compilar un programa de ejemplo:

```powershell
mvn -q exec:java "-Dexec.mainClass=compiladores.App" "-Dexec.args=entrada/programa.txt"
```

- El argumento en `-Dexec.args` es la ruta al archivo fuente en el subconjunto de C++.
- Puedes reemplazar `entrada/programa.txt` por cualquier otro archivo dentro de `entrada/` (por ejemplo, `entrada/programa_errores.txt`, `entrada/test_if_else.txt`, etc.).

#### Archivos de salida generados
Tras una ejecución exitosa, el compilador produce:
- `salida/codigo_intermedio.txt`: código de tres direcciones generado por `GeneradorCodigoIntermedio`.
- `salida/codigo_optimizado.txt`: versión optimizada por `Optimizador` (const-prop, folding, CSE, liveness).
- `salida/programa.asm`: código ensamblador NASM x86 generado por `GeneradorAssembler`.

Para ensamblar y enlazar el archivo ASM en un entorno Linux de 32 bits:

```bash
nasm -f elf32 salida/programa.asm -o salida/programa.o
ld -m elf_i386 salida/programa.o -o salida/programa
./salida/programa
```

#### Interpretación de mensajes, errores y warnings
El componente `Reportador` centraliza los mensajes del compilador y utiliza colores para diferenciarlos:
- Verde: mensajes de éxito o información (por ejemplo, compilación completada, rutas de salida generadas).
- Amarillo: *warnings* (situaciones no críticas pero potencialmente problemáticas, como código inalcanzable o conversiones implícitas sospechosas).
- Rojo: errores léxicos, sintácticos o semánticos que impiden continuar la compilación.

Cada mensaje incluye, cuando aplica, número de línea y columna del token correspondiente, lo que permite localizar rápidamente el problema en el archivo fuente.

#### Buenas prácticas de uso
- Editar únicamente los archivos en `src/main/antlr4` y `src/main/java`; nunca modificar archivos generados bajo `target/`.
- Tras cambiar la gramática `compiladores.g4`, volver a ejecutar `mvn clean compile` para regenerar lexer/parser.
- Mantener los programas de prueba dentro de `entrada/` para facilitar su compilación desde la línea de comandos.

### Notas técnicas ampliadas

- **Arquitectura y flujo (detalle)**:
    - Gramática ANTLR4 en `src/main/antlr4/compiladores/compiladores.g4` → genera `compiladoresLexer`/`Parser` en `target/generated-sources/antlr4/`.
    - Listener semántico `Escucha` valida ámbitos, tipos y firmas; usa `TablaSimbolos` (singleton con pila de contextos).
    - Visitor `GeneradorCodigoIntermedio` emite tres direcciones (temporales `tN`, etiquetas `lN`) hacia `salida/codigo_intermedio.txt`.
    - `Optimizador` aplica propagación de constantes, constant folding, eliminación de subexpresiones comunes, eliminación de código inalcanzable después de `goto` y `return`, eliminación de asignaciones redundantes `x = x;` y liveness para eliminar código muerto en `salida/codigo_optimizado.txt`.
    - Backend `GeneradorAssembler` produce NASM x86 con soporte `int/char/double` (x87) en `salida/programa.asm`.
- **Convenciones y guías rápidas**:
    - No editar archivos generados en `target/`.
    - Siempre balancear `addContexto()/delContexto()` al entrar/salir de bloques.
    - Tokens en MAYÚSCULAS, reglas en minúsculas; fragmentos solo para clases de caracteres.
    - Backend: cdecl simple, prólogo/epílogo con `ebp`; retorno en `eax` (int/char) o `st0` (double).
- **Ejemplo de traducción ASM (fragmento)**:
```nasm
section .bss
        x: resd 1
        y: resd 1
        z: resd 1

section .text
_start:
        mov dword [x], 5
        mov dword [y], 10
        ; z = x + y
        mov eax, [x]
        push eax
        mov eax, [y]
        pop ebx
        add eax, ebx
        mov [z], eax
    ```
    - **Resumen de las 6 fases**:
    - 1) Léxico: `compiladoresLexer` tokeniza y detecta caracteres inválidos.
    - 2) Sintáctico: `compiladoresParser` valida la estructura y construye el parse tree.
    - 3) Semántico: `Escucha` verifica tipos, ámbitos, inicialización y firmas; llena la tabla de símbolos.
    - 4) Código intermedio: `GeneradorCodigoIntermedio` produce tres direcciones (temporales/etiquetas).
    - 5) Optimización: `Optimizador` ejecuta const-prop, folding, CSE y liveness para eliminar código muerto.
    - 6) Generación de código: `GeneradorAssembler` emite NASM x86 (int/char/double con x87).
