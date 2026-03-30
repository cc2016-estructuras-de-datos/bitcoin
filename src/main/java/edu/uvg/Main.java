package edu.uvg;

import edu.uvg.interpreter.ScriptInterpreter;
import edu.uvg.interpreter.ScriptParser;
import edu.uvg.model.ScriptToken;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.Arrays;
import java.util.List;

/**
 * Punto de entrada del intérprete de Bitcoin Script — Fase 2.
 *
 * Demostraciones obligatorias:
 *   a. P2PKH correcto e incorrecto
 *   b. Condicional con OP_IF / OP_ELSE / OP_ENDIF (anidado)
 *   c. (Avanzado) Multisig 2-de-3 con OP_CHECKMULTISIG (mock)
 *
 * Uso:
 *   java Main          → ejecuta todas las demostraciones sin traza
 *   java Main --trace  → ejecuta todas las demostraciones con traza de pila
 */
public class Main {

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        boolean traceMode = args.length > 0 && args[0].equals("--trace");

        printHeader();

        // ══════════════════════════════════════════════════════
        //  BLOQUE A: P2PKH
        // ══════════════════════════════════════════════════════

        section("A", "P2PKH — Transacción VÁLIDA (hash correcto)");
        runP2PKH(traceMode, true);

        section("A", "P2PKH — Transacción INVÁLIDA (hash incorrecto)");
        runP2PKH(traceMode, false);

        // ══════════════════════════════════════════════════════
        //  BLOQUE B: Condicionales
        // ══════════════════════════════════════════════════════

        section("B", "OP_IF simple — condición TRUE (debe ser VÁLIDO)");
        runSimpleScript(traceMode, Arrays.asList(
            "OP_1", "OP_IF", "OP_1", "OP_ENDIF"
        ));

        section("B", "OP_IF / OP_ELSE — condición FALSE (rama ELSE, debe ser VÁLIDO)");
        runSimpleScript(traceMode, Arrays.asList(
            "OP_0", "OP_IF", "OP_0", "OP_ELSE", "OP_1", "OP_ENDIF"
        ));

        section("B", "OP_IF anidado — outer TRUE, inner FALSE → rama ELSE inner (debe ser VÁLIDO)");
        runSimpleScript(traceMode, Arrays.asList(
            "OP_1", "OP_IF",
                "OP_0", "OP_IF",
                    "OP_0",
                "OP_ELSE",
                    "OP_1",
                "OP_ENDIF",
            "OP_ENDIF"
        ));

        section("B", "OP_NOTIF — condición FALSE entra al bloque (debe ser VÁLIDO)");
        runSimpleScript(traceMode, Arrays.asList(
            "OP_0", "OP_NOTIF", "OP_1", "OP_ENDIF"
        ));

        section("B", "Condicional con aritmética — 5 > 3 → OP_IF (debe ser VÁLIDO)");
        runSimpleScript(traceMode, Arrays.asList(
            "05", "03", "OP_GREATERTHAN",
            "OP_IF", "OP_1", "OP_ELSE", "OP_0", "OP_ENDIF"
        ));

        // ══════════════════════════════════════════════════════
        //  BLOQUE C: Multisig 2-de-3 (avanzado)
        // ══════════════════════════════════════════════════════

        section("C", "Multisig 2-de-3 — 2 firmas VÁLIDAS (debe ser VÁLIDO)");
        runMultisig(traceMode, true, false);

        section("C", "Multisig 2-de-3 — 1 firma vacía (debe FALLAR)");
        runMultisig(traceMode, false, false);

        section("C", "Multisig 2-de-3 — todas las claves vacías (debe FALLAR)");
        runMultisig(traceMode, true, true);

        // ══════════════════════════════════════════════════════
        //  EXTRAS: opcodes nuevos Fase 2
        // ══════════════════════════════════════════════════════

        section("Extra", "OP_ADD — 3 + 4 = 7 (no cero → debe ser VÁLIDO)");
        runSimpleScript(traceMode, Arrays.asList("03", "04", "OP_ADD"));

        section("Extra", "OP_SUB — 5 - 5 = 0 (cero → debe FALLAR)");
        runSimpleScript(traceMode, Arrays.asList("05", "05", "OP_SUB"));

        section("Extra", "OP_NOT — NOT(0) = 1 (debe ser VÁLIDO)");
        runSimpleScript(traceMode, Arrays.asList("OP_0", "OP_NOT"));

        section("Extra", "OP_SHA256 — hash de <data> (no vacío → debe ser VÁLIDO)");
        runSimpleScript(traceMode, Arrays.asList("<data>", "OP_SHA256"));

        section("Extra", "OP_HASH256 — doble SHA256 de <data> (debe ser VÁLIDO)");
        runSimpleScript(traceMode, Arrays.asList("<data>", "OP_HASH256"));
    }

    // ─────────────────────────────────────────────────────────────────
    //  Demostraciones
    // ─────────────────────────────────────────────────────────────────

    /**
     * Ejecuta la prueba P2PKH.
     * valid=true  → usa el hash160 real de la pubKey (transacción válida).
     * valid=false → usa un hash incorrecto (transacción inválida).
     */
    private static void runP2PKH(boolean traceMode, boolean valid) {
        String firma  = "<firma>";
        String pubKey = "<pubKey>";

        String pubKeyHash;
        if (valid) {
            pubKeyHash = computeHash160Hex(
                pubKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } else {
            pubKeyHash = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeef";
        }

        System.out.println("  scriptSig:    " + firma + " " + pubKey);
        System.out.println("  scriptPubKey: OP_DUP OP_HASH160 "
                + pubKeyHash.substring(0, 8) + "... OP_EQUALVERIFY OP_CHECKSIG");
        System.out.println();

        runSimpleScript(traceMode, Arrays.asList(
            firma, pubKey,
            "OP_DUP", "OP_HASH160", pubKeyHash, "OP_EQUALVERIFY", "OP_CHECKSIG"
        ));
    }

    /**
     * Ejecuta la demostración de Multisig 2-de-3.
     *
     * @param traceMode     si mostrar traza de pila
     * @param validSigs     si las firmas son válidas (no vacías)
     * @param emptyKeys     si las claves públicas son vacías (fuerza fallo)
     */
    private static void runMultisig(boolean traceMode,
                                     boolean validSigs,
                                     boolean emptyKeys) {

        byte[] sig1 = validSigs ? new byte[]{0x30, 0x44} : new byte[0];
        byte[] sig2 = validSigs ? new byte[]{0x30, 0x45} : new byte[0];
        byte[] key1 = emptyKeys ? new byte[0] : new byte[]{0x02, 0x01};
        byte[] key2 = emptyKeys ? new byte[0] : new byte[]{0x02, 0x02};
        byte[] key3 = emptyKeys ? new byte[0] : new byte[]{0x02, 0x03};

        String sig1Hex = bytesToHex(sig1);
        String sig2Hex = bytesToHex(sig2);
        String key1Hex = bytesToHex(key1);
        String key2Hex = bytesToHex(key2);
        String key3Hex = bytesToHex(key3);

        System.out.println("  scriptSig:    OP_0 <firma1> <firma2>");
        System.out.println("  scriptPubKey: OP_2 <key1> <key2> <key3> OP_3 OP_CHECKMULTISIG");
        System.out.println();

        // scriptSig: OP_0 firma1 firma2
        // scriptPubKey: OP_2 key1 key2 key3 OP_3 OP_CHECKMULTISIG
        runSimpleScript(traceMode, Arrays.asList(
            "OP_0",
            sig1Hex.isEmpty() ? "00" : sig1Hex,
            sig2Hex.isEmpty() ? "00" : sig2Hex,
            "OP_2",
            key1Hex.isEmpty() ? "00" : key1Hex,
            key2Hex.isEmpty() ? "00" : key2Hex,
            key3Hex.isEmpty() ? "00" : key3Hex,
            "OP_3",
            "OP_CHECKMULTISIG"
        ));
    }

    /**
     * Parsea y ejecuta un script arbitrario mostrando resultado.
     */
    private static void runSimpleScript(boolean traceMode, List<String> tokens) {
        ScriptParser      parser      = new ScriptParser();
        ScriptInterpreter interpreter = new ScriptInterpreter(traceMode);
        List<ScriptToken> parsed      = parser.parse(tokens);
        boolean result = interpreter.execute(parsed);

        System.out.println();
        System.out.println("  Resultado: " +
                (result ? "✓ TRANSACCIÓN VÁLIDA" : "✗ TRANSACCIÓN INVÁLIDA"));
        System.out.println("═════════════════════════════════════════════════════");
    }

    // ─────────────────────────────────────────────────────────────────
    //  Utilidades
    // ─────────────────────────────────────────────────────────────────

    /**
     * Imprime la cabecera de las demostraciones en consola.
     */
    private static void printHeader() {
        System.out.println("══════════════════════════════════════════════════════");
        System.out.println("  Proyecto #1 — Intérprete de Bitcoin Script | UVG");
        System.out.println("  Fase 2 — Intérprete completo");
        System.out.println("  Autores: Franco, Weslly, Sipac");
        System.out.println("══════════════════════════════════════════════════════");
        System.out.println();
    }

    /**
     * Imprime un encabezado de sección para cada demostración.
     *
     * @param block identificador de bloque (A/B/C/Extra)
     * @param title descripción breve del caso de prueba
     */
    private static void section(String block, String title) {
        System.out.println();
        System.out.println("▶ [" + block + "] " + title);
        System.out.println("─────────────────────────────────────────────────────");
    }

    /**
     * Calcula el HASH160 en formato hexadecimal (RIPEMD160(SHA256(data))).
     *
     * @param data bytes de entrada
     * @return hash160 en hex
     */
    private static String computeHash160Hex(byte[] data) {
        try {
            java.security.MessageDigest sha256 =
                    java.security.MessageDigest.getInstance("SHA-256");
            byte[] shaResult = sha256.digest(data);
            java.security.MessageDigest ripemd160 =
                    java.security.MessageDigest.getInstance("RIPEMD160");
            byte[] hash = ripemd160.digest(shaResult);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("RIPEMD-160 no disponible.", e);
        }
    }

    /**
     * Convierte un arreglo de bytes a su representación hexadecimal minúscula.
     *
     * @param bytes datos de entrada
     * @return texto hexadecimal ("" si bytes está vacío)
     */
    private static String bytesToHex(byte[] bytes) {
        if (bytes.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}