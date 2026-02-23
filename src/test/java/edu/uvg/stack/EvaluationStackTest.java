package edu.uvg.stack;

import edu.uvg.exceptions.EmptyStackException;
import edu.uvg.model.OpcodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests para EvaluationStack: operaciones básicas y casos borde. */
class EvaluationStackTest {

    private EvaluationStack stack;

    @BeforeEach
    void setUp() {
        stack = new EvaluationStack();
    }

    // ── push / pop ────────────────────────────────────────────────────

    @Test
    void pushAndPop_returnsSameBytes() throws Exception {
        byte[] data = {0x01, 0x02};
        stack.push(data);
        assertArrayEquals(data, stack.pop(OpcodeType.OP_DUP));
    }

    @Test
    void push_null_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> stack.push(null));
    }

    @Test
    void pop_emptyStack_throwsEmptyStackException() {
        assertThrows(EmptyStackException.class, () -> stack.pop(OpcodeType.OP_DUP));
    }

    @Test
    void lifoOrder_isRespected() throws Exception {
        stack.push(new byte[]{0x01});
        stack.push(new byte[]{0x02});
        stack.push(new byte[]{0x03});
        assertArrayEquals(new byte[]{0x03}, stack.pop(OpcodeType.OP_DUP));
        assertArrayEquals(new byte[]{0x02}, stack.pop(OpcodeType.OP_DUP));
        assertArrayEquals(new byte[]{0x01}, stack.pop(OpcodeType.OP_DUP));
    }

    // ── peek ─────────────────────────────────────────────────────────

    @Test
    void peek_doesNotRemoveElement() throws Exception {
        stack.push(new byte[]{0x05});
        stack.peek(OpcodeType.OP_DUP);
        assertEquals(1, stack.size());
    }

    @Test
    void peek_emptyStack_throwsEmptyStackException() {
        assertThrows(EmptyStackException.class, () -> stack.peek(OpcodeType.OP_DUP));
    }

    // ── isEmpty / size ────────────────────────────────────────────────

    @Test
    void isEmpty_trueWhenEmpty() {
        assertTrue(stack.isEmpty());
    }

    @Test
    void isEmpty_falseAfterPush() {
        stack.push(new byte[]{0x01});
        assertFalse(stack.isEmpty());
    }

    @Test
    void size_tracksCorrectly() {
        assertEquals(0, stack.size());
        stack.push(new byte[]{0x01});
        stack.push(new byte[]{0x02});
        assertEquals(2, stack.size());
    }

    // ── defensive copy ────────────────────────────────────────────────

    @Test
    void push_storesDefensiveCopy() throws Exception {
        byte[] data = {0x01, 0x02};
        stack.push(data);
        data[0] = (byte) 0xFF; // mutar el original no debe afectar la pila
        assertArrayEquals(new byte[]{0x01, 0x02}, stack.pop(OpcodeType.OP_DUP));
    }

    // ── snapshot ──────────────────────────────────────────────────────

    @Test
    void snapshot_returnsAllElements() {
        stack.push(new byte[]{0x01});
        stack.push(new byte[]{0x02});
        assertEquals(2, stack.snapshot().size());
    }

    @Test
    void snapshot_doesNotModifyStack() {
        stack.push(new byte[]{0x01});
        stack.snapshot();
        assertEquals(1, stack.size());
    }

    // ── toString ──────────────────────────────────────────────────────

    @Test
    void toString_emptyStack() {
        assertEquals("[ vacía ]", stack.toString());
    }

    @Test
    void toString_nonEmptyStack_containsElements() {
        stack.push(new byte[]{0x01});
        assertTrue(stack.toString().contains("01"));
    }
}
