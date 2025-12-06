section .bss
    ; Variables globales
    siguiente_char: resb 1  ; char
    c: resb 1  ; char
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
    mov dword [x], 5
    ; Inicialización de y
    mov dword [y], 10
    ; Inicialización de d1
    mov dword [d1], 3
    ; Inicialización de d2
    mov dword [d2], 2
    ; Inicialización de c1 (char)
    mov byte [c1], 65

    ; Asignación a z
    mov eax, [x]
    push eax
    mov eax, [y]
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [z], eax

    ; Asignación a z
    mov eax, [z]
    push eax
    mov eax, 3
    push eax
    mov eax, 2
    mov ebx, eax
    pop eax
    imul eax, ebx  ; multiplicación
    mov ebx, eax
    pop eax
    sub eax, ebx  ; resta
    mov [z], eax

    ; Asignación a z
    mov eax, [z]
    push eax
    mov eax, 3
    mov ebx, eax
    pop eax
    cdq            ; extender signo para división
    idiv ebx       ; división
    mov [z], eax

    ; Asignación a d1
    mov eax, [d1]
    push eax
    mov eax, [d2]
    push eax
    mov eax, 2
    mov ebx, eax
    pop eax
    imul eax, ebx  ; multiplicación
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [d1], eax

    ; Asignación a d1
    mov eax, [d1]
    push eax
    mov eax, 2
    mov ebx, eax
    pop eax
    cdq            ; extender signo para división
    idiv ebx       ; división
    mov [d1], eax

    ; Estructura IF
    mov eax, [condicion4]
    push eax
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L0  ; saltar si falso
    ; Inicio de bloque

    ; Asignación a z
    mov eax, [z]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
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
    sub eax, ebx  ; resta
    mov [z], eax
    ; Fin de bloque
L1:

    ; Estructura IF
    mov eax, [condicion3]
    push eax
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L2  ; saltar si falso
    ; Inicio de bloque

    ; Asignación a z
    mov eax, 1
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
    add eax, ebx  ; suma
    mov [z], eax
    ; Fin de bloque
L3:
    push eax  ; pasar argumento
    call sumar
    add esp, 4  ; limpiar argumento
    push eax  ; pasar argumento
    call max_int
    add esp, 4  ; limpiar argumento
    push eax  ; pasar argumento
    call promedio3
    add esp, 4  ; limpiar argumento
    push eax  ; pasar argumento
    call factorial
    add esp, 4  ; limpiar argumento
    push eax  ; pasar argumento
    call prueba_break_continue
    add esp, 4  ; limpiar argumento
    ; Inicialización de contador
    mov dword [contador], 0

    ; Estructura WHILE
L4:
    mov eax, [contador]
    push eax
    mov eax, 5
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L5  ; salir si falso
    ; Inicio de bloque

    ; Asignación a contador
    mov eax, [contador]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [contador], eax
    ; Fin de bloque
    jmp L4  ; repetir bucle
L5:
    ; Inicialización de suma_for
    mov dword [suma_for], 0

    ; Estructura FOR
L6:
    mov eax, [i]
    push eax
    mov eax, 5
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L7  ; salir si falso
    ; Inicio de bloque
    ; Inicio de bloque

    ; Asignación a suma_for
    mov eax, [suma_for]
    push eax
    mov eax, [i]
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [suma_for], eax
    ; Fin de bloque
    ; Inicialización de suma_pares
    mov dword [suma_pares], 0

    ; Estructura FOR
L8:
    mov eax, [i]
    push eax
    mov eax, 10
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L9  ; salir si falso
    ; Inicio de bloque
    ; Inicio de bloque

    ; Estructura IF
    mov eax, [i]
    push eax
    mov eax, 2
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
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
    cmp eax, ebx  ; comparación
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
    add eax, ebx  ; suma
    mov [suma_pares], eax
    ; Fin de bloque

    ; Asignación a c2
    mov eax, [siguiente_char]
    mov [c2], eax
    ; Inicialización de c3 (char)
    mov byte [c3], 122
    push eax  ; pasar argumento
    call siguiente_char
    add esp, 4  ; limpiar argumento
    mov eax, 0
    ; Fin de bloque
    ; Actualización por expresión
    mov eax, [i]
    jmp L8  ; repetir bucle
L9:

sumar:
    ; cuerpo de función
    ; Inicio de bloque
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
    cmp eax, ebx  ; comparación
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
    mov eax, [prom]
    ; Fin de bloque
    mov eax, 0
    ret

contar_hasta:
    ; cuerpo de función
    ; Inicio de bloque
    ; Inicialización de i
    mov dword [i], 0

    ; Estructura WHILE
L16:
    mov eax, [i]
    push eax
    mov eax, [n]
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L17  ; salir si falso
    ; Inicio de bloque

    ; Asignación a i
    mov eax, [i]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
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
    mov dword [res], 1

    ; Estructura FOR
L18:
    mov eax, [i]
    push eax
    mov eax, [n]
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L19  ; salir si falso
    ; Inicio de bloque
    ; Inicio de bloque

    ; Asignación a res
    mov eax, [res]
    push eax
    mov eax, [i]
    mov ebx, eax
    pop eax
    imul eax, ebx  ; multiplicación
    mov [res], eax
    ; Fin de bloque
    mov eax, [res]
    ; Fin de bloque
    ; Actualización por expresión
    mov eax, [i]
    jmp L18  ; repetir bucle
L19:

prueba_break_continue:
    ; cuerpo de función
    ; Inicio de bloque
    ; Inicialización de i
    mov dword [i], 0
    ; Inicialización de suma
    mov dword [suma], 0

    ; Estructura WHILE
L20:
    mov eax, [i]
    push eax
    mov eax, [limite]
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
    je L21  ; salir si falso
    ; Inicio de bloque

    ; Asignación a i
    mov eax, [i]
    push eax
    mov eax, 1
    mov ebx, eax
    pop eax
    add eax, ebx  ; suma
    mov [i], eax

    ; Estructura IF
    mov eax, [i]
    push eax
    mov eax, 3
    mov ebx, eax
    pop eax
    cmp eax, ebx  ; comparación
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
    cmp eax, ebx  ; comparación
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
    add eax, ebx  ; suma
    mov [suma], eax
    ; Fin de bloque
    jmp L20  ; repetir bucle
L21:
    mov eax, [suma]
    ; Fin de bloque
    mov eax, 0
    ret
    ; Inicio de bloque
    mov eax, [r]
    ; Fin de bloque
    ; Fin de bloque
    mov eax, 0
    ret
    ; Fin de bloque
    ; Actualización por expresión
    mov eax, [i]
    jmp L6  ; repetir bucle
L7:
    ; Fin de bloque
    mov eax, 0
    ret

    ; Fin del programa
    mov eax, 1        ; syscall: exit
    xor ebx, ebx      ; código de salida: 0
    int 0x80          ; llamada al sistema
