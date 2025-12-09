section .bss
    ; Variables globales
    a: resd 1  ; int
    b: resd 1  ; int
    c: resd 1  ; int
    d1: resd 1  ; double
    d2: resd 1  ; double
    e: resd 1  ; int
    ch: resb 1  ; char
    r1: resd 1  ; int
    r2: resd 1  ; int
    r3: resd 1  ; int
    y: resd 1  ; int

; Código ensamblador generado automáticamente
; Compilador: TecCompilacion2024
; Sintaxis: NASM (x86)

bits 32
global _start

section .text
_start:
    ; Inicio del programa

main:
    ; cuerpo de función
    ; Inicio de bloque
    ; Inicialización de a
    mov eax, 1
    mov dword [a], eax
    mov eax, 1

    ; Asignación a x
    mov eax, 5
    mov [x], eax
    ; Inicialización de c
    mov eax, [d]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    mov dword [c], eax
    mov eax, [d]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    ; Inicialización de d1
    mov eax, 2.5
    mov dword [d1], eax
    mov eax, 2.5
    ; Inicialización de d2
    mov eax, 1
    mov dword [d2], eax
    mov eax, 1
    ; Inicialización de e
    mov eax, [d1]
    mov dword [e], eax
    mov eax, [d1]
    ; Inicialización de ch
    mov eax, 65
    mov byte [ch], al
    mov eax, 65
    ; Inicialización de r1
    mov dword [r1], eax
    ; Inicialización de r2
    mov dword [r2], eax
    ; Inicialización de r3
    mov dword [r3], eax

    ; Estructura IF
    mov eax, [z]
    push eax
    mov eax, 0
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    setg al
    je L0  ; saltar si falso
    ; Inicio de bloque

    ; Asignación a y
    mov eax, [y]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [y], eax
    ; Fin de bloque
    jmp L1  ; saltar al final
L0:
L1:

    ; Estructura FOR
L2:
    mov eax, [i]
    push eax
    mov eax, 10
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    setl al
    je L3  ; salir si falso
    ; Inicio de bloque
    ; break
    jmp L3
    ; Fin de bloque
    ; Actualización por expresión
    mov eax, [i]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    jmp L2  ; repetir bucle
L3:
    mov eax, 0.5
    ; Fin de bloque
    mov eax, 0
    ret

sumar:
    ; cuerpo de función
    ; Inicio de bloque
    mov eax, [a]
    push eax
    mov eax, [b]
    mov ebx, eax
    pop eax
    add eax, ebx
    ; Fin de bloque
    mov eax, 0
    ret

sinReturn:
    ; cuerpo de función
    ; Inicio de bloque
    ; Inicialización de y
    mov eax, [x]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    mov dword [y], eax
    mov eax, [x]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    ; Fin de bloque
    mov eax, 0
    ret

g:
    ; cuerpo de función
    ; Inicio de bloque
    mov eax, 1
    ; Fin de bloque
    mov eax, 0
    ret

h:
    ; cuerpo de función
    ; Inicio de bloque
    mov eax, [a]
    ; Fin de bloque
    mov eax, 0
    ret

h:
    ; cuerpo de función
    ; Inicio de bloque
    mov eax, 1.0
    ; Fin de bloque
    mov eax, 0
    ret

testBreak:
    ; cuerpo de función
    ; Inicio de bloque
    ; Fin de bloque
    mov eax, 0
    ret

testContinue:
    ; cuerpo de función
    ; Inicio de bloque
    ; Fin de bloque
    mov eax, 0
    ret

    ; Fin del programa
    mov eax, 1        ; syscall: exit
    xor ebx, ebx      ; código de salida: 0
    int 0x80          ; llamada al sistema
