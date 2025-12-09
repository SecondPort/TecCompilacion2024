section .data
    ; Constantes inicializadas
    _cd0 dq 3.14
    _cd1 dq 3.14

section .bss
    ; Variables globales
    variableGlobal: resd 1  ; int
    valorGlobal: resq 1  ; double
    caracterGlobal: resb 1  ; char
    variableLocal: resd 1  ; int
    variableNoUsada1: resd 1  ; int
    variableNoUsada3: resq 1  ; double
    x: resd 1  ; int
    y: resd 1  ; int
    z: resd 1  ; int
    resultado: resd 1  ; int
    valor: resd 1  ; int

; Código ensamblador generado automáticamente
; Compilador: TecCompilacion2024
; Sintaxis: NASM (x86)

bits 32
global _start

section .text
_start:
    ; Inicio del programa

miFuncion:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque

    ; Asignación a parametro1
    mov eax, 100
    mov [parametro1], eax

    ; Asignación a variableLocal
    mov eax, [parametro1]
    push eax
    mov eax, 5
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [variableLocal], eax

    ; Asignación a variableFantasma
    mov eax, 42
    mov [variableFantasma], eax

    ; Asignación a miFuncion
    mov eax, 10
    mov [miFuncion], eax

    ; Asignación a valorGlobal
    fld qword [_cd0]
    fstp qword [valorGlobal]
    mov eax, [variableLocal]
    jmp miFuncion_end
    ; Fin de bloque
miFuncion_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

funcionVoid:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque

    ; Asignación a x
    mov eax, 10
    mov [x], eax

    ; Asignación a y
    mov eax, [x]
    push eax
    mov eax, 5
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [y], eax

    ; Asignación a w
    mov eax, [x]
    push eax
    mov eax, [y]
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [w], eax
    ; Fin de bloque
funcionVoid_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

main:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque

    ; Asignación a resultado
    fld qword [_cd1]
    sub esp, 8
    fstp qword [esp]
    mov eax, 5
    push eax
    call miFuncion
    add esp, 12
    mov [resultado], eax

    ; Asignación a valor
    mov eax, [resultado]
    push eax
    mov eax, 10
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [valor], eax

    ; Asignación a variableFinal
    mov eax, [valor]
    mov [variableFinal], eax

    ; Asignación a variableGlobal
    mov eax, [valor]
    mov [variableGlobal], eax
    mov eax, [resultado]
    jmp main_end
    ; Fin de bloque
main_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

    ; Fin del programa
    mov eax, 1        ; syscall: exit
    xor ebx, ebx      ; código de salida: 0
    int 0x80          ; llamada al sistema
