#!/bin/bash
# Script para compilar el proyecto Java y ensamblar el código generado
# Uso: ./compilar_y_ensamblar.sh [archivo_entrada]
# Si no se proporciona archivo, usa entrada/programa.txt por defecto

set -e  # Salir si hay algún error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Banner
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   Compilador TecCompilacion2024${NC}"
echo -e "${BLUE}   Generador de Código Ensamblador${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Archivo de entrada
ARCHIVO_ENTRADA="${1:-entrada/test_aritmetica.txt}"

if [ ! -f "$ARCHIVO_ENTRADA" ]; then
    echo -e "${RED}Error: El archivo $ARCHIVO_ENTRADA no existe${NC}"
    exit 1
fi

echo -e "${YELLOW}Archivo de entrada:${NC} $ARCHIVO_ENTRADA\n"

# Fase 1: Compilar el proyecto Java
echo -e "${GREEN}[1/5] Compilando proyecto Java...${NC}"
mvn clean compile -q
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Compilación Java exitosa${NC}\n"
else
    echo -e "${RED}✗ Error en la compilación Java${NC}"
    exit 1
fi

# Fase 2: Ejecutar el compilador
echo -e "${GREEN}[2/5] Ejecutando compilador...${NC}"
mvn exec:java -Dexec.mainClass="compiladores.App" -q 2>&1 | grep -v "INFO\|WARNING"
if [ -f "salida/programa.asm" ]; then
    echo -e "${GREEN}✓ Código ensamblador generado${NC}\n"
else
    echo -e "${RED}✗ No se generó el código ensamblador${NC}"
    exit 1
fi

# Mostrar resumen del código generado
LINEAS=$(wc -l < salida/programa.asm)
VARIABLES=$(grep -c "resd" salida/programa.asm || echo "0")
ETIQUETAS=$(grep -c "^L[0-9]*:" salida/programa.asm || echo "0")

echo -e "${BLUE}Resumen del código generado:${NC}"
echo -e "  • Líneas de código: $LINEAS"
echo -e "  • Variables declaradas: $VARIABLES"
echo -e "  • Etiquetas de control: $ETIQUETAS\n"

# Verificar si NASM está instalado
if ! command -v nasm &> /dev/null; then
    echo -e "${YELLOW}⚠ NASM no está instalado. Instalando...${NC}"
    sudo apt-get update -qq && sudo apt-get install -y nasm -qq
fi

# Fase 3: Ensamblar con NASM
echo -e "${GREEN}[3/5] Ensamblando con NASM...${NC}"
nasm -f elf32 salida/programa.asm -o salida/programa.o 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Ensamblado exitoso${NC}\n"
else
    echo -e "${RED}✗ Error en el ensamblado${NC}"
    exit 1
fi

# Fase 4: Enlazar
echo -e "${GREEN}[4/5] Enlazando ejecutable...${NC}"
ld -m elf_i386 salida/programa.o -o salida/programa 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Enlazado exitoso${NC}\n"
else
    echo -e "${RED}✗ Error en el enlazado${NC}"
    echo -e "${YELLOW}Nota: Si el error es sobre formato no reconocido,${NC}"
    echo -e "${YELLOW}      ejecuta: sudo apt-get install gcc-multilib${NC}"
    exit 1
fi

# Fase 5: Ejecutar
echo -e "${GREEN}[5/5] Ejecutando programa generado...${NC}"
./salida/programa
CODIGO_SALIDA=$?
echo -e "${GREEN}✓ Programa ejecutado${NC}"
echo -e "${BLUE}Código de salida:${NC} $CODIGO_SALIDA\n"

# Mostrar información de archivos generados
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Archivos generados:${NC}"
echo -e "  • Código ASM: ${GREEN}salida/programa.asm${NC}"
echo -e "  • Objeto:     ${GREEN}salida/programa.o${NC}"
echo -e "  • Ejecutable: ${GREEN}salida/programa${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Opciones adicionales
echo -e "${YELLOW}Comandos útiles:${NC}"
echo -e "  Ver código ASM:    ${GREEN}cat salida/programa.asm${NC}"
echo -e "  Ver desensamblado: ${GREEN}objdump -d salida/programa${NC}"
echo -e "  Debugear con GDB:  ${GREEN}gdb salida/programa${NC}"
echo -e "  Ver archivo info:  ${GREEN}file salida/programa${NC}\n"

echo -e "${GREEN}¡Compilación completada exitosamente!${NC}"

