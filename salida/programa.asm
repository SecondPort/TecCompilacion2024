section .bss
    ; Variables globales
    x: resd 1  ; int
    y: resd 1  ; int
    z: resd 1  ; int
    d1: resd 1  ; double
    d2: resd 1  ; double
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
    prom: resd 1  ; double
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
    mov eax, 3.5
    mov dword [d1], eax
    mov eax, 3.5
    ; Inicialización de d2
    mov eax, 2.0
    mov dword [d2], eax
    mov eax, 2.0
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
    mov ebx, eax
    pop eax
    sub eax, ebx
    push eax
    mov eax, 2
    mov ebx, eax
    pop eax
    imul eax, ebx
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
    mov eax, [d1]
    push eax
    mov eax, [d2]
    mov ebx, eax
    pop eax
    add eax, ebx
    push eax
    mov eax, 2.0
    mov ebx, eax
    pop eax
    imul eax, ebx
    mov [d1], eax

    ; Asignación a d1
    mov eax, [d1]
    push eax
    mov eax, 2.5
    mov ebx, eax
    pop eax
    cdq
    idiv ebx
    mov [d1], eax
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
    mov dword [suma], eax
    ; Inicialización de maximo
    mov dword [maximo], eax
    ; Inicialización de prom
    mov dword [prom], eax
    ; Inicialización de fact_5
    mov dword [fact_5], eax
    ; Inicialización de resultado_bc
    mov dword [resultado_bc], eax
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
    mov [c2], eax
    ; Inicialización de c3
    mov eax, 122
    mov byte [c3], al
    mov eax, 122
    ; Inicialización de c4
    mov byte [c4], al
    mov eax, 0
    ; Fin de bloque
    mov eax, 0
    ret

sumar:
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
    ; Fin de bloque
    mov eax, 0
    ret

max_int:
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
    ; Fin de bloque
    jmp L15  ; saltar al final
L14:
    ; Inicio de bloque
    mov eax, [b]
    ; Fin de bloque
L15:
    ; Fin de bloque
    mov eax, 0
    ret

promedio3:
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
    mov dword [prom], eax
    mov eax, [suma]
    push eax
    mov eax, 3
    mov ebx, eax
    pop eax
    cdq
    idiv ebx
    mov eax, [prom]
    ; Fin de bloque
    mov eax, 0
    ret

contar_hasta:
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
    mov eax, 0
    ret

factorial:
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
    ; Fin de bloque
    mov eax, 0
    ret

prueba_break_continue:
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
    ; Fin de bloque
    mov eax, 0
    ret

siguiente_char:
    ; cuerpo de función
    ; Inicio de bloque
    ; Inicialización de r
    mov eax, [c]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    mov byte [r], al
    mov eax, [c]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx
    mov eax, [r]
    ; Fin de bloque
    mov eax, 0
    ret

    ; Fin del programa
    mov eax, 1        ; syscall: exit
    xor ebx, ebx      ; código de salida: 0
    int 0x80          ; llamada al sistema
