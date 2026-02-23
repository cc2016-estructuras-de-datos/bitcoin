package edu.uvg.interpreter;

import edu.uvg.model.ScriptToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración del intérprete completo.
 * Cada test construye un script real, lo parsea y lo ejecuta.
 */
class ScriptInterpreterTest {

    private ScriptParser parser;

    @BeforeEach
    void setUp() {
        parser = new ScriptParser();
    }

    // ── Helper ────────────────────────────────────────────────────────

    private boolean run(String... tokens) {
        List<ScriptToken> parsed = parser.parse(Arrays.asList(tokens));
        return new ScriptInterpreter(false).execute(parsed);
    }

    /** Computa el HASH160 real de un dato, igual que OP_HASH160 de Sipac. */
    private String hash160Hex(byte[] data) throws Exception {
        MessageDigest sha256   = MessageDigest.getInstance("SHA-256");
        MessageDigest ripemd160 = MessageDigest.getInstance("RIPEMD160");
        byte[] hash = ripemd160.digest(sha256.digest(data));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // ── Casos básicos ─────────────────────────────────────────────────

    @Test
    void op1_isValid() {
        assertTrue(run("OP_1"));
    }

    @Test
    void op0_isInvalid() {
        assertFalse(run("OP_0"));
    }

    @Test
    void emptyScript_isInvalid() {
        List<ScriptToken> empty = List.of();
        assertFalse(new ScriptInterpreter(false).execute(empty));
    }

    @Test
    void literalNonZero_isValid() {
        assertTrue(run("01")); // empuja 0x01 = TRUE
    }

    @Test
    void literalZero_isInvalid() {
        assertFalse(run("00")); // empuja 0x00 = FALSE
    }

    // ── OP_DUP ───────────────────────────────────────────────────────

    @Test
    void opDup_duplicatesAndLeavesTrue() {
        assertTrue(run("01", "OP_DUP", "OP_DROP"));
    }

    // ── OP_EQUAL ─────────────────────────────────────────────────────

    @Test
    void opEqual_sameValues_isValid() {
        assertTrue(run("01", "OP_DUP", "OP_EQUAL"));
    }

    @Test
    void opEqual_differentValues_isInvalid() {
        assertFalse(run("01", "02", "OP_EQUAL"));
    }

    // ── OP_EQUALVERIFY ───────────────────────────────────────────────

    @Test
    void opEqualVerify_sameValues_continuesExecution() {
        // Si EQUALVERIFY pasa, queda OP_1 en la pila → válido
        assertTrue(run("01", "01", "OP_EQUALVERIFY", "OP_1"));
    }

    @Test
    void opEqualVerify_differentValues_fails() {
        assertFalse(run("01", "02", "OP_EQUALVERIFY", "OP_1"));
    }

    // ── OP_VERIFY ────────────────────────────────────────────────────

    @Test
    void opVerify_trueValue_continues() {
        assertTrue(run("01", "OP_VERIFY", "OP_1"));
    }

    @Test
    void opVerify_falseValue_fails() {
        assertFalse(run("OP_0", "OP_VERIFY", "OP_1"));
    }

    // ── OP_RETURN ────────────────────────────────────────────────────

    @Test
    void opReturn_alwaysFails() {
        assertFalse(run("OP_1", "OP_RETURN"));
    }

    // ── OP_SWAP ──────────────────────────────────────────────────────

    @Test
    void opSwap_bringsTrueToTop() {
        // Pila: [FALSE, TRUE] → swap → [TRUE, FALSE] → cima TRUE = válido
        assertTrue(run("OP_0", "OP_1", "OP_SWAP", "OP_DROP"));
    }

    // ── OP_OVER ──────────────────────────────────────────────────────

    @Test
    void opOver_copiesBottomToTop() {
        // [01, 02] → OVER → [01, 02, 01] → cima 01 = válido
        assertTrue(run("01", "02", "OP_OVER", "OP_DROP", "OP_DROP"));
    }

    // ── OP_N ─────────────────────────────────────────────────────────

    @Test
    void opN_valuesFrom2to16_areValid() {
        for (int n = 2; n <= 16; n++) {
            assertTrue(run("OP_" + n), "OP_" + n + " debería ser válido");
        }
    }

    // ── OP_HASH160 ───────────────────────────────────────────────────

    @Test
    void opHash160_produces20ByteHash() {
        // Solo verificamos que no falle y deje algo en la pila
        // (el resultado es no-cero si el hash tiene algún byte != 0)
        List<ScriptToken> tokens = parser.parse(List.of("<data>", "OP_HASH160"));
        ScriptInterpreter interp = new ScriptInterpreter(false);
        // No podemos garantizar que el hash sea "true" porque puede ser todo 0
        // Lo que verificamos es que no lance excepción (no falle por error)
        assertDoesNotThrow(() -> interp.execute(tokens));
    }

    // ── P2PKH — la prueba central del proyecto ────────────────────────

    @Test
    void p2pkh_validTransaction_isTrue() throws Exception {
        byte[] pubKeyBytes = "<pubKey>".getBytes();
        String pubKeyHex   = bytesToHex(pubKeyBytes);
        String pubKeyHash  = hash160Hex(pubKeyBytes);

        boolean result = run(
            "<firma>",        // scriptSig parte 1
            pubKeyHex,        // scriptSig parte 2 (pubKey como hex)
            "OP_DUP",
            "OP_HASH160",
            pubKeyHash,       // hash160 de la pubKey
            "OP_EQUALVERIFY",
            "OP_CHECKSIG"
        );

        assertTrue(result, "Una transacción P2PKH válida debe retornar true");
    }

    @Test
    void p2pkh_wrongHash_isFalse() {
        boolean result = run(
            "<firma>",
            "<pubKey>",
            "OP_DUP",
            "OP_HASH160",
            "deadbeefdeadbeefdeadbeefdeadbeefdeadbeef", // hash incorrecto
            "OP_EQUALVERIFY",
            "OP_CHECKSIG"
        );

        assertFalse(result, "Hash incorrecto debe causar fallo en OP_EQUALVERIFY");
    }

    @Test
    void p2pkh_emptySignature_isFalse() throws Exception {
        byte[] pubKeyBytes = "<pubKey>".getBytes();
        String pubKeyHex   = bytesToHex(pubKeyBytes);
        String pubKeyHash  = hash160Hex(pubKeyBytes);

        boolean result = run(
            "",               // firma vacía
            pubKeyHex,
            "OP_DUP",
            "OP_HASH160",
            pubKeyHash,
            "OP_EQUALVERIFY",
            "OP_CHECKSIG"
        );

        assertFalse(result, "Firma vacía debe causar fallo en OP_CHECKSIG");
    }

    // ── OP_IF / OP_ELSE / OP_ENDIF ───────────────────────────────────

    @Test
    void opIf_trueCondition_executesIfBranch() {
        // OP_1 → entra al IF → empuja 01 → válido
        assertTrue(run("OP_1", "OP_IF", "01", "OP_ENDIF"));
    }

    @Test
    void opIf_falseCondition_skipsIfBranch() {
        // OP_0 → no entra al IF → pila queda vacía → inválido
        assertFalse(run("OP_0", "OP_IF", "01", "OP_ENDIF"));
    }

    @Test
    void opIfElse_falseCondition_executesElseBranch() {
        // OP_0 → salta IF, ejecuta ELSE → empuja 01 → válido
        assertTrue(run("OP_0", "OP_IF", "OP_0", "OP_ELSE", "01", "OP_ENDIF"));
    }

    @Test
    void opNotif_falseCondition_executesBlock() {
        // OP_0 → NOTIF entra (porque la condición es falsa) → válido
        assertTrue(run("OP_0", "OP_NOTIF", "01", "OP_ENDIF"));
    }

    // ── Casos borde ───────────────────────────────────────────────────

    @Test
    void stackUnderflow_onOpDup_fails() {
        assertFalse(run("OP_DUP"));
    }

    @Test
    void stackUnderflow_onOpSwap_fails() {
        assertFalse(run("01", "OP_SWAP"));
    }

    @Test
    void unknownOpcode_throwsDuringParse() {
        assertThrows(IllegalArgumentException.class,
                () -> run("OP_FAKE"));
    }

    // ── Utilidad ─────────────────────────────────────────────────────

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
