package util;

/**
 * Utilidad para capitalizar cadenas de texto.
 */
public class Capitalizador {
    /**
     * Capitaliza la primera letra de cada palabra en el texto
     * y convierte el resto de letras a minúsculas.
     *
     * Ejemplo: "angie milena BELLO" -> "Angie Milena Bello"
     *
     * @param texto Cadena de entrada
     * @return Texto capitalizado por palabras
     */
    public static String capitalizarPorPalabras(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        StringBuilder resultado = new StringBuilder();
        String[] palabras = texto.trim().split("\\s+");
        for (int i = 0; i < palabras.length; i++) {
            String palabra = palabras[i];
            if (palabra.length() > 1) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)))
                        .append(palabra.substring(1).toLowerCase());
            } else {
                resultado.append(palabra.toUpperCase());
            }
            if (i < palabras.length - 1) {
                resultado.append(" ");
            }
        }
        return resultado.toString();
    }
}
