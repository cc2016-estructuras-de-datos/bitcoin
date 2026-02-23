package edu.uvg.opcodes;

import edu.uvg.exceptions.EmptyStackException;
import edu.uvg.exceptions.ScriptExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;

/** Tests para ControlOpcodes: OP_VERIFY y OP_RETURN. */
class ControlOpcodesTest {

    private Deque<byte[]> stack;

    @BeforeEach
    void setUp() {
        stack = new ArrayDeque<>();
    }

    // ── OP_VERIFY ────────────────────────────────────────────────────

    @Test
    void opVerify_trueValue_consumesElement() throws Exception {
        stack.push(new byte[]{0x01});
        ControlOpcodes.opVerify().execute(stack, null);
        assertTrue(stack.isEmpty()); // el elemento fue consumido
    }

    @Test
    void opVerify_falseValue_throws() {
        stack.push(new byte[0]); // FALSE = array vacío
        assertThrows(ScriptExecutionException.class,
                () -> ControlOpcodes.opVerify().execute(stack, null));
    }

    @Test
    void opVerify_zeroValue_throws() {
        stack.push(new byte[]{0x00});
        assertThrows(ScriptExecutionException.class,
                () -> ControlOpcodes.opVerify().execute(stack, null));
    }

    @Test
    void opVerify_emptyStack_throws() {
        assertThrows(EmptyStackException.class,
                () -> ControlOpcodes.opVerify().execute(stack, null));
    }

    // ── OP_RETURN ────────────────────────────────────────────────────

    @Test
    void opReturn_alwaysThrows() {
        assertThrows(ScriptExecutionException.class,
                () -> ControlOpcodes.opReturn().execute(stack, null));
    }

    @Test
    void opReturn_throwsEvenWithTrueOnStack() {
        stack.push(new byte[]{0x01});
        assertThrows(ScriptExecutionException.class,
                () -> ControlOpcodes.opReturn().execute(stack, null));
    }
}
