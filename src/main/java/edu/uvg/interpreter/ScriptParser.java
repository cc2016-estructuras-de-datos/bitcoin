package edu.uvg.interpreter;

import edu.uvg.model.OpcodeType;
import edu.uvg.model.ScriptToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Parsea una lista de strings (tokens del script) y los convierte
 * en una lista de {@link ScriptToken} listos para ejecutar.
 *
 * Reglas de parseo:
 *   1. Si el token comienza con "OP_" se busca en OpcodeType.
 *   2. Si el token está entre "<" y ">"  dato mock (se convierte a bytes UTF-8).
 *   3. Cualquier otro token → se asume hexadecimal y se convierte a byte[].
 *
 * El ScriptParser no ejecuta nada; solo transforma strings en tokens tipados.
 * Esto separa claramente la responsabilidad de parseo de la de ejecución.
 *
 * @author Franco
 * @version 1.0
 */
public class ScriptParser {

    /**
     * Parsea el script completo (scriptSig + scriptPubKey concatenados).
     *
     * @param tokens lista de strings del script en orden de ejecución
     * @return lista de ScriptToken listos para el intérprete
     * @throws IllegalArgumentException si un token no puede ser interpretado
     */
    public List<ScriptToken> parse(List<String> tokens) {
        List<ScriptToken> result = new ArrayList<>();
        for (String raw : tokens) {
            result.add(parseToken(raw.trim()));
        }
        return result;
    }

    /**
     * Parsea un token individual.
     *
     * @param raw el string del token
     * @return el ScriptToken correspondiente
     */
    private ScriptToken parseToken(String raw) {

        // ── 1. Opcode ────────────────────────────────────────────────────
        if (raw.toUpperCase().startsWith("OP_")) {
            try {
                OpcodeType opcode = OpcodeType.valueOf(raw.toUpperCase());
                return new ScriptToken(opcode, raw);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Opcode desconocido: '" + raw + "'");
            }
        }

        // 2. Dato mock entre ángulos (<firma>, <pubKey>, etc.)
        if (raw.startsWith("<") && raw.endsWith(">")) {
            byte[] data = raw.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return new ScriptToken(data, raw);
        }

        // 3. Hexadecimal
        try {
            byte[] data = hexToBytes(raw);
            return new ScriptToken(data, raw);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Token no reconocido (no es opcode ni hexadecimal): '" + raw + "'");
        }
    }

    /**
     * Convierte un string hexadecimal a byte[].
     * Si tiene longitud impar, añade un cero al inicio.
     *
     * @param hex string hexadecimal (ej: "02aabb")
     * @return byte array correspondiente
     * @throws IllegalArgumentException si el string no es hexadecimal válido
     */
    private byte[] hexToBytes(String hex) {
        if (hex.isEmpty()) return new byte[0];

        // Quitar prefijo 0x si existe
        if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        }

        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }

        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < result.length; i++) {
            String byteStr = hex.substring(2 * i, 2 * i + 2);
            // Lanza NumberFormatException → la capturamos arriba como IllegalArgumentException
            result[i] = (byte) Integer.parseInt(byteStr, 16);
        }
        return result;
    }
}
