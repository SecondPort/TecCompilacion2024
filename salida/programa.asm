section .data
    ; Constantes inicializadas
    _cd0 dq 3.14

section .bss
    ; Variables globales
    contadorGlobal: resd 1  ; int
    valorPi: resq 1  ; double
    inicial: resb 1  ; char
    activo: resd 1  ; bool
    resultado: resd 1  ; int
    estado: resd 1  ; int
    temp: resd 1  ; int
    numeros: resd 3  ; int
    auxiliar: resd 1  ; int

; Código ensamblador generado automáticamente
; Compilador: TecCompilacion2024
; Sintaxis: NASM (x86)

bits 32
global _start

section .text
_start:
    ; Inicio del programa

sumar:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque

    ; Asignación a resultado
    mov eax, [a]
    push eax
    mov eax, [b]
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [resultado], eax

    ; Asignación a contadorGlobal
    mov eax, [contadorGlobal]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [contadorGlobal], eax
    mov eax, [resultado]
    jmp sumar_end
    ; Fin de bloque
sumar_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

main:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque

    ; Asignación a contadorGlobal
    mov eax, 0
    mov [contadorGlobal], eax

    ; Asignación a valorPi
    fld qword [_cd0]
    fstp qword [valorPi]

    ; Asignación a inicial
    mov eax, 77
    mov byte [inicial], al

    ; Asignación a numeros (arreglo)
    mov eax, 10
    mov eax, 0
    shl eax, 2
    lea ebx, [numeros]
    add ebx, eax
    mov [ebx], eax

    ; Asignación a numeros (arreglo)
    mov eax, 20
    mov eax, 1
    shl eax, 2
    lea ebx, [numeros]
    add ebx, eax
    mov [ebx], eax

    ; Asignación a numeros (arreglo)
    mov eax, 30
    mov eax, 2
    shl eax, 2
    lea ebx, [numeros]
    add ebx, eax
    mov [ebx], eax

    ; Asignación a temp
    mov eax, 0
    shl eax, 2
    lea ebx, [numeros]
    add ebx, eax
    mov eax, [ebx]
    push eax
    mov eax, 1
    shl eax, 2
    lea ebx, [numeros]
    add ebx, eax
    mov eax, [ebx]
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [temp], eax

    ; Asignación a temp
    mov eax, [temp]
    push eax
    mov eax, 2
    mov ebx, eax
    pop eax
    imul eax, ebx
    mov [temp], eax

    ; Asignación a temp
    mov eax, [temp]
    push eax
    mov eax, 3
    mov ebx, eax
    pop eax
    cdq
    idiv ebx
    mov [temp], eax

    ; Asignación a temp
    mov eax, [temp]
    push eax
    mov eax, 5
    mov ebx, eax
    pop eax
    cdq
    idiv ebx
    mov eax, edx
    mov [temp], eax

    ; Asignación a estado
    mov eax, 5
    push eax
    mov eax, [temp]
    push eax
    call sumar
    add esp, 8
    mov [estado], eax

    ; Asignación a contadorGlobal
    mov eax, [estado]
    mov [contadorGlobal], eax

    ; Asignación a valorPi
    mov eax, [temp]
    push eax
    fild dword [esp]
    add esp, 4
    fstp qword [valorPi]

    ; Asignación a inicial
    mov eax, 88
    mov byte [inicial], al

    ; Estructura IF
    mov eax, [estado]
    push eax
    mov eax, 0
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    setg al
    je L0  ; saltar si falso
    ; Inicio de bloque

    ; Asignación a auxiliar
    mov eax, [estado]
    push eax
    mov eax, 10
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [auxiliar], eax

    ; Asignación a estado
    mov eax, [auxiliar]
    mov [estado], eax
    ; Fin de bloque
    jmp L1  ; saltar al final
L0:
L1:
    mov eax, [estado]
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
