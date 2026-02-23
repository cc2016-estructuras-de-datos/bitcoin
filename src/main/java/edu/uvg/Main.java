package edu.uvg;

import edu.uvg.interpreter.ScriptInterpreter;
import edu.uvg.interpreter.ScriptParser;
import edu.uvg.model.ScriptToken;

import java.util.Arrays;
import java.util.List;

/**
 * Punto de entrada del intérprete de Bitcoin Script.
 *
 * ejecuta la prueba P2PKH requerida por la Fase 1:
 *
 *   scriptSig:    <firma>  <pubKey>
 *   scriptPubKey: OP_DUP  OP_HASH160  <pubKeyHash>  OP_EQUALVERIFY  OP_CHECKSIG
 *
 * Uso:
 *   java Main          → ejecuta la prueba P2PKH sin traza
 *   java Main --trace  → ejecuta la prueba P2PKH con traza de la pila
 *
 * nota sobre OP_HASH160 en la simulación:
 *   Dado que CryptoComparisonOpcodes.opHash160() usa SHA-256 + RIPEMD-160 reales,
 *   el pubKeyHash del scriptPubKey debe ser exactamente el HASH160 del <pubKey>
 *   que se ingresa en el scriptSig. Para esta prueba, ambos valores se generan
 *   programáticamente para que el test sea consistente.
 *
 * @author Franco
 * @version 1.0
 */
public class Main {

    public static void main(String[] args) {

        boolean traceMode = args.length > 0 && args[0].equals("--trace");

        System.out.println("══════════════════════════════════════════════════════");
        System.out.println("  Proyecto #1 — Intérprete de Bitcoin Script | UVG");
        System.out.println("  Autores: Franco, Weslly, Sipac");
        System.out.println("══════════════════════════════════════════════════════");
        System.out.println();

        // ── Prueba 1: P2PKH válido ──────────────────────────────────────
        System.out.println("▶ PRUEBA 1: P2PKH — Transacción VÁLIDA");
        System.out.println("─────────────────────────────────────────────────────");
        runP2PKH(traceMode, true);

        System.out.println();

        // ── Prueba 2: P2PKH inválido (hash incorrecto) ──────────────────
        System.out.println("▶ PRUEBA 2: P2PKH — Hash Incorrecto (debe FALLAR)");
        System.out.println("─────────────────────────────────────────────────────");
        runP2PKH(traceMode, false);

        System.out.println();

        // ── Prueba 3: Script simple con OP_1 ───────────────────────────
        System.out.println("▶ PRUEBA 3: Script simple — OP_1 (debe ser VÁLIDO)");
        System.out.println("─────────────────────────────────────────────────────");
        runSimpleScript(traceMode, Arrays.asList("OP_1"));

        System.out.println();

        // ── Prueba 4: OP_0 → debe FALLAR ──────────────────────────────
        System.out.println("▶ PRUEBA 4: Script simple — OP_0 (debe FALLAR)");
        System.out.println("─────────────────────────────────────────────────────");
        runSimpleScript(traceMode, Arrays.asList("OP_0"));

        System.out.println();

        // ── Prueba 5: OP_DUP + OP_EQUAL ────────────────────────────────
        System.out.println("▶ PRUEBA 5: OP_DUP + OP_EQUAL (debe ser VÁLIDO)");
        System.out.println("─────────────────────────────────────────────────────");
        runSimpleScript(traceMode, Arrays.asList("01", "OP_DUP", "OP_EQUAL"));
    }

    /**
     * Ejecuta la prueba principal P2PKH.
     *
     * Para que la prueba sea coherente, el pubKeyHash que va en el
     * scriptPubKey debe ser el HASH160 real de la pubKey del scriptSig.
     *
     * En esta simulación:
     *   - <pubKey> es el string "<pubKey>" convertido a bytes UTF-8.
     *   - Su HASH160 se computa en tiempo de ejecución dentro de OP_HASH160.
     *   - Por eso, en el scriptPubKey, pasamos la misma pubKey como dato
     *     auxiliar y pre-computamos el hash con el mismo algoritmo.
     *
     * @param traceMode si mostrar traza de la pila
     * @param valid     si generar un hash correcto (valid=true) o incorrecto
     */
    private static void runP2PKH(boolean traceMode, boolean valid) {

        // Datos mock del scriptSig
        String firma  = "<firma>";
        String pubKey = "<pubKey>";

        // Hash160 de la clave pública
        String pubKeyHash;
        if (valid) {
            pubKeyHash = computeHash160Hex(pubKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } else {
            // Hash incorrecto: cualquier valor distinto
            pubKeyHash = "deadbeefdeadbeefdeadbeef00000000deadbeef";
        }

        // Script completo = scriptSig + scriptPubKey
        List<String> script = Arrays.asList(
            //  scriptSig (lo que provee quien gasta)
            firma,
            pubKey,
            // scriptPubKey (el candado del output)
            "OP_DUP",
            "OP_HASH160",
            pubKeyHash,
            "OP_EQUALVERIFY",
            "OP_CHECKSIG"
        );

        System.out.println("  scriptSig:    " + firma + " " + pubKey);
        System.out.println("  scriptPubKey: OP_DUP OP_HASH160 " +
                pubKeyHash.substring(0, 8) + "... OP_EQUALVERIFY OP_CHECKSIG");
        System.out.println();

        runSimpleScript(traceMode, script);
    }

    /**
     * Parsea y ejecuta un script arbitrario.
     *
     * @param traceMode si mostrar traza
     * @param tokens    los tokens del script
     */
    private static void runSimpleScript(boolean traceMode, List<String> tokens) {
        ScriptParser      parser      = new ScriptParser();
        ScriptInterpreter interpreter = new ScriptInterpreter(traceMode);

        List<ScriptToken> parsed = parser.parse(tokens);
        boolean result = interpreter.execute(parsed);

        System.out.println();
        System.out.println("  Resultado: " +
                (result ? "✓ TRANSACCIÓN VÁLIDA" : "✗ TRANSACCIÓN INVÁLIDA"));
        System.out.println("═════════════════════════════════════════════════════");
    }

    /**
     * Computa el HASH160 (SHA-256 seguido de RIPEMD-160) de un dato.
     * Necesario para pre-calcular el pubKeyHash que va en el scriptPubKey
     * de la prueba P2PKH, de modo que sea consistente con lo que hace
     * CryptoComparisonOpcodes.opHash160().
     *
     * @param data los bytes de entrada (la clave pública mock)
     * @return el hash resultante como string hexadecimal
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
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (java.security.NoSuchAlgorithmException e) {
            // Fallback: si RIPEMD-160 no está disponible en la JVM,
            // retornamos solo el SHA-256
            throw new RuntimeException("RIPEMD-160 no disponible. " +
                    "Asegúrate de tener BouncyCastle en el classpath.", e);
        }
    }
}
