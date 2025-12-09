# Compilador de subconjunto C++ (TecCompilacion2024)

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
- **Arquitectura general**: gramática ANTLR4 → lexer/parser generados → listener semántico (`Escucha`) → visitor de CI (`GeneradorCodigoIntermedio`) → optimizador (`Optimizador`) → backend NASM (`GeneradorAssembler`) → archivos en `salida/`.
- **Fases de compilación**: léxico, sintáctico, semántico, CI, optimización, backend (ver resumen más abajo).
- **Decisiones de diseño**:
    - Backend x86 32 bits con convención cdecl simple (prólogo/epílogo `ebp`), retorno en `eax` o `st0` (double vía x87).
    - Soporte de `double` implementado con x87; constantes en `.data`, variables en `.bss`.
    - CI de llamadas empaqueta argumentos en string; el backend evalúa tipos en el árbol (no desde CI).
    - Optimizador con liveness completo para eliminar asignaciones muertas (incluye no temporales), además de const-prop, folding y CSE.

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
- **Técnicas de optimización implementadas**: propagación de constantes, constant folding, eliminación de subexpresiones comunes, liveness completo para eliminar asignaciones no usadas (temporales y no temporales).

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
- Manejo de `double`: se resolvió con x87, constantes en `.data` y conversión int→double en retornos/asignaciones.
- Tipado de llamadas: el CI conserva args como string; el backend evalúa tipos desde el árbol para empujar 4/8 bytes.
- Liveness: se implementó un CFG con etiquetas/if/goto para evitar eliminar asignaciones necesarias en no temporales.

## Conclusiones
Se completó un pipeline funcional de compilación para un subconjunto de C++: lexer/parser ANTLR, semántica con tabla de símbolos, CI, optimización y backend NASM con soporte de `double`. Quedan como mejoras posibles: tipado más estricto en llamadas/expresiones en CI, generación de temporales por argumento, y optimizaciones adicionales de bucles.

## Referencias Bibliográficas
- Aho, Lam, Sethi, Ullman. *Compilers: Principles, Techniques, and Tools*.
- Dick Grune et al. *Modern Compiler Design* (para referencia complementaria).
- Alfred V. Aho, Jeffrey D. Ullman. *Construcción de Compiladores*.

## Anexo (notas y comandos útiles)
- **Instrucciones de uso**:
    - Compilar y regenerar parser: `mvn clean compile`.
    - Ejecutar compilador con entrada por defecto: `mvn -q exec:java "-Dexec.mainClass=compiladores.App" "-Dexec.args=entrada/programa.txt"`.
    - Cambiar archivo de entrada: sustituir la ruta en `-Dexec.args` por cualquier archivo en `entrada/`.
    - Ensamblar/ejecutar en Linux: `nasm -f elf32 salida/programa.asm -o salida/programa.o && ld -m elf_i386 salida/programa.o -o salida/programa`.
- **Arquitectura y flujo (detalle)**:
    - Gramática ANTLR4 en `src/main/antlr4/compiladores/compiladores.g4` → genera `compiladoresLexer`/`Parser` en `target/generated-sources/antlr4/`.
    - Listener semántico `Escucha` valida ámbitos, tipos y firmas; usa `TablaSimbolos` (singleton con pila de contextos).
    - Visitor `GeneradorCodigoIntermedio` emite tres direcciones (temporales `tN`, etiquetas `lN`) hacia `salida/codigo_intermedio.txt`.
    - `Optimizador` aplica const-prop, folding, CSE y liveness para `salida/codigo_optimizado.txt`.
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
