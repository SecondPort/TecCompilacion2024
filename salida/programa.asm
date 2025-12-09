section .data
    ; Constantes inicializadas
    _cd0 dq 2.5
    _cd1 dq 2.5
    _cd2 dq 0.5
    _cd3 dq 1.0

section .bss
    ; Variables globales
    a: resd 1  ; int
    b: resd 1  ; int
    c: resd 1  ; int
    d1: resq 1  ; double
    d2: resq 1  ; double
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
    push ebp
    mov ebp, esp
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
    fld qword [_cd0]
    fstp qword [d1]
    fld qword [_cd1]
    ; Inicialización de d2
    mov eax, 1
    push eax
    fild dword [esp]
    add esp, 4
    fstp qword [d2]
    mov eax, 1
    ; Inicialización de e
    fld qword [d1]
    fistp dword [e]
    fld qword [d1]
    ; Inicialización de ch
    mov eax, 65
    mov byte [ch], al
    mov eax, 65
    ; Inicialización de r1
    fld qword [d1]
    sub esp, 8
    fstp qword [esp]
    mov eax, [a]
    push eax
    call sumar
    add esp, 12
    mov dword [r1], eax
    fld qword [d1]
    sub esp, 8
    fstp qword [esp]
    mov eax, [a]
    push eax
    call sumar
    add esp, 12
    ; Inicialización de r2
    mov eax, [a]
    push eax
    call sumar
    add esp, 4
    mov dword [r2], eax
    mov eax, [a]
    push eax
    call sumar
    add esp, 4
    ; Inicialización de r3
    mov eax, 10
    push eax
    call sinReturn
    add esp, 4
    mov dword [r3], eax
    mov eax, 10
    push eax
    call sinReturn
    add esp, 4

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
    fld qword [_cd2]
    jmp main_end
    ; Fin de bloque
main_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

sumar:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque
    mov eax, [a]
    push eax
    mov eax, [b]
    mov ebx, eax
    pop eax
    add eax, ebx
    jmp sumar_end
    ; Fin de bloque
sumar_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

sinReturn:
    push ebp
    mov ebp, esp
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
sinReturn_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

g:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque
    mov eax, 1
    jmp g_end
    ; Fin de bloque
g_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

h:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque
    mov eax, [a]
    jmp h_end
    ; Fin de bloque
h_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

h:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque
    fld qword [_cd3]
    jmp h_end
    ; Fin de bloque
h_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

testBreak:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque
    ; Fin de bloque
testBreak_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

testContinue:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque
    ; Fin de bloque
testContinue_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

    ; Fin del programa
    mov eax, 1        ; syscall: exit
    xor ebx, ebx      ; código de salida: 0
    int 0x80          ; llamada al sistema
