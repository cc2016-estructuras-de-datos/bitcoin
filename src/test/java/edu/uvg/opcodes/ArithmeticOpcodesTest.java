package edu.uvg.opcodes;

import edu.uvg.exceptions.EmptyStackException;
import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.model.ScriptElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para ArithmeticOpcodes:
 * OP_ADD, OP_SUB, OP_NOT, OP_BOOLAND, OP_BOOLOR,
 * OP_NUMEQUALVERIFY, OP_LESSTHAN, OP_GREATERTHAN.
 */
class ArithmeticOpcodesTest {

    private Deque<byte[]> stack;

    @BeforeEach
    void setUp() {
        stack = new ArrayDeque<>();
    }

    // ── OP_ADD ───────────────────────────────────────────────────────

    @Test
    void opAdd_twoPositives_returnsSum() throws Exception {
        stack.push(ScriptElement.fromInt(3).getData());
        stack.push(ScriptElement.fromInt(4).getData());
        ArithmeticOpcodes.opAdd().execute(stack, null);
        assertEquals(7, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opAdd_negativeAndPositive_returnsCorrectSum() throws Exception {
        stack.push(ScriptElement.fromInt(-3).getData());
        stack.push(ScriptElement.fromInt(5).getData());
        ArithmeticOpcodes.opAdd().execute(stack, null);
        assertEquals(2, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opAdd_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> ArithmeticOpcodes.opAdd().execute(stack, null));
    }

    @Test
    void opAdd_oneElement_throws() {
        stack.push(ScriptElement.fromInt(1).getData());
        assertThrows(EmptyStackException.class,
                () -> ArithmeticOpcodes.opAdd().execute(stack, null));
    }

    // ── OP_SUB ───────────────────────────────────────────────────────

    @Test
    void opSub_smallerFromLarger_returnsPositive() throws Exception {
        // pila: cima=2, segundo=5  →  5 - 2 = 3
        stack.push(ScriptElement.fromInt(5).getData());
        stack.push(ScriptElement.fromInt(2).getData());
        ArithmeticOpcodes.opSub().execute(stack, null);
        assertEquals(3, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opSub_largerFromSmaller_returnsNegative() throws Exception {
        // pila: cima=5, segundo=2  →  2 - 5 = -3
        stack.push(ScriptElement.fromInt(2).getData());
        stack.push(ScriptElement.fromInt(5).getData());
        ArithmeticOpcodes.opSub().execute(stack, null);
        assertEquals(-3, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opSub_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> ArithmeticOpcodes.opSub().execute(stack, null));
    }

    // ── OP_NOT ───────────────────────────────────────────────────────

    @Test
    void opNot_zero_pushesOne() throws Exception {
        stack.push(ScriptElement.fromInt(0).getData());
        ArithmeticOpcodes.opNot().execute(stack, null);
        assertEquals(1, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opNot_nonZero_pushesZero() throws Exception {
        stack.push(ScriptElement.fromInt(5).getData());
        ArithmeticOpcodes.opNot().execute(stack, null);
        assertEquals(0, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opNot_one_pushesZero() throws Exception {
        stack.push(ScriptElement.fromInt(1).getData());
        ArithmeticOpcodes.opNot().execute(stack, null);
        assertEquals(0, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opNot_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> ArithmeticOpcodes.opNot().execute(stack, null));
    }

    // ── OP_BOOLAND ───────────────────────────────────────────────────

    @Test
    void opBoolAnd_bothNonZero_pushesOne() throws Exception {
        stack.push(ScriptElement.fromInt(3).getData());
        stack.push(ScriptElement.fromInt(7).getData());
        ArithmeticOpcodes.opBoolAnd().execute(stack, null);
        assertEquals(1, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opBoolAnd_oneZero_pushesZero() throws Exception {
        stack.push(ScriptElement.fromInt(0).getData());
        stack.push(ScriptElement.fromInt(5).getData());
        ArithmeticOpcodes.opBoolAnd().execute(stack, null);
        assertEquals(0, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opBoolAnd_bothZero_pushesZero() throws Exception {
        stack.push(ScriptElement.fromInt(0).getData());
        stack.push(ScriptElement.fromInt(0).getData());
        ArithmeticOpcodes.opBoolAnd().execute(stack, null);
        assertEquals(0, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opBoolAnd_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> ArithmeticOpcodes.opBoolAnd().execute(stack, null));
    }

    // ── OP_BOOLOR ────────────────────────────────────────────────────

    @Test
    void opBoolOr_bothNonZero_pushesOne() throws Exception {
        stack.push(ScriptElement.fromInt(1).getData());
        stack.push(ScriptElement.fromInt(2).getData());
        ArithmeticOpcodes.opBoolOr().execute(stack, null);
        assertEquals(1, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opBoolOr_oneNonZero_pushesOne() throws Exception {
        stack.push(ScriptElement.fromInt(0).getData());
        stack.push(ScriptElement.fromInt(3).getData());
        ArithmeticOpcodes.opBoolOr().execute(stack, null);
        assertEquals(1, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opBoolOr_bothZero_pushesZero() throws Exception {
        stack.push(ScriptElement.fromInt(0).getData());
        stack.push(ScriptElement.fromInt(0).getData());
        ArithmeticOpcodes.opBoolOr().execute(stack, null);
        assertEquals(0, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opBoolOr_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> ArithmeticOpcodes.opBoolOr().execute(stack, null));
    }

    // ── OP_NUMEQUALVERIFY ─────────────────────────────────────────────

    @Test
    void opNumEqualVerify_sameIntegers_doesNotThrow() {
        stack.push(ScriptElement.fromInt(42).getData());
        stack.push(ScriptElement.fromInt(42).getData());
        assertDoesNotThrow(
                () -> ArithmeticOpcodes.opNumEqualVerify().execute(stack, null));
        assertTrue(stack.isEmpty());
    }

    @Test
    void opNumEqualVerify_differentIntegers_throws() {
        stack.push(ScriptElement.fromInt(1).getData());
        stack.push(ScriptElement.fromInt(2).getData());
        assertThrows(ScriptExecutionException.class,
                () -> ArithmeticOpcodes.opNumEqualVerify().execute(stack, null));
    }

    @Test
    void opNumEqualVerify_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> ArithmeticOpcodes.opNumEqualVerify().execute(stack, null));
    }

    // ── OP_LESSTHAN ──────────────────────────────────────────────────

    @Test
    void opLessThan_secondLessThanFirst_pushesOne() throws Exception {
        // cima=5, segundo=3  →  3 < 5  → TRUE
        stack.push(ScriptElement.fromInt(3).getData());
        stack.push(ScriptElement.fromInt(5).getData());
        ArithmeticOpcodes.opLessThan().execute(stack, null);
        assertEquals(1, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opLessThan_secondGreaterThanFirst_pushesZero() throws Exception {
        // cima=2, segundo=7  →  7 < 2  → FALSE
        stack.push(ScriptElement.fromInt(7).getData());
        stack.push(ScriptElement.fromInt(2).getData());
        ArithmeticOpcodes.opLessThan().execute(stack, null);
        assertEquals(0, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opLessThan_equalValues_pushesZero() throws Exception {
        stack.push(ScriptElement.fromInt(5).getData());
        stack.push(ScriptElement.fromInt(5).getData());
        ArithmeticOpcodes.opLessThan().execute(stack, null);
        assertEquals(0, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opLessThan_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> ArithmeticOpcodes.opLessThan().execute(stack, null));
    }

    // ── OP_GREATERTHAN ───────────────────────────────────────────────

    @Test
    void opGreaterThan_secondGreaterThanFirst_pushesOne() throws Exception {
        // cima=2, segundo=7  →  7 > 2  → TRUE
        stack.push(ScriptElement.fromInt(7).getData());
        stack.push(ScriptElement.fromInt(2).getData());
        ArithmeticOpcodes.opGreaterThan().execute(stack, null);
        assertEquals(1, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opGreaterThan_secondLessThanFirst_pushesZero() throws Exception {
        // cima=5, segundo=3  →  3 > 5  → FALSE
        stack.push(ScriptElement.fromInt(3).getData());
        stack.push(ScriptElement.fromInt(5).getData());
        ArithmeticOpcodes.opGreaterThan().execute(stack, null);
        assertEquals(0, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opGreaterThan_equalValues_pushesZero() throws Exception {
        stack.push(ScriptElement.fromInt(4).getData());
        stack.push(ScriptElement.fromInt(4).getData());
        ArithmeticOpcodes.opGreaterThan().execute(stack, null);
        assertEquals(0, new ScriptElement(stack.pop()).toInt());
    }

    @Test
    void opGreaterThan_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> ArithmeticOpcodes.opGreaterThan().execute(stack, null));
    }
}