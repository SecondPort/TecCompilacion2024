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
        return optimizadas;
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

