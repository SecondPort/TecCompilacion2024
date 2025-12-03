section .bss
    ; Variables globales
    a: resd 1  ; int
    b: resd 1  ; int
    c: resb 1  ; char
    i: resd 1  ; int
    suma: resd 1  ; int
    ch: resb 1  ; char
    r: resd 1  ; int
    local: resd 1  ; int
    res: resd 1  ; int
    noInit: resd 1  ; int
    otro: resd 1  ; int

; Código ensamblador generado automáticamente
; Compilador: TecCompilacion2024
; Sintaxis: NASM (x86)

bits 32
global _start

section .text
_start:
    ; Inicio del programa
    ; Inicialización de b
    mov dword [b], 5
    ; Inicialización de c (char)
    mov byte [c], 122

    ; Asignación a a
    mov eax, 3
    mov [a], eax

    ; Asignación a b
    mov eax, [b]
    push eax
    mov eax, [a]
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [b], eax

    ; Estructura IF
    mov eax, [a]
    push eax
    mov eax, 3
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L0  ; saltar si falso
    ; Inicio de bloque

    ; Asignación a b
    mov eax, [b]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [b], eax
    ; Fin de bloque
    jmp L1  ; saltar al final
L0:
    ; Inicio de bloque

    ; Asignación a b
    mov eax, [b]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    sub eax, ebx  ; resta
    mov [b], eax
    ; Fin de bloque
L1:

    ; Estructura WHILE
L2:
    mov eax, [a]
    push eax
    mov eax, 10
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L3  ; salir si falso
    ; Inicio de bloque

    ; Asignación a a
    mov eax, [a]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [a], eax
    ; Fin de bloque
    jmp L2  ; repetir bucle
L3:
    ; Inicialización de suma
    mov dword [suma], 0
    ; Inicialización de ch (char)
    mov byte [ch], 97

    ; Estructura FOR
L4:
    mov eax, [i]
    push eax
    mov eax, 10
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L5  ; salir si falso
    ; Inicio de bloque

    ; Estructura IF
    mov eax, [i]
    push eax
    mov eax, 2
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L6  ; saltar si falso
    ; Inicio de bloque
    ; continue
    jmp L4
    ; Fin de bloque
    jmp L7  ; saltar al final
L6:
L7:

    ; Estructura IF
    mov eax, [i]
    push eax
    mov eax, 7
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L8  ; saltar si falso
    ; Inicio de bloque
    ; break
    jmp L5
    ; Fin de bloque
    jmp L9  ; saltar al final
L8:
L9:

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
    jmp L4  ; repetir bucle
L5:

f:
    ; cuerpo de función
    ; Inicio de bloque
    ; Inicialización de r
    mov dword [r], 0

    ; Asignación a r
    mov eax, [x]
    push eax
    mov eax, [y]
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [r], eax
    mov eax, [r]
    ; Fin de bloque
    mov eax, 0
    ret

g:
    ; cuerpo de función
    ; Inicio de bloque
    mov eax, [p]
    ; Fin de bloque
    mov eax, 0
    ret

h:
    ; cuerpo de función
    ; Inicio de bloque
    ; Inicialización de local
    mov dword [local], 1

    ; Asignación a local
    mov eax, [local]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [local], eax
    ; Fin de bloque
    mov eax, 0
    ret

    ; Asignación a res
    mov eax, [f]
    mov [res], eax

    ; Asignación a res
    mov eax, [g]
    mov [res], eax

    ; Asignación a x
    mov eax, 10
    mov [x], eax

    ; Estructura IF
    mov eax, [noInit]
    push eax
    mov eax, 0
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L10  ; saltar si falso
    ; Inicio de bloque

    ; Asignación a noInit
    mov eax, 1
    mov [noInit], eax
    ; Fin de bloque
    jmp L11  ; saltar al final
L10:
L11:

    ; Asignación a b
    mov eax, [b]
    push eax
    mov eax, [otro]
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [b], eax

    ; Fin del programa
    mov eax, 1        ; syscall: exit
    xor ebx, ebx      ; código de salida: 0
    int 0x80          ; llamada al sistema
