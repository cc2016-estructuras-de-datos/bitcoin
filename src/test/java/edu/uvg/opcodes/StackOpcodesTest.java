package edu.uvg.opcodes;

import edu.uvg.exceptions.EmptyStackException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;

/** Tests para StackOpcodes: OP_DUP, OP_DROP, OP_SWAP, OP_OVER. */
class StackOpcodesTest {

    private Deque<byte[]> stack;

    @BeforeEach
    void setUp() {
        stack = new ArrayDeque<>();
    }

    // ── OP_DUP ───────────────────────────────────────────────────────

    @Test
    void opDup_duplicatesTop() throws Exception {
        stack.push(new byte[]{0x01});
        StackOpcodes.opDup().execute(stack, null);
        assertEquals(2, stack.size());
        assertArrayEquals(new byte[]{0x01}, stack.pop());
        assertArrayEquals(new byte[]{0x01}, stack.pop());
    }

    @Test
    void opDup_copyIsIndependent() throws Exception {
        byte[] original = {0x01};
        stack.push(original);
        StackOpcodes.opDup().execute(stack, null);
        stack.pop()[0] = (byte) 0xFF; // mutar la copia no afecta al original
        assertArrayEquals(new byte[]{0x01}, stack.pop());
    }

    @Test
    void opDup_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> StackOpcodes.opDup().execute(stack, null));
    }

    // ── OP_DROP ──────────────────────────────────────────────────────

    @Test
    void opDrop_removesTop() throws Exception {
        stack.push(new byte[]{0x01});
        stack.push(new byte[]{0x02});
        StackOpcodes.opDrop().execute(stack, null);
        assertEquals(1, stack.size());
        assertArrayEquals(new byte[]{0x01}, stack.pop());
    }

    @Test
    void opDrop_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> StackOpcodes.opDrop().execute(stack, null));
    }

    // ── OP_SWAP ──────────────────────────────────────────────────────

    @Test
    void opSwap_swapsTopTwo() throws Exception {
        stack.push(new byte[]{0x01}); // fondo
        stack.push(new byte[]{0x02}); // cima
        StackOpcodes.opSwap().execute(stack, null);
        assertArrayEquals(new byte[]{0x01}, stack.pop()); // ahora 0x01 está en la cima
        assertArrayEquals(new byte[]{0x02}, stack.pop());
    }

    @Test
    void opSwap_oneElement_throws() {
        stack.push(new byte[]{0x01});
        assertThrows(EmptyStackException.class,
                () -> StackOpcodes.opSwap().execute(stack, null));
    }

    @Test
    void opSwap_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> StackOpcodes.opSwap().execute(stack, null));
    }

    // ── OP_OVER ──────────────────────────────────────────────────────

    @Test
    void opOver_copiesSecondToTop() throws Exception {
        stack.push(new byte[]{0x01}); // fondo
        stack.push(new byte[]{0x02}); // cima
        StackOpcodes.opOver().execute(stack, null);
        // resultado esperado: [ 0x01 | 0x02 | 0x01 ] (cima = 0x01)
        assertEquals(3, stack.size());
        assertArrayEquals(new byte[]{0x01}, stack.pop()); // cima = copia del segundo
        assertArrayEquals(new byte[]{0x02}, stack.pop());
        assertArrayEquals(new byte[]{0x01}, stack.pop());
    }

    @Test
    void opOver_oneElement_throws() {
        stack.push(new byte[]{0x01});
        assertThrows(EmptyStackException.class,
                () -> StackOpcodes.opOver().execute(stack, null));
    }
}
