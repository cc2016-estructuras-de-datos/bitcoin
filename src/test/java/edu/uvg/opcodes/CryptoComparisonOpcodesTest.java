package edu.uvg.opcodes;

import edu.uvg.BaseTest;
import edu.uvg.exceptions.EmptyStackException;
import edu.uvg.exceptions.ScriptExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;

/** Tests para CryptoComparisonOpcodes: OP_EQUAL, OP_EQUALVERIFY, OP_HASH160, OP_CHECKSIG. */
class CryptoComparisonOpcodesTest extends BaseTest {

    private Deque<byte[]> stack;

    @BeforeEach
    void setUp() {
        stack = new ArrayDeque<>();
    }

    // ── OP_EQUAL ─────────────────────────────────────────────────────

    @Test
    void opEqual_sameValues_pushesTrue() throws Exception {
        stack.push(new byte[]{0x01, 0x02});
        stack.push(new byte[]{0x01, 0x02});
        CryptoComparisonOpcodes.opEqual().execute(stack, null);
        assertArrayEquals(new byte[]{1}, stack.pop());
    }

    @Test
    void opEqual_differentValues_pushesFalse() throws Exception {
        stack.push(new byte[]{0x01});
        stack.push(new byte[]{0x02});
        CryptoComparisonOpcodes.opEqual().execute(stack, null);
        assertArrayEquals(new byte[0], stack.pop());
    }

    @Test
    void opEqual_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> CryptoComparisonOpcodes.opEqual().execute(stack, null));
    }

    @Test
    void opEqual_oneElement_throws() {
        stack.push(new byte[]{0x01});
        assertThrows(EmptyStackException.class,
                () -> CryptoComparisonOpcodes.opEqual().execute(stack, null));
    }

    // ── OP_EQUALVERIFY ───────────────────────────────────────────────

    @Test
    void opEqualVerify_sameValues_doesNotThrow() {
        stack.push(new byte[]{(byte) 0xAB});
        stack.push(new byte[]{(byte) 0xAB});
        assertDoesNotThrow(
                () -> CryptoComparisonOpcodes.opEqualVerify().execute(stack, null));
        assertTrue(stack.isEmpty()); // los dos elementos fueron consumidos
    }

    @Test
    void opEqualVerify_differentValues_throws() {
        stack.push(new byte[]{0x01});
        stack.push(new byte[]{0x02});
        assertThrows(ScriptExecutionException.class,
                () -> CryptoComparisonOpcodes.opEqualVerify().execute(stack, null));
    }

    @Test
    void opEqualVerify_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> CryptoComparisonOpcodes.opEqualVerify().execute(stack, null));
    }

    // ── OP_HASH160 ───────────────────────────────────────────────────

    @Test
    void opHash160_producesNonEmptyResult() throws Exception {
        stack.push("hola".getBytes());
        CryptoComparisonOpcodes.opHash160().execute(stack, null);
        assertFalse(stack.isEmpty());
        assertEquals(20, stack.pop().length); // HASH160 siempre produce 20 bytes
    }

    @Test
    void opHash160_deterministicForSameInput() throws Exception {
        byte[] input = "bitcoin".getBytes();

        stack.push(input.clone());
        CryptoComparisonOpcodes.opHash160().execute(stack, null);
        byte[] hash1 = stack.pop();

        stack.push(input.clone());
        CryptoComparisonOpcodes.opHash160().execute(stack, null);
        byte[] hash2 = stack.pop();

        assertArrayEquals(hash1, hash2);
    }

    @Test
    void opHash160_differentInputs_differentHashes() throws Exception {
        stack.push("input1".getBytes());
        CryptoComparisonOpcodes.opHash160().execute(stack, null);
        byte[] hash1 = stack.pop();

        stack.push("input2".getBytes());
        CryptoComparisonOpcodes.opHash160().execute(stack, null);
        byte[] hash2 = stack.pop();

        assertFalse(java.util.Arrays.equals(hash1, hash2));
    }

    @Test
    void opHash160_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> CryptoComparisonOpcodes.opHash160().execute(stack, null));
    }

    // ── OP_CHECKSIG (mock) ───────────────────────────────────────────

    @Test
    void opCheckSigMock_nonEmptyFirmaAndKey_pushesTrue() throws Exception {
        stack.push(new byte[]{0x02, (byte) 0xAB}); // pubKey
        stack.push(new byte[]{0x30, 0x45}); // firma
        CryptoComparisonOpcodes.opCheckSigMock().execute(stack, null);
        assertArrayEquals(new byte[]{1}, stack.pop());
    }

    @Test
    void opCheckSigMock_emptySignature_pushesFalse() throws Exception {
        stack.push(new byte[]{0x02, (byte) 0xAB}); // pubKey
        stack.push(new byte[0]);             // firma vacía
        CryptoComparisonOpcodes.opCheckSigMock().execute(stack, null);
        assertArrayEquals(new byte[0], stack.pop());
    }

    @Test
    void opCheckSigMock_emptyPublicKey_pushesFalse() throws Exception {
        stack.push(new byte[0]);             // pubKey vacía
        stack.push(new byte[]{0x30, 0x45}); // firma
        CryptoComparisonOpcodes.opCheckSigMock().execute(stack, null);
        assertArrayEquals(new byte[0], stack.pop());
    }

    @Test
    void opCheckSigMock_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> CryptoComparisonOpcodes.opCheckSigMock().execute(stack, null));
    }
    
    // ── OP_SHA256 ────────────────────────────────────────────────────

    @Test
    void opSha256_produces32Bytes() throws Exception {
        stack.push("hola".getBytes());
        CryptoComparisonOpcodes.opSha256().execute(stack, null);
        assertEquals(32, stack.pop().length);
    }

    @Test
    void opSha256_deterministic() throws Exception {
        byte[] input = "bitcoin".getBytes();
        stack.push(input.clone());
        CryptoComparisonOpcodes.opSha256().execute(stack, null);
        byte[] hash1 = stack.pop();

        stack.push(input.clone());
        CryptoComparisonOpcodes.opSha256().execute(stack, null);
        byte[] hash2 = stack.pop();

        assertArrayEquals(hash1, hash2);
    }

    @Test
    void opSha256_differentInputs_differentHashes() throws Exception {
        stack.push("input1".getBytes());
        CryptoComparisonOpcodes.opSha256().execute(stack, null);
        byte[] h1 = stack.pop();

        stack.push("input2".getBytes());
        CryptoComparisonOpcodes.opSha256().execute(stack, null);
        byte[] h2 = stack.pop();

        assertFalse(Arrays.equals(h1, h2));
    }

    @Test
    void opSha256_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> CryptoComparisonOpcodes.opSha256().execute(stack, null));
    }

    // ── OP_HASH256 ───────────────────────────────────────────────────

    @Test
    void opHash256_produces32Bytes() throws Exception {
        stack.push("data".getBytes());
        CryptoComparisonOpcodes.opHash256().execute(stack, null);
        assertEquals(32, stack.pop().length);
    }

    @Test
    void opHash256_differentFromSingleSha256() throws Exception {
        byte[] input = "test".getBytes();

        stack.push(input.clone());
        CryptoComparisonOpcodes.opSha256().execute(stack, null);
        byte[] singleHash = stack.pop();

        stack.push(input.clone());
        CryptoComparisonOpcodes.opHash256().execute(stack, null);
        byte[] doubleHash = stack.pop();

        assertFalse(Arrays.equals(singleHash, doubleHash),
                "OP_HASH256 debe diferir de un SHA-256 simple");
    }

    @Test
    void opHash256_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> CryptoComparisonOpcodes.opHash256().execute(stack, null));
    }

    // ── OP_CHECKSIGVERIFY ────────────────────────────────────────────

    @Test
    void opCheckSigVerify_validSignatureAndKey_doesNotThrow() {
        stack.push(new byte[]{0x02, 0x03}); // pubKey
        stack.push(new byte[]{0x30, 0x45}); // firma
        assertDoesNotThrow(
                () -> CryptoComparisonOpcodes.opCheckSigVerifyMock().execute(stack, null));
        assertTrue(stack.isEmpty());
    }

    @Test
    void opCheckSigVerify_emptySignature_throws() {
        stack.push(new byte[]{0x02, 0x03}); // pubKey
        stack.push(new byte[0]);             // firma vacía
        assertThrows(ScriptExecutionException.class,
                () -> CryptoComparisonOpcodes.opCheckSigVerifyMock().execute(stack, null));
    }

    @Test
    void opCheckSigVerify_emptyPublicKey_throws() {
        stack.push(new byte[0]);             // pubKey vacía
        stack.push(new byte[]{0x30, 0x45}); // firma
        assertThrows(ScriptExecutionException.class,
                () -> CryptoComparisonOpcodes.opCheckSigVerifyMock().execute(stack, null));
    }

    @Test
    void opCheckSigVerify_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> CryptoComparisonOpcodes.opCheckSigVerifyMock().execute(stack, null));
    }

    // ── OP_CHECKMULTISIG (mock) ───────────────────────────────────────

    @Test
    void opCheckMultiSig_2of3_allValid_pushesTrue() throws Exception {
        // Protocolo (cima → fondo): OP_0 | firma1 | firma2 | 2 | key1 | key2 | key3 | 3
        stack.push(edu.uvg.model.ScriptElement.fromInt(3).getData()); // N=3
        stack.push(new byte[]{0x03});  // key3
        stack.push(new byte[]{0x02});  // key2
        stack.push(new byte[]{0x01});  // key1
        stack.push(edu.uvg.model.ScriptElement.fromInt(2).getData()); // M=2
        stack.push(new byte[]{0x30, 0x45}); // firma2
        stack.push(new byte[]{0x30, 0x44}); // firma1
        stack.push(new byte[0]);  // OP_0 extra (bug Bitcoin)

        CryptoComparisonOpcodes.opCheckMultiSigMock().execute(stack, null);
        assertArrayEquals(new byte[]{1}, stack.pop());
    }

    @Test
    void opCheckMultiSig_1of2_validSignature_pushesTrue() throws Exception {
        stack.push(edu.uvg.model.ScriptElement.fromInt(2).getData()); // N=2
        stack.push(new byte[]{0x02});  // key2
        stack.push(new byte[]{0x01});  // key1
        stack.push(edu.uvg.model.ScriptElement.fromInt(1).getData()); // M=1
        stack.push(new byte[]{0x30, 0x45}); // firma1
        stack.push(new byte[0]);  // OP_0 extra

        CryptoComparisonOpcodes.opCheckMultiSigMock().execute(stack, null);
        assertArrayEquals(new byte[]{1}, stack.pop());
    }

    @Test
    void opCheckMultiSig_emptySignature_pushesFalse() throws Exception {
        stack.push(edu.uvg.model.ScriptElement.fromInt(1).getData()); // N=1
        stack.push(new byte[]{0x01});  // key1
        stack.push(edu.uvg.model.ScriptElement.fromInt(1).getData()); // M=1
        stack.push(new byte[0]);  // firma vacía
        stack.push(new byte[0]);  // OP_0 extra

        CryptoComparisonOpcodes.opCheckMultiSigMock().execute(stack, null);
        assertArrayEquals(new byte[0], stack.pop());
    }

    @Test
    void opCheckMultiSig_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> CryptoComparisonOpcodes.opCheckMultiSigMock().execute(stack, null));
    }
}
