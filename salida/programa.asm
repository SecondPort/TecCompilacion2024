section .bss
    ; Variables globales
    x: resd 1  ; int
    y: resd 1  ; int
    z: resd 1  ; int
    j: resd 1  ; int
    i: resd 1  ; int
    i: resd 1  ; int
    l: resd 1  ; int

; Código ensamblador generado automáticamente
; Compilador: TecCompilacion2024
; Sintaxis: NASM (x86)

bits 32
global _start

section .text
_start:
    ; Inicio del programa
    ; Inicialización de x
    mov dword [x], 1
    ; Inicialización de y
    mov dword [y], 3

    ; Asignación a x
    mov eax, 1
    push eax
    mov eax, [y]
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [x], eax

    ; Asignación a x
    mov eax, 1
    push eax
    mov eax, [b]
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [x], eax

    ; Asignación a z
    mov eax, 1
    push eax
    mov eax, [y]
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [z], eax

    ; Estructura FOR
    ; Inicialización de i
    mov dword [i], 0
    mov eax, [i]
    push eax
    mov eax, 10
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
L0:
    je L1  ; salir si falso
    ; Inicio de bloque
    ; Fin de bloque
    jmp L0  ; repetir bucle
L1:

    ; Estructura FOR
    ; Inicialización de i
    mov dword [i], 0
    mov eax, [i]
    push eax
    mov eax, 10
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
L2:
    je L3  ; salir si falso
    ; Inicio de bloque
    ; Fin de bloque
    jmp L2  ; repetir bucle
L3:

    ; Estructura FOR
    ; Inicialización de l
    mov dword [l], 0
    mov eax, [l]
    push eax
    mov eax, 10
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
L4:
    je L5  ; salir si falso
    ; Inicio de bloque
    ; Fin de bloque
    jmp L4  ; repetir bucle
L5:

    ; Fin del programa
    mov eax, 1        ; syscall: exit
    xor ebx, ebx      ; código de salida: 0
    int 0x80          ; llamada al sistema
