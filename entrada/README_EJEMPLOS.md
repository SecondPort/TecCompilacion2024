# Ejemplos de Código Fuente

Este directorio contiene varios ejemplos de código fuente para probar el compilador y el generador de código ensamblador.

## Archivos de Ejemplo

### 1. `programa.txt` (Archivo original con errores)
**Propósito:** Archivo de prueba original del proyecto con errores intencionales.

**Contenido:**
- Declaraciones con errores sintácticos (punto y coma faltante)
- Uso de variable no declarada (`b`)
- Uso de variable no inicializada (`j`)
- Bucles for con errores

**Uso:**
```bash
# Este es el archivo por defecto
mvn exec:java -Dexec.mainClass="compiladores.App"
```

**Errores esperados:**
- Error sintáctico en línea 1
- Error semántico por variable `b` no declarada
- Error semántico por variable `j` no inicializada

---

### 2. `ejemplo_completo.txt` (Sin errores)
**Propósito:** Demostración completa de todas las características soportadas.

**Características demostradas:**
- ✅ Declaraciones con inicialización
- ✅ Expresiones aritméticas complejas
- ✅ Estructura while
- ✅ Estructura if-else
- ✅ Bucle for

**Código:**
```c
int x = 10;
int y = 20;
int resultado;

resultado = x + y * 2;

int contador = 0;

while (contador < 5) {
    contador = contador + 1;
}

if (resultado > 30) {
    x = 100;
} else {
    y = 200;
}

for (int i = 0; i < 3; i++) {
    resultado = resultado + 1;
}
```

**Uso:**
Para usar este archivo, modifica `App.java` línea 62 o copia este contenido a `programa.txt`.

---

### 3. `test_aritmetica.txt`
**Propósito:** Prueba exhaustiva de operaciones aritméticas.

**Operaciones probadas:**
- ✅ Suma (+)
- ✅ Resta (-)
- ✅ Multiplicación (*)
- ✅ División (/)
- ✅ Módulo (%)
- ✅ Expresiones con precedencia

**Código:**
```c
int a = 10;
int b = 5;
int suma;
int resta;
int mult;
int div;
int mod;

suma = a + b;
resta = a - b;
mult = a * b;
div = a / b;
mod = a % b;

int complejo;
complejo = a + b * 2 - 3;
```

**Valores esperados:**
- suma = 15
- resta = 5
- mult = 50
- div = 2
- mod = 0
- complejo = 10 + (5*2) - 3 = 17

---

### 4. `test_if_else.txt`
**Propósito:** Prueba de estructuras condicionales.

**Características:**
- ✅ If-else con comparaciones
- ✅ Asignaciones condicionales
- ✅ If sin else

**Código:**
```c
int x = 10;
int y = 20;
int mayor;

if (x > y) {
    mayor = x;
} else {
    mayor = y;
}

int resultado = 0;

if (mayor > 15) {
    resultado = 100;
}
```

**Lógica:**
1. Compara x (10) con y (20)
2. Como x NO es mayor, mayor = y = 20
3. Como mayor (20) > 15, resultado = 100

---

### 5. `test_while.txt`
**Propósito:** Prueba de bucles while.

**Características:**
- ✅ Bucle con condición
- ✅ Modificación de contador dentro del bucle
- ✅ Acumulación de valores

**Código:**
```c
int contador = 0;
int limite = 10;
int suma = 0;

while (contador < limite) {
    suma = suma + contador;
    contador = contador + 1;
}
```

**Lógica:**
- Suma = 0 + 1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9 = 45
- Contador final = 10

---

### 6. `test_for.txt`
**Propósito:** Prueba de bucles for.

**Características:**
- ✅ Bucle for con inicialización
- ✅ Condición de salida
- ✅ Incremento automático

**Código:**
```c
int suma = 0;

for (int i = 0; i < 5; i++) {
    suma = suma + i;
}

int producto = 1;

for (int j = 1; j < 4; j++) {
    producto = producto * j;
}
```

**Lógica:**
- suma = 0 + 1 + 2 + 3 + 4 = 10
- producto = 1 * 1 * 2 * 3 = 6

---

## Cómo Usar Estos Ejemplos

### Método 1: Modificar App.java
Edita la línea 62 en `src/main/java/compiladores/App.java`:

```java
// Cambiar de:
CharStream input = CharStreams.fromFileName("entrada/programa.txt");

// A (por ejemplo):
CharStream input = CharStreams.fromFileName("entrada/test_aritmetica.txt");
```

Luego compila y ejecuta:
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="compiladores.App"
```

### Método 2: Copiar al archivo por defecto
```bash
cp entrada/test_aritmetica.txt entrada/programa.txt
mvn exec:java -Dexec.mainClass="compiladores.App"
```

### Método 3: Usar el script de automatización
Si has modificado `App.java` para aceptar argumentos:
```bash
./compilar_y_ensamblar.sh entrada/test_aritmetica.txt
```

---

## Verificar Resultados

### Ver el código ensamblador generado:
```bash
cat salida/programa.asm
```

### Ver desensamblado del ejecutable:
```bash
objdump -d salida/programa | less
```

### Debugear con GDB:
```bash
gdb salida/programa

# Comandos útiles:
(gdb) break _start
(gdb) run
(gdb) stepi              # Ejecutar instrucción por instrucción
(gdb) info registers     # Ver registros
(gdb) x/10xw &variable   # Ver memoria de variable
```

---

## Crear Tus Propios Ejemplos

### Plantilla básica:
```c
// Declaraciones
int variable1 = valor;
int variable2;

// Asignaciones
variable2 = expresion;

// Estructuras de control
if (condicion) {
    // código
}

while (condicion) {
    // código
}

for (int i = inicio; i < fin; i++) {
    // código
}
```

### Reglas importantes:
1. ✅ Todas las declaraciones deben terminar con `;`
2. ✅ Las variables deben declararse antes de usarse
3. ✅ Las variables deben inicializarse antes de leerse
4. ✅ Los bloques deben estar entre `{` y `}`
5. ✅ Las comparaciones deben estar entre `(` y `)`

### Errores comunes a evitar:
- ❌ Usar variables no declaradas
- ❌ Leer variables no inicializadas
- ❌ Declarar la misma variable dos veces en el mismo scope
- ❌ Olvidar punto y coma
- ❌ Olvidar llaves en bloques

---

## Comparación de Ejemplos

| Ejemplo | Declaraciones | Asignaciones | If-Else | While | For | Complejidad |
|---------|---------------|--------------|---------|-------|-----|-------------|
| programa.txt | 5 | 3 | ❌ | ❌ | 3 | Media (con errores) |
| ejemplo_completo.txt | 4 | 5 | ✅ | ✅ | ✅ | Alta |
| test_aritmetica.txt | 8 | 6 | ❌ | ❌ | ❌ | Baja |
| test_if_else.txt | 4 | 2 | ✅ | ❌ | ❌ | Baja |
| test_while.txt | 3 | 2 | ❌ | ✅ | ❌ | Baja |
| test_for.txt | 2 | 2 | ❌ | ❌ | ✅ | Baja |

---

## Progresión de Aprendizaje Sugerida

### Nivel 1: Básico
1. `test_aritmetica.txt` - Entender operaciones básicas
2. Variables y asignaciones simples

### Nivel 2: Intermedio
1. `test_if_else.txt` - Estructuras condicionales
2. Comparaciones y flujo de control

### Nivel 3: Avanzado
1. `test_while.txt` - Bucles con condiciones
2. `test_for.txt` - Bucles con iteradores
3. `ejemplo_completo.txt` - Integración de todo

### Nivel 4: Debugging
1. `programa.txt` - Identificar y corregir errores
2. Análisis de mensajes de error del compilador

---

## Extender los Ejemplos

### Ideas para nuevos ejemplos:
- 📝 Cálculo de factorial usando for
- 📝 Búsqueda de números primos
- 📝 Serie de Fibonacci
- 📝 Conversión de temperatura
- 📝 Cálculo de áreas geométricas
- 📝 Algoritmos de ordenamiento simple

---

**Nota:** Para funciones personalizadas y arrays, se necesitará extender el generador de código en el futuro.

