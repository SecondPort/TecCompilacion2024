<!-- 76c29fe7-d8d8-4ec3-a2b5-624658e7ae83 6a65e8ae-5473-47d7-b434-9b92c713ebd1 -->
# Plan: Documentación Javadoc Completa del Compilador

## Alcance

Documentar los 4 archivos Java escritos manualmente utilizando **Javadoc estándar completo** con:

- Descripciones detalladas de clases, métodos y campos
- Etiquetas `@param`, `@return`, `@throws` donde aplique
- Etiquetas `@author`, `@version`, `@since` en clases
- Ejemplos de uso para componentes complejos
- Referencias cruzadas con `@see` y `{@link}`

## Archivos a Documentar

### 1. [TablaSimbolos.java](src/main/java/compiladores/TablaSimbolos.java)

**Complejidad**: Alta - Contiene 4 tipos (TablaSimbolos, Id, Variable, Funcion, TipoDato)

**Elementos a documentar**:

- **Clase `TablaSimbolos`**: Patrón Singleton, manejo de contextos anidados
                                                                                                                                - Documentar el patrón singleton y su propósito
                                                                                                                                - Métodos: `getInstancia()`, `addContexto()`, `delContexto()`, `addSimbolo()`, `getSimbolo()`, `contieneSimbolo()`, `contieneSimboloLocal()`
                                                                                                                                - Campo `ts` (tabla de símbolos por contexto)

- **Clase abstracta `Id`**: Clase base para identificadores
                                                                                                                                - Documentar cada campo: `nombre`, `tipoDato`, `inicializado`, `usado`
                                                                                                                                - Documentar todos los getters/setters con su propósito

- **Clase `Variable`**: Extensión vacía de Id (documentar herencia)

- **Clase `Funcion`**: 
                                                                                                                                - Campo `argumentos` y su propósito

- **Enum `TipoDato`**: Tipos de datos soportados (VOID, INT, DOUBLE)

### 2. [Escucha.java](src/main/java/compiladores/Escucha.java)

**Complejidad**: Muy Alta - 295 líneas, 15+ métodos override

**Elementos a documentar**:

- **Clase `Escucha`**: Implementación del patrón Listener de ANTLR4
                                                                                                                                - Descripción del propósito (análisis semántico y validaciones)
                                                                                                                                - Campos: `nodos`, `tokens`, `errors`, `tabla`, `erroresAcumulados`

- **Métodos principales** (16 overrides):
                                                                                                                                - `enterPrograma()` / `exitPrograma()`: Inicio/fin del parsing
                                                                                                                                - `enterBloque()` / `exitBloque()`: Manejo de contextos de bloque
                                                                                                                                - `exitDeclaracion()`: Validación de declaraciones de variables
                                                                                                                                - `exitPrototipofunc()` / `exitDeclaracionfunc()`: Declaraciones de funciones
                                                                                                                                - `exitAsignacion()`: Validaciones de asignaciones
                                                                                                                                - `exitFactor()`, `exitFinfor()`, `exitFactorfunc()`, `exitListafactfunc()`, `exitLlamadafunc()`: Validaciones de uso de identificadores
                                                                                                                                - `enterEveryRule()`, `visitErrorNode()`, `visitTerminal()`: Contadores

### 3. [Caminante.java](src/main/java/compiladores/Caminante.java)

**Complejidad**: Media - Implementación del patrón Visitor

**Elementos a documentar**:

- **Clase `Caminante`**: Visitor de ANTLR4 para recorrer el árbol sintáctico
                                                                                                                                - Campo: `asignaciones` (contador)
                                                                                                                                - Métodos: `visitPrograma()`, `visitAsignacion()`, `visitTerminal()`
                                                                                                                                - Explicar el patrón Visitor y su uso

### 4. [App.java](src/main/java/compiladores/App.java)

**Complejidad**: Media - Punto de entrada

**Elementos a documentar**:

- **Clase `App`**: Clase principal del compilador
                                                                                                                                - Método `main()`: Flujo completo de compilación
                                                                                                                                - Documentar cada paso: lectura de archivo, creación de lexer, parser, listener, generación del parse tree

## Estándares a Aplicar

**Formato Javadoc**:

```java
/**
 * Descripción breve en una línea.
 * <p>
 * Descripción detallada en múltiples líneas si es necesario.
 * Puede incluir HTML para formato.
 * </p>
 *
 * @param nombreParam descripción del parámetro
 * @return descripción del valor de retorno
 * @throws TipoExcepcion descripción de cuándo se lanza
 * @see ClaseRelacionada
 * @since 1.0
 */
```

**Orden de etiquetas**: `@author`, `@version`, `@param`, `@return`, `@throws`, `@see`, `@since`

**Para clases**: Incluir descripción del propósito, responsabilidad, patrones de diseño utilizados

**Para métodos**: Describir qué hace (no cómo), precondiciones, postcondiciones, efectos secundarios

## Beneficios Esperados

1. **Mantenibilidad**: Código autodocumentado y comprensible
2. **Generación de documentación**: Posibilidad de generar HTML con `javadoc`
3. **IDE Support**: Mejor autocompletado y hints en IDEs
4. **Estándares profesionales**: Código listo para producción/revisión académica

### To-dos

- [ ] Documentar TablaSimbolos.java (clase Singleton, Id, Variable, Funcion, TipoDato)
- [ ] Documentar Escucha.java (Listener con 16 métodos override)
- [ ] Documentar Caminante.java (patrón Visitor)
- [ ] Documentar App.java (clase principal y método main)