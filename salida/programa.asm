section .data
    ; Constantes inicializadas
    _cd0 dq 3.5
    _cd1 dq 3.5
    _cd2 dq 2.0
    _cd3 dq 2.0
    _cd4 dq 2.0
    _cd5 dq 2.5

section .bss
    ; Variables globales
    x: resd 1  ; int
    y: resd 1  ; int
    z: resd 1  ; int
    d1: resq 1  ; double
    d2: resq 1  ; double
    c1: resb 1  ; char
    c2: resb 1  ; char
    condicion1: resd 1  ; int
    condicion2: resd 1  ; int
    condicion3: resd 1  ; int
    condicion4: resd 1  ; int
    condicion5: resd 1  ; int
    condicion6: resd 1  ; int
    suma: resd 1  ; int
    maximo: resd 1  ; int
    prom: resq 1  ; double
    fact_5: resd 1  ; int
    resultado_bc: resd 1  ; int
    contador: resd 1  ; int
    suma_for: resd 1  ; int
    i: resd 1  ; int
    suma_pares: resd 1  ; int
    c3: resb 1  ; char
    c4: resb 1  ; char
    r: resd 1  ; int
    res: resd 1  ; int

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
    ; Inicialización de x
    mov eax, 5
    mov dword [x], eax
    mov eax, 5
    ; Inicialización de y
    mov eax, 10
    mov dword [y], eax
    mov eax, 10
    ; Inicialización de d1
    fld qword [_cd0]
    fstp qword [d1]
    fld qword [_cd1]
    ; Inicialización de d2
    fld qword [_cd2]
    fstp qword [d2]
    fld qword [_cd3]
    ; Inicialización de c1
    mov eax, 65
    mov byte [c1], al
    mov eax, 65

    ; Asignación a z
    mov eax, [x]
    push eax
    mov eax, [y]
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [z], eax

    ; Asignación a z
    mov eax, [z]
    push eax
    mov eax, 3
    push eax
    mov eax, 2
    mov ebx, eax
    pop eax
    imul eax, ebx
    mov ebx, eax
    pop eax
    sub eax, ebx
    mov [z], eax

    ; Asignación a z
    mov eax, [z]
    push eax
    mov eax, 3
    mov ebx, eax
    pop eax
    cdq
    idiv ebx
    mov [z], eax

    ; Asignación a d1
    fld qword [d1]
    sub esp, 8
    fstp qword [esp]
    fld qword [d2]
    sub esp, 8
    fstp qword [esp]
    fld qword [_cd4]
    fld qword [esp]
    add esp, 8
    fmulp st1, st0
    fld qword [esp]
    add esp, 8
    faddp st1, st0
    fstp qword [d1]

    ; Asignación a d1
    fld qword [d1]
    sub esp, 8
    fstp qword [esp]
    fld qword [_cd5]
    fld qword [esp]
    add esp, 8
    fxch st1
    fdivp st1, st0
    fstp qword [d1]
    ; Inicialización de condicion1
    mov eax, [x]
    push eax
    mov eax, [y]
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    setl al
    mov dword [condicion1], eax
    mov eax, [x]
    push eax
    mov eax, [y]
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    setl al
    ; Inicialización de condicion2
    mov eax, [z]
    push eax
    mov eax, 3
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    sete al
    mov dword [condicion2], eax
    mov eax, [z]
    push eax
    mov eax, 3
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    sete al
    ; Inicialización de condicion3
    mov eax, [x]
    push eax
    mov eax, [y]
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    setg al
    mov dword [condicion3], eax
    mov eax, [x]
    push eax
    mov eax, [y]
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    setg al
    ; Inicialización de condicion4
    mov eax, [condicion1]
    push eax
    mov eax, [condicion2]
    mov ebx, eax
    pop eax
    cmp eax, 0
    setne al
    movzx eax, al
    cmp ebx, 0
    setne bl
    movzx ebx, bl
    and eax, ebx
    mov dword [condicion4], eax
    mov eax, [condicion1]
    push eax
    mov eax, [condicion2]
    mov ebx, eax
    pop eax
    cmp eax, 0
    setne al
    movzx eax, al
    cmp ebx, 0
    setne bl
    movzx ebx, bl
    and eax, ebx
    ; Inicialización de condicion5
    mov eax, [condicion3]
    push eax
    mov eax, [condicion2]
    mov ebx, eax
    pop eax
    cmp eax, 0
    setne al
    movzx eax, al
    cmp ebx, 0
    setne bl
    movzx ebx, bl
    or eax, ebx
    mov dword [condicion5], eax
    mov eax, [condicion3]
    push eax
    mov eax, [condicion2]
    mov ebx, eax
    pop eax
    cmp eax, 0
    setne al
    movzx eax, al
    cmp ebx, 0
    setne bl
    movzx ebx, bl
    or eax, ebx
    ; Inicialización de condicion6
    mov eax, [condicion3]
    cmp eax, 0
    mov eax, 0
    sete al
    mov dword [condicion6], eax
    mov eax, [condicion3]
    cmp eax, 0
    mov eax, 0
    sete al

    ; Estructura IF
    mov eax, [condicion4]
    je L0  ; saltar si falso
    ; Inicio de bloque

    ; Asignación a z
    mov eax, [z]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [z], eax
    ; Fin de bloque
    jmp L1  ; saltar al final
L0:
    ; Inicio de bloque

    ; Asignación a z
    mov eax, [z]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    sub eax, ebx
    mov [z], eax
    ; Fin de bloque
L1:

    ; Estructura IF
    mov eax, [condicion3]
    je L2  ; saltar si falso
    ; Inicio de bloque

    ; Asignación a z
    mov eax, 1
    neg eax
    mov [z], eax
    ; Fin de bloque
    jmp L3  ; saltar al final
L2:
    ; Inicio de bloque

    ; Asignación a z
    mov eax, [z]
    push eax
    mov eax, 2
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [z], eax
    ; Fin de bloque
L3:
    ; Inicialización de suma
    mov eax, [y]
    push eax
    mov eax, [x]
    push eax
    call sumar
    add esp, 8
    mov dword [suma], eax
    mov eax, [y]
    push eax
    mov eax, [x]
    push eax
    call sumar
    add esp, 8
    ; Inicialización de maximo
    mov eax, [y]
    push eax
    mov eax, [x]
    push eax
    call max_int
    add esp, 8
    mov dword [maximo], eax
    mov eax, [y]
    push eax
    mov eax, [x]
    push eax
    call max_int
    add esp, 8
    ; Inicialización de prom
    mov eax, 5
    push eax
    mov eax, 4
    push eax
    mov eax, 3
    push eax
    call promedio3
    add esp, 12
    fstp qword [prom]
    mov eax, 5
    push eax
    mov eax, 4
    push eax
    mov eax, 3
    push eax
    call promedio3
    add esp, 12
    ; Inicialización de fact_5
    mov eax, 5
    push eax
    call factorial
    add esp, 4
    mov dword [fact_5], eax
    mov eax, 5
    push eax
    call factorial
    add esp, 4
    ; Inicialización de resultado_bc
    mov eax, 10
    push eax
    call prueba_break_continue
    add esp, 4
    mov dword [resultado_bc], eax
    mov eax, 10
    push eax
    call prueba_break_continue
    add esp, 4
    ; Inicialización de contador
    mov eax, 0
    mov dword [contador], eax
    mov eax, 0

    ; Estructura WHILE
L4:
    mov eax, [contador]
    push eax
    mov eax, 5
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    setl al
    je L5  ; salir si falso
    ; Inicio de bloque

    ; Asignación a contador
    mov eax, [contador]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [contador], eax
    ; Fin de bloque
    jmp L4  ; repetir bucle
L5:
    ; Inicialización de suma_for
    mov eax, 0
    mov dword [suma_for], eax
    mov eax, 0

    ; Estructura FOR
L6:
    mov eax, [i]
    push eax
    mov eax, 5
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    setl al
    je L7  ; salir si falso
    ; Inicio de bloque

    ; Asignación a suma_for
    mov eax, [suma_for]
    push eax
    mov eax, [i]
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [suma_for], eax
    ; Fin de bloque
    ; Actualización por expresión
    mov eax, [i]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    jmp L6  ; repetir bucle
L7:
    ; Inicialización de suma_pares
    mov eax, 0
    mov dword [suma_pares], eax
    mov eax, 0

    ; Estructura FOR
L8:
    mov eax, [i]
    push eax
    mov eax, 10
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    setl al
    je L9  ; salir si falso
    ; Inicio de bloque

    ; Estructura IF
    mov eax, [i]
    push eax
    mov eax, 2
    mov ebx, eax
    pop eax
    cdq
    idiv ebx
    mov eax, edx
    push eax
    mov eax, 0
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    setne al
    je L10  ; saltar si falso
    ; Inicio de bloque
    ; continue
    jmp L8
    ; Fin de bloque
    jmp L11  ; saltar al final
L10:
L11:

    ; Estructura IF
    mov eax, [i]
    push eax
    mov eax, 6
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    setg al
    je L12  ; saltar si falso
    ; Inicio de bloque
    ; break
    jmp L9
    ; Fin de bloque
    jmp L13  ; saltar al final
L12:
L13:

    ; Asignación a suma_pares
    mov eax, [suma_pares]
    push eax
    mov eax, [i]
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [suma_pares], eax
    ; Fin de bloque
    ; Actualización por expresión
    mov eax, [i]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    jmp L8  ; repetir bucle
L9:

    ; Asignación a c2
    movzx eax, byte [c1]
    push eax
    call siguiente_char
    add esp, 4
    mov byte [c2], al
    ; Inicialización de c3
    mov eax, 122
    mov byte [c3], al
    mov eax, 122
    ; Inicialización de c4
    movzx eax, byte [c3]
    push eax
    call siguiente_char
    add esp, 4
    mov byte [c4], al
    movzx eax, byte [c3]
    push eax
    call siguiente_char
    add esp, 4
    mov eax, 0
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
    ; Inicialización de r
    mov eax, [a]
    push eax
    mov eax, [b]
    mov ebx, eax
    pop eax
    add eax, ebx
    mov dword [r], eax
    mov eax, [a]
    push eax
    mov eax, [b]
    mov ebx, eax
    pop eax
    add eax, ebx
    mov eax, [r]
    jmp sumar_end
    ; Fin de bloque
sumar_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

max_int:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque

    ; Estructura IF
    mov eax, [a]
    push eax
    mov eax, [b]
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    setg al
    je L14  ; saltar si falso
    ; Inicio de bloque
    mov eax, [a]
    jmp max_int_end
    ; Fin de bloque
    jmp L15  ; saltar al final
L14:
    ; Inicio de bloque
    mov eax, [b]
    jmp max_int_end
    ; Fin de bloque
L15:
    ; Fin de bloque
max_int_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

promedio3:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque
    ; Inicialización de suma
    mov eax, [a]
    push eax
    mov eax, [b]
    mov ebx, eax
    pop eax
    add eax, ebx
    push eax
    mov eax, [c]
    mov ebx, eax
    pop eax
    add eax, ebx
    mov dword [suma], eax
    mov eax, [a]
    push eax
    mov eax, [b]
    mov ebx, eax
    pop eax
    add eax, ebx
    push eax
    mov eax, [c]
    mov ebx, eax
    pop eax
    add eax, ebx
    ; Inicialización de prom
    mov eax, [suma]
    push eax
    mov eax, 3
    mov ebx, eax
    pop eax
    cdq
    idiv ebx
    push eax
    fild dword [esp]
    add esp, 4
    fstp qword [prom]
    mov eax, [suma]
    push eax
    mov eax, 3
    mov ebx, eax
    pop eax
    cdq
    idiv ebx
    fld qword [prom]
    jmp promedio3_end
    ; Fin de bloque
promedio3_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

contar_hasta:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque
    ; Inicialización de i
    mov eax, 0
    mov dword [i], eax
    mov eax, 0

    ; Estructura WHILE
L16:
    mov eax, [i]
    push eax
    mov eax, [n]
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    setl al
    je L17  ; salir si falso
    ; Inicio de bloque

    ; Asignación a i
    mov eax, [i]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [i], eax
    ; Fin de bloque
    jmp L16  ; repetir bucle
L17:
    ; Fin de bloque
contar_hasta_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

factorial:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque
    ; Inicialización de res
    mov eax, 1
    mov dword [res], eax
    mov eax, 1

    ; Estructura FOR
L18:
    mov eax, [i]
    push eax
    mov eax, [n]
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    setle al
    je L19  ; salir si falso
    ; Inicio de bloque

    ; Asignación a res
    mov eax, [res]
    push eax
    mov eax, [i]
    mov ebx, eax
    pop eax
    imul eax, ebx
    mov [res], eax
    ; Fin de bloque
    ; Actualización por expresión
    mov eax, [i]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    jmp L18  ; repetir bucle
L19:
    mov eax, [res]
    jmp factorial_end
    ; Fin de bloque
factorial_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

prueba_break_continue:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque
    ; Inicialización de i
    mov eax, 0
    mov dword [i], eax
    mov eax, 0
    ; Inicialización de suma
    mov eax, 0
    mov dword [suma], eax
    mov eax, 0

    ; Estructura WHILE
L20:
    mov eax, [i]
    push eax
    mov eax, [limite]
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    setl al
    je L21  ; salir si falso
    ; Inicio de bloque

    ; Asignación a i
    mov eax, [i]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [i], eax

    ; Estructura IF
    mov eax, [i]
    push eax
    mov eax, 3
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    sete al
    je L22  ; saltar si falso
    ; Inicio de bloque
    ; continue
    jmp L20
    ; Fin de bloque
    jmp L23  ; saltar al final
L22:
L23:

    ; Estructura IF
    mov eax, [i]
    push eax
    mov eax, 7
    mov ebx, eax
    pop eax
    cmp eax, ebx
    mov eax, 0
    sete al
    je L24  ; saltar si falso
    ; Inicio de bloque
    ; break
    jmp L21
    ; Fin de bloque
    jmp L25  ; saltar al final
L24:
L25:

    ; Asignación a suma
    mov eax, [suma]
    push eax
    mov eax, [i]
    mov ebx, eax
    pop eax
    add eax, ebx
    mov [suma], eax
    ; Fin de bloque
    jmp L20  ; repetir bucle
L21:
    mov eax, [suma]
    jmp prueba_break_continue_end
    ; Fin de bloque
prueba_break_continue_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

siguiente_char:
    push ebp
    mov ebp, esp
    ; cuerpo de función
    ; Inicio de bloque
    ; Inicialización de r
    movzx eax, byte [c]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    mov byte [r], al
    movzx eax, byte [c]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    mov eax, [r]
    jmp siguiente_char_end
    ; Fin de bloque
siguiente_char_end:
    mov esp, ebp
    pop ebp
    mov eax, 0
    ret

    ; Fin del programa
    mov eax, 1        ; syscall: exit
    xor ebx, ebx      ; código de salida: 0
    int 0x80          ; llamada al sistema
