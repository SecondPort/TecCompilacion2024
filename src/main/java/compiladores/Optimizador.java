package compiladores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implementa optimizaciones sobre código intermedio en tres direcciones.
 * <p>
 * Actualmente aplica de forma iterativa:
 * <ul>
 *   <li>Propagación de constantes</li>
 *   <li>Constant folding (simplificación de expresiones constantes)</li>
 *   <li>Eliminación de subexpresiones comunes dentro de bloques básicos</li>
 * </ul>
 * Las optimizaciones se realizan hasta alcanzar un punto fijo (sin cambios).
 * </p>
 */
public class Optimizador {

    /**
     * Optimiza una lista de instrucciones de tres direcciones.
     *
     * @param instrucciones lista original de instrucciones
     * @return nueva lista con las instrucciones optimizadas
     */
    public List<Instruccion> optimizar(List<Instruccion> instrucciones) {
        List<Instruccion> optimizadas = new ArrayList<>();
        // Deep copy to avoid modifying original list objects if we want to preserve them (optional but safer)
        for (Instruccion inst : instrucciones) {
            optimizadas.add(new Instruccion(inst.op, inst.arg1, inst.arg2, inst.result));
        }

        boolean cambios = true;
        
        while (cambios) {
            cambios = false;
            Map<String, String> constantes = new HashMap<>();
            Map<String, String> expresiones = new HashMap<>(); // Key: "op,arg1,arg2", Value: resultTemp
            
            for (int i = 0; i < optimizadas.size(); i++) {
                Instruccion inst = optimizadas.get(i);
                
                // Reset analysis on basic block boundaries (labels, jumps)
                if (inst.op.equals("label") || inst.op.startsWith("if") || inst.op.equals("goto")) {
                    constantes.clear();
                    expresiones.clear();
                    continue;
                }
                
                String op = inst.op;
                String arg1 = inst.arg1;
                String arg2 = inst.arg2;
                String result = inst.result;
                
                // 1. Propagación de Constantes
                boolean localChange = false;
                if (arg1 != null && constantes.containsKey(arg1)) {
                    inst.arg1 = constantes.get(arg1);
                    arg1 = inst.arg1;
                    localChange = true;
                }
                if (arg2 != null && constantes.containsKey(arg2)) {
                    inst.arg2 = constantes.get(arg2);
                    arg2 = inst.arg2;
                    localChange = true;
                }
                if (localChange) cambios = true;
                
                // 2. Constant Folding (Carpeta de constantes)
                if (isNumeric(arg1) && (arg2 == null || isNumeric(arg2)) && !op.equals("=")) {
                   String val = calcular(op, arg1, arg2);
                   if (val != null) {
                       inst.op = "=";
                       inst.arg1 = val;
                       inst.arg2 = null;
                       // Update local vars for next steps
                       op = "=";
                       arg1 = val;
                       arg2 = null;
                       cambios = true;
                   }
                }
                
                // 3. Eliminación de Subexpresiones Comunes
                if (!op.equals("=") && arg1 != null) {
                     String key = op + "," + arg1 + "," + (arg2 == null ? "" : arg2);
                     if (expresiones.containsKey(key)) {
                         String existingVar = expresiones.get(key);
                         // Replace with assignment
                         inst.op = "=";
                         inst.arg1 = existingVar;
                         inst.arg2 = null;
                         cambios = true;
                         // Result is now an alias to existingVar, handled by const/copy propagation logic below?
                         // If result = existingVar, and existingVar is const, next pass handles it.
                         // If existingVar is variable, it's a copy.
                     } else {
                         expresiones.put(key, result);
                     }
                }

                // Update analysis maps
                // Invalidate expressions that use the modified variable 'result'
                if (result != null) {
                    Iterator<Map.Entry<String, String>> it = expresiones.entrySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next().getKey();
                        // Key format: op,arg1,arg2
                        String[] parts = key.split(",");
                        if ((parts.length > 1 && parts[1].equals(result)) || 
                            (parts.length > 2 && parts[2].equals(result))) {
                            it.remove();
                        }
                    }
                    
                    // Update constants
                    if (op.equals("=") && isNumeric(arg1)) {
                        constantes.put(result, arg1);
                    } else {
                        constantes.remove(result);
                    }
                }
            }
        }
        // Pasada final: eliminación de código muerto sobre la lista ya optimizada
        eliminarCodigoMuerto(optimizadas);
        return optimizadas;
    }

    /**
     * Elimina instrucciones cuyo resultado nunca se usa posteriormente.
     * <p>
     * Recorre la lista de instrucciones de atrás hacia adelante y mantiene
     * un conjunto de variables "vivas" (live). Si una instrucción asigna a
     * una variable que no está viva y que parece un temporal (por ejemplo,
     * nombres que comienzan con {@code "t"}), la instrucción se considera
     * código muerto y se elimina.
     * </p>
     * <p>
     * Las instrucciones de control de flujo (labels, if, goto, call, return)
     * nunca se eliminan por seguridad, aun si su resultado es un temporal.
     * </p>
     *
     * @param instrucciones lista de instrucciones ya optimizadas sobre la que
     *                      se aplicará eliminación de código muerto in-place
     */
    private void eliminarCodigoMuerto(List<Instruccion> instrucciones) {
        boolean cambio = true;
        while (cambio) {
            cambio = false;

            // Construye mapa de etiquetas para los saltos
            Map<String, Integer> etiquetas = new HashMap<>();
            for (int i = 0; i < instrucciones.size(); i++) {
                Instruccion inst = instrucciones.get(i);
                if ("label".equals(inst.op) && inst.result != null) {
                    etiquetas.put(inst.result, i);
                }
            }

            // Dataflow de liveness con CFG explícito
            List<java.util.Set<String>> in = new ArrayList<>(instrucciones.size());
            List<java.util.Set<String>> out = new ArrayList<>(instrucciones.size());
            for (int i = 0; i < instrucciones.size(); i++) {
                in.add(new java.util.HashSet<>());
                out.add(new java.util.HashSet<>());
            }

            boolean actualizado = true;
            while (actualizado) {
                actualizado = false;
                for (int i = instrucciones.size() - 1; i >= 0; i--) {
                    Instruccion inst = instrucciones.get(i);
                    java.util.Set<String> uso = obtenerUsos(inst);
                    String def = obtenerDef(inst);

                    java.util.Set<String> outActual = calcularSucesoresOut(in, etiquetas, i, instrucciones.size(), inst);
                    java.util.Set<String> inNuevo = new java.util.HashSet<>(uso);
                    java.util.Set<String> outMenosDef = new java.util.HashSet<>(outActual);
                    if (def != null) {
                        outMenosDef.remove(def);
                    }
                    inNuevo.addAll(outMenosDef);

                    if (!outActual.equals(out.get(i)) || !inNuevo.equals(in.get(i))) {
                        out.set(i, outActual);
                        in.set(i, inNuevo);
                        actualizado = true;
                    }
                }
            }

            // Pasada de eliminación usando in/out calculados
            for (int i = instrucciones.size() - 1; i >= 0; i--) {
                Instruccion inst = instrucciones.get(i);
                String def = obtenerDef(inst);
                if (def == null) {
                    continue;
                }

                boolean usado = out.get(i).contains(def);
                boolean esEliminable = instruccionPura(inst);

                if (!usado && esEliminable) {
                    instrucciones.remove(i);
                    cambio = true;
                }
            }
        }
    }

    private java.util.Set<String> obtenerUsos(Instruccion inst) {
        java.util.Set<String> usos = new java.util.HashSet<>();
        if (esVariable(inst.arg1)) usos.add(inst.arg1);
        if (esVariable(inst.arg2)) usos.add(inst.arg2);

        // Los argumentos de call vienen empaquetados en un string "a,b,c"
        if ("call".equals(inst.op) && inst.arg2 != null) {
            for (String token : inst.arg2.split(",")) {
                token = token.trim();
                if (esVariable(token)) {
                    usos.add(token);
                }
            }
        }
        return usos;
    }

    private String obtenerDef(Instruccion inst) {
        if (inst.result == null || inst.result.isEmpty()) return null;
        return esVariable(inst.result) ? inst.result : null;
    }

    private boolean esVariable(String s) {
        if (s == null || s.isEmpty()) return false;
        if (isNumeric(s)) return false;
        // Literales de char vienen con comillas simples
        if ((s.startsWith("'") && s.endsWith("'")) || (s.startsWith("\"") && s.endsWith("\""))) return false;
        // Etiquetas lX también deberían excluirse como variables
        if (s.startsWith("l")) return false;
        return true;
    }

    private boolean instruccionPura(Instruccion inst) {
        String op = inst.op;
        if (op == null) return false;

        // Instrucciones con efectos de control o potenciales efectos externos no se tocan
        if (op.equals("call") || op.equals("return") || op.equals("goto") || op.equals("if") || op.equals("label")) {
            return false;
        }
        // El resto se considera una asignación pura (\n, +, -, *, /, %, !, ==, etc.)
        return true;
    }

    private java.util.Set<String> calcularSucesoresOut(List<java.util.Set<String>> in,
                                                       Map<String, Integer> etiquetas,
                                                       int indice,
                                                       int total,
                                                       Instruccion inst) {
        java.util.Set<String> resultado = new java.util.HashSet<>();
        String op = inst.op;

        // goto lX
        if ("goto".equals(op) && inst.result != null && etiquetas.containsKey(inst.result)) {
            resultado.addAll(in.get(etiquetas.get(inst.result)));
            return resultado;
        }

        // if cond goto lX : flujo cae al siguiente y al destino
        if ("if".equals(op)) {
            if (inst.result != null && etiquetas.containsKey(inst.result)) {
                resultado.addAll(in.get(etiquetas.get(inst.result)));
            }
            if (indice + 1 < total) {
                resultado.addAll(in.get(indice + 1));
            }
            return resultado;
        }

        // return no tiene sucesores
        if ("return".equals(op)) {
            return resultado;
        }

        // resto: sucesor lineal
        if (indice + 1 < total) {
            resultado.addAll(in.get(indice + 1));
        }
        return resultado;
    }

    private boolean isNumeric(String s) {
        if (s == null) return false;
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String calcular(String op, String arg1, String arg2) {
        try {
            double v1 = Double.parseDouble(arg1);
            double v2 = (arg2 != null && !arg2.isEmpty()) ? Double.parseDouble(arg2) : 0;
            
            switch (op) {
                case "+": return format(v1 + v2);
                case "-": return format(v1 - v2);
                case "*": return format(v1 * v2);
                case "/": if (v2 == 0) return null; return format(v1 / v2);
                case "%": if (v2 == 0) return null; return format(v1 % v2);
                case "<": return (v1 < v2) ? "1" : "0";
                case ">": return (v1 > v2) ? "1" : "0";
                case "==": return (v1 == v2) ? "1" : "0";
                case "!=": return (v1 != v2) ? "1" : "0";
                default: return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    private String format(double d) {
        if (d == (long) d) return String.valueOf((long) d);
        return String.valueOf(d);
    }
}

