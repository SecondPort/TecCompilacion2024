# Guía de Generación de Código Ensamblador

## Descripción General

El compilador TecCompilacion2024 ahora incluye un **generador de código ensamblador** que traduce código fuente en lenguaje similar a C a código ensamblador NASM (sintaxis x86 de 32 bits).

## Características Implementadas

### ✅ Operaciones Soportadas

#### 1. Declaraciones de Variables
```c
int x = 10;      // Declaración con inicialización
int y;           // Declaración sin inicialización
double z = 3.14; // Variables de tipo double
```

**Código generado:**
```nasm
section .bss
    x: resd 1  ; int
    y: resd 1  ; int
    z: resd 1  ; double

section .text
    mov dword [x], 10
```

#### 2. Asignaciones
```c
x = 5;
y = x + 10;
```

**Código generado:**
```nasm
    mov eax, 5
    mov [x], eax
    
    mov eax, [x]
    push eax
    mov eax, 10
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [y], eax
```

#### 3. Expresiones Aritméticas

**Operadores soportados:**
- Suma: `+`
- Resta: `-`
- Multiplicación: `*`
- División: `/`
- Módulo: `%`

**Precedencia correcta:**
```c
resultado = a + b * c;  // Multiplica b*c primero, luego suma a
```

**Código generado (respeta precedencia):**
```nasm
    mov eax, [a]
    push eax
    mov eax, [b]
    push eax
    mov eax, [c]
    mov ebx, eax
    pop eax
    imul eax, ebx  ; b * c
    mov ebx, eax
    pop eax
    add eax, ebx   ; a + (b*c)
    mov [resultado], eax
```

#### 4. Estructuras de Control

##### IF-ELSE
```c
if (x > 5) {
    y = 10;
} else {
    y = 20;
}
```

**Código generado:**
```nasm
    ; Estructura IF
    mov eax, [x]
    push eax
    mov eax, 5
    mov ebx, eax
    pop eax
    cmp eax, ebx
    je L0      ; saltar si falso
    mov eax, 10
    mov [y], eax
    jmp L1     ; saltar al final
L0:
    mov eax, 20
    mov [y], eax
L1:
```

##### WHILE
```c
while (contador < 10) {
    contador = contador + 1;
}
```

**Código generado:**
```nasm
    ; Estructura WHILE
L2:
    mov eax, [contador]
    push eax
    mov eax, 10
    mov ebx, eax
    pop eax
    cmp eax, ebx
    je L3          ; salir si falso
    
    ; cuerpo del bucle
    mov eax, [contador]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [contador], eax
    
    jmp L2         ; repetir bucle
L3:
```

##### FOR
```c
for (int i = 0; i < 10; i++) {
    suma = suma + i;
}
```

**Código generado:**
```nasm
    ; Estructura FOR
    mov dword [i], 0
    mov eax, [i]
    push eax
    mov eax, 10
    mov ebx, eax
    pop eax
    cmp eax, ebx
L4:
    je L5          ; salir si falso
    
    ; cuerpo del for
    mov eax, [suma]
    push eax
    mov eax, [i]
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [suma], eax
    
    jmp L4         ; repetir bucle
L5:
```

#### 5. Comparaciones

**Operadores soportados:**
- Igual: `==`
- Diferente: `!=`
- Mayor que: `>`
- Menor que: `<`

**Nota:** Los operadores lógicos `&&` y `||` están definidos en la gramática pero su generación de código está pendiente de implementación completa.

## Arquitectura de Generación

### Estrategia de Compilación

1. **Almacenamiento de Variables**
   - Todas las variables se declaran en la sección `.bss`
   - Cada variable int/double ocupa 4 bytes (`resd 1`)

2. **Evaluación de Expresiones**
   - Se usa el registro `EAX` como acumulador principal
   - Se usa el registro `EBX` como acumulador secundario
   - La pila se usa para preservar valores temporales en operaciones complejas

3. **Flujo de Control**
   - Cada estructura genera etiquetas únicas (L0, L1, L2, ...)
   - Se usan saltos condicionales basados en flags del procesador
   - La instrucción `cmp` establece los flags para comparaciones

4. **Convenciones x86**
   - Sintaxis Intel (destino primero): `mov destino, origen`
   - Syscall de salida: `int 0x80` con `eax=1` (exit)

## Uso del Generador

### 1. Compilar el Proyecto
```bash
mvn clean compile
```

### 2. Crear/Editar Archivo de Entrada
Edita el archivo `entrada/programa.txt` con tu código fuente.

Ejemplo:
```c
int x = 5;
int y = 10;
int z;

z = x + y;

if (z > 10) {
    x = 100;
}
```

### 3. Ejecutar el Compilador
```bash
mvn exec:java -Dexec.mainClass="compiladores.App"
```

### 4. Verificar la Salida
El código ensamblador se genera en: `salida/programa.asm`

## Ensamblar y Ejecutar el Código Generado

### Requisitos
- **NASM** (Netwide Assembler)
- **ld** (GNU Linker)
- Sistema Linux de 32 bits o con soporte para binarios de 32 bits

### Instalación de NASM (Debian/Ubuntu)
```bash
sudo apt-get update
sudo apt-get install nasm
sudo apt-get install gcc-multilib  # Para soporte de 32 bits
```

### Proceso de Ensamblado y Ejecución

#### 1. Ensamblar el código
```bash
nasm -f elf32 salida/programa.asm -o salida/programa.o
```

**Explicación:**
- `-f elf32`: Formato de salida ELF de 32 bits
- `-o`: Especifica el archivo objeto de salida

#### 2. Enlazar el ejecutable
```bash
ld -m elf_i386 salida/programa.o -o salida/programa
```

**Explicación:**
- `-m elf_i386`: Emulación para arquitectura i386 (32 bits)
- `-o`: Especifica el archivo ejecutable de salida

#### 3. Ejecutar el programa
```bash
./salida/programa
echo $?  # Ver el código de salida (siempre 0 en esta implementación)
```

### Script de Automatización

Puedes crear un script bash para automatizar el proceso completo:

```bash
#!/bin/bash
# compilar_y_ejecutar.sh

echo "=== Compilando proyecto Java ==="
mvn clean compile

echo -e "\n=== Ejecutando compilador ==="
mvn exec:java -Dexec.mainClass="compiladores.App"

if [ -f "salida/programa.asm" ]; then
    echo -e "\n=== Ensamblando código ==="
    nasm -f elf32 salida/programa.asm -o salida/programa.o
    
    if [ $? -eq 0 ]; then
        echo "=== Enlazando ejecutable ==="
        ld -m elf_i386 salida/programa.o -o salida/programa
        
        if [ $? -eq 0 ]; then
            echo -e "\n=== Ejecutando programa ==="
            ./salida/programa
            echo -e "\nCódigo de salida: $?"
        else
            echo "Error en el enlazado"
        fi
    else
        echo "Error en el ensamblado"
    fi
else
    echo "No se generó el archivo ensamblador"
fi
```

Dar permisos de ejecución:
```bash
chmod +x compilar_y_ejecutar.sh
./compilar_y_ejecutar.sh
```

## Ejemplos Completos

### Ejemplo 1: Cálculo Aritmético Simple
**Entrada (`entrada/programa.txt`):**
```c
int a = 5;
int b = 3;
int suma;
int producto;

suma = a + b;
producto = a * b;
```

**Salida esperada:** Variables inicializadas y operaciones realizadas.

### Ejemplo 2: Estructura de Control
**Entrada:**
```c
int x = 10;
int resultado = 0;

if (x > 5) {
    resultado = 100;
} else {
    resultado = 50;
}
```

**Salida esperada:** `resultado` contendrá 100.

### Ejemplo 3: Bucles
**Entrada:**
```c
int suma = 0;
int i;

for (i = 0; i < 5; i++) {
    suma = suma + i;
}
```

**Salida esperada:** `suma` contendrá 0+1+2+3+4 = 10.

## Limitaciones Conocidas

### Actualmente NO Soportado
- ❌ **Funciones personalizadas** (prototipofunc, declaracionfunc, llamadafunc)
- ❌ **Arrays** (declarados pero no implementados)
- ❌ **Operadores lógicos compuestos** (múltiples AND/OR en condiciones)
- ❌ **Incremento/decremento inline** (++/-- como expresión)
- ❌ **Entrada/salida** (printf, scanf equivalentes)
- ❌ **Retorno de valores** (return statements)

### En Desarrollo
- 🔄 **Optimización de código** (eliminar movimientos redundantes)
- 🔄 **Gestión de registros mejorada** (uso de más registros)
- 🔄 **Soporte para expresiones booleanas complejas**

## Debugging del Código Generado

### Visualizar el Código Generado
```bash
cat salida/programa.asm
```

### Debugging con GDB
```bash
# Ensamblar con símbolos de debug
nasm -f elf32 -g -F dwarf salida/programa.asm -o salida/programa.o
ld -m elf_i386 salida/programa.o -o salida/programa

# Ejecutar con GDB
gdb salida/programa

# Comandos útiles de GDB:
# break _start    - Establecer breakpoint en inicio
# run             - Ejecutar programa
# stepi           - Ejecutar una instrucción
# info registers  - Ver valores de registros
# x/4xw 0x...     - Ver memoria (dirección de variable)
# quit            - Salir de GDB
```

### Verificar Estructura del Binario
```bash
file salida/programa          # Ver tipo de archivo
objdump -d salida/programa    # Desensamblar el binario
readelf -h salida/programa    # Ver headers ELF
```

## Solución de Problemas

### Error: "file format not recognized"
**Causa:** Sistema no soporta binarios de 32 bits.

**Solución:**
```bash
sudo apt-get install gcc-multilib
```

### Error: "No such file or directory" al ejecutar
**Causa:** Falta el loader de 32 bits.

**Solución:**
```bash
sudo apt-get install libc6-i386
```

### Código genera pero no funciona como esperado
**Diagnóstico:**
1. Verificar errores semánticos en la salida del compilador
2. Revisar el código ensamblador generado
3. Usar GDB para debug paso a paso
4. Verificar que las variables estén inicializadas antes de usarse

## Estructura del Código Generado

### Formato Estándar
```nasm
; Sección de datos (variables)
section .bss
    variable1: resd 1
    variable2: resd 1

; Código ensamblador generado automáticamente
bits 32
global _start

; Sección de código ejecutable
section .text
_start:
    ; Código del programa
    ; ...
    
    ; Salida del programa
    mov eax, 1        ; syscall: exit
    xor ebx, ebx      ; código: 0
    int 0x80          ; llamada al sistema
```

## Referencias

### Documentación NASM
- [Manual oficial de NASM](https://www.nasm.us/doc/)
- [Sintaxis Intel vs AT&T](https://www.nasm.us/doc/nasmdoc2.html)

### Instrucciones x86
- [Referencia de instrucciones Intel](https://www.intel.com/content/www/us/en/developer/articles/technical/intel-sdm.html)
- [Syscalls de Linux](https://chromium.googlesource.com/chromiumos/docs/+/master/constants/syscalls.md)

### Tutoriales
- [Assembly x86 en Linux](https://cs.lmu.edu/~ray/notes/nasmtutorial/)
- [Calling conventions](https://en.wikipedia.org/wiki/X86_calling_conventions)

## Contribuciones Futuras

### Mejoras Planificadas
1. **Soporte para funciones**
   - Generación de prólogo/epílogo
   - Paso de parámetros por pila
   - Valor de retorno en EAX

2. **Optimizaciones**
   - Eliminación de código muerto
   - Propagación de constantes
   - Reducción de movimientos redundantes

3. **Extensiones**
   - Soporte para tipos de dato adicionales (char, float)
   - Arrays multidimensionales
   - Strings

4. **Debugging**
   - Generación de información de debug (DWARF)
   - Comentarios con línea de código original
   - Mapeo de variables a direcciones

---

**Autor:** TecCompilacion2024  
**Versión:** 1.0  
**Fecha:** 2024

