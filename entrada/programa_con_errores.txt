// Variables globales
int variableGlobal;
int variableGlobal; // ERROR: Variable duplicada en el mismo ámbito
double valorGlobal;
char caracterGlobal;

// Función con múltiples errores
int miFuncion(int parametro1, double parametro2) {
    int variableLocal;
    int variableLocal; // ERROR: Variable duplicada en función
    
    // Variables nunca utilizadas (WARNING)
    int variableNoUsada1;
    string variableNoUsada2;
    double variableNoUsada3;
    
    parametro1 = 100; // OK: asignación a parámetro
    variableLocal = parametro1 + 5; // OK: uso de variable
    
    // ERROR: Asignación a variable no declarada
    variableFantasma = 42;
    
    // ERROR: Asignación a una función (no es variable)
    miFuncion = 10;
    
    // OK: usar variable global
    valorGlobal = 3.14;
    
    return variableLocal;
}

void funcionVoid() {
    int x;
    int y;
    int z; // WARNING: declarada pero no usada
    
    x = 10; // OK
    y = x + 5; // OK: uso de variables
    
    // ERROR: variable no declarada
    w = x + y;
}

// Función main para completar el programa
int main() {
    int resultado;
    int valor;
    
    resultado = miFuncion(5, 3.14); // OK: uso de función
    valor = resultado + 10;         // OK: uso de variables
    
    // ERROR: variable no declarada
    variableFinal = valor;
    
    // OK: usar variable global
    variableGlobal = valor;
    
    return resultado;
}