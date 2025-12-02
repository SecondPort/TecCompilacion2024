section .bss
    ; Variables globales
    x: resd 1  ; int
    y: resd 1  ; int
    z: resd 1  ; int
    b: resd 1  ; int
    j: resd 1  ; int
    suma: resd 1  ; int
    i: resd 1  ; int
    k: resd 1  ; int
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
    ; Inicialización de b
    mov dword [b], 5

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
    ; Inicialización de j
    mov dword [j], 0
    ; Inicialización de suma
    mov dword [suma], 0

    ; Estructura FOR
    ; Inicialización de i
    mov dword [i], 0
L0:
    mov eax, [i]
    push eax
    mov eax, 10
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L1  ; salir si falso
    ; Inicio de bloque

    ; Asignación a suma
    mov eax, [suma]
    push eax
    mov eax, [i]
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [suma], eax
    ; Fin de bloque
    ; Incremento de i
    inc dword [i]
    jmp L0  ; repetir bucle
L1:

    ; Estructura FOR
    ; Inicialización de k
    mov dword [k], 0
L2:
    mov eax, [k]
    push eax
    mov eax, 5
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L3  ; salir si falso
    ; Inicio de bloque

    ; Asignación a j
    mov eax, [j]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [j], eax
    ; Fin de bloque
    ; Incremento de k
    inc dword [k]
    jmp L2  ; repetir bucle
L3:

    ; Estructura FOR
    ; Inicialización de l
    mov dword [l], 0
L4:
    mov eax, [l]
    push eax
    mov eax, 3
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L5  ; salir si falso
    ; Inicio de bloque

    ; Asignación a x
    mov eax, [x]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [x], eax
    ; Fin de bloque
    ; Incremento de l
    inc dword [l]
    jmp L4  ; repetir bucle
L5:

    ; Fin del programa
    mov eax, 1        ; syscall: exit
    xor ebx, ebx      ; código de salida: 0
    int 0x80          ; llamada al sistema
