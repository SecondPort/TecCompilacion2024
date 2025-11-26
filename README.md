# TecCompilacion2024

## Descripción General del Proyecto
Este es un proyecto de compilador construido con **ANTLR4** (v4.13.1) y **Java 17**. El compilador realiza análisis léxico, sintáctico y semántico sobre un lenguaje similar a C.

## Arquitectura

### Límites de Componentes
- **Definición de Gramática**: `src/main/antlr4/compiladores/compiladores.g4` - Gramática ANTLR4 que define la sintaxis del lenguaje
- **Código Generado**: `target/generated-sources/antlr4/` - Clases auto-generadas de parser/lexer (NO editar manualmente)
- **Patrón Listener**: `Escucha.java` extiende `compiladoresBaseListener` para análisis semántico durante el recorrido del árbol de parseo
- **Patrón Visitor**: `Caminante.java` extiende `compiladoresBaseVisitor` para recorrido alternativo del árbol (actualmente usado para contar asignaciones)
- **Tabla de Símbolos**: `TablaSimbolos.java` - Singleton que gestiona tablas de símbolos con ámbitos usando una pila de contextos

### Flujo de Datos
1. Archivo de entrada (`entrada/programa.txt`) → CharStream
2. `compiladoresLexer` tokeniza la entrada
3. `compiladoresParser` construye el árbol de parseo usando las reglas de gramática
4. El listener `Escucha` recorre el árbol, realizando chequeos semánticos y poblando la tabla de símbolos
5. Los mensajes de error se imprimen en stdout durante el recorrido

## Flujos de Trabajo Críticos

### Compilar y Generar el Parser
```bash
mvn clean compile
```
Esto activa el plugin Maven de ANTLR4 para regenerar el parser/lexer desde el archivo `.g4`. **Siempre ejecutar después de cambios en la gramática.**

### Ejecutar el Compilador
```bash
mvn exec:java -Dexec.mainClass="compiladores.App"
```
Parsea `entrada/programa.txt` por defecto. Para cambiar la entrada, modificar la ruta hardcodeada en `App.java`:
```java
CharStream input = CharStreams.fromFileName("entrada/programa.txt");
```

## Convenciones Específicas del Proyecto

### Gestión de la Tabla de Símbolos
- **Contextos con ámbito**: Llamar `tabla.addContexto()` al entrar, `tabla.delContexto()` al salir de bloques
- **Patrón Singleton**: Siempre usar `TablaSimbolos.getInstancia()` - nunca instanciar directamente
- **Búsqueda local vs global**: Usar `contieneSimboloLocal()` para chequeos de doble declaración en el ámbito actual, `contieneSimbolo()` para validación de uso en todos los ámbitos

### Patrón de Validación Semántica
Todos los chequeos semánticos en `Escucha.java` siguen este patrón:
```java
@Override
public void exitRuleName(RuleContext ctx) {
    super.exitRuleName(ctx);  // Llamar al padre primero
    
    // 1. Validación semántica
    Id simbolo = tabla.getSimbolo(ctx.ID().getText());
    if (simbolo == null) {
        // Reportar error con número de línea
        System.out.println("Error semantico: ... (Linea: " + ctx.getStart().getLine() + ")");
        errors++;
    }
    
    // 2. Validación sintáctica (si es necesario)
    Token lastToken = ctx.getStop();
    if (lastToken == null || !lastToken.getText().equals(";")) {
        System.out.println("Error sintáctico: ... (Línea: " + ctx.getStop().getLine() + ")");
    }
}
```

### Directrices para Extensión de Gramática
- **Tokens terminales**: Definir en MAYÚSCULAS (ej., `INT`, `WHILE`, `PYC`)
- **Reglas**: Definir en minúsculas (ej., `programa`, `declaracion`, `expresion`)
- **Fragmentos**: Usar solo para clases de caracteres (ej., `fragment LETRA : [A-Za-z];`)
- La generación de Parser/Visitor está habilitada en `pom.xml` - ambos patrones están disponibles pero Listener es el primario

## Pruebas
Los archivos de prueba de entrada están en el directorio `entrada/`. El archivo de prueba actual (`programa.txt`) intencionalmente contiene errores:
- Punto y coma faltantes
- Uso de variable no declarada (`b`)
- Uso de variable no inicializada (`j` en bucle for)

## Errores Comunes
- **No editar archivos generados** en `target/` o `src/main/antlr4/compiladores/*.java`
- **La pila de contextos debe estar balanceada**: Cada `addContexto()` necesita un `delContexto()` correspondiente
- **Inicialización de variables**: Rastrear mediante `setInicializado()` en la declaración, chequear antes de usar
- La ruta del archivo de entrada está hardcodeada - actualizar `App.java` para archivos de prueba diferentes

## Archivos Clave para Referencia
- Reglas de gramática: `src/main/antlr4/compiladores/compiladores.g4`
- Análisis semántico: `src/main/java/compiladores/Escucha.java`
- Estructura de tabla de símbolos: `src/main/java/compiladores/TablaSimbolos.java`
- Entrada de prueba: `entrada/programa.txt`
