section .bss
    ; Variables globales
    i: resd 1  ; int
    suma: resd 1  ; int
    c: resb 1  ; char

; Código ensamblador generado automáticamente
; Compilador: TecCompilacion2024
; Sintaxis: NASM (x86)

bits 32
global _start

section .text
_start:
    ; Inicio del programa
    ; Inicialización de suma
    mov dword [suma], 0
    ; Inicialización de c (char)
    mov byte [c], 97

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

    ; Estructura IF
    mov eax, [i]
    push eax
    mov eax, 3
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L2  ; saltar si falso
    ; Inicio de bloque
    ; continue
    jmp L0
    ; Fin de bloque
    jmp L3  ; saltar al final
L2:
L3:

    ; Estructura IF
    mov eax, [i]
    push eax
    mov eax, 7
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L4  ; saltar si falso
    ; Inicio de bloque
    ; break
    jmp L1
    ; Fin de bloque
    jmp L5  ; saltar al final
L4:
L5:

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

    ; Fin del programa
    mov eax, 1        ; syscall: exit
    xor ebx, ebx      ; código de salida: 0
    int 0x80          ; llamada al sistema
