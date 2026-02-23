package edu.uvg.opcodes;

import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.model.ScriptElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;

/** Tests para DataOpcodes: OP_0, OP_1, OP_N y PUSHDATA. */
class DataOpcodesTest {

    private Deque<byte[]> stack;

    @BeforeEach
    void setUp() {
        stack = new ArrayDeque<>();
    }

    @Test
    void opFalse_pushesEmptyArray() throws Exception {
        DataOpcodes.opFalse().execute(stack, null);
        assertArrayEquals(new byte[0], stack.pop());
    }

    @Test
    void opFalse_isFalsy() throws Exception {
        DataOpcodes.opFalse().execute(stack, null);
        assertFalse(new ScriptElement(stack.pop()).toBoolean());
    }

    @Test
    void opTrue_pushesOne() throws Exception {
        DataOpcodes.opTrue().execute(stack, null);
        assertArrayEquals(new byte[]{0x01}, stack.pop());
    }

    @Test
    void opTrue_isTruthy() throws Exception {
        DataOpcodes.opTrue().execute(stack, null);
        assertTrue(new ScriptElement(stack.pop()).toBoolean());
    }

    @Test
    void opN_pushesCorrectInteger() throws Exception {
        for (int n = 2; n <= 16; n++) {
            stack.clear();
            DataOpcodes.opN(n).execute(stack, null);
            assertEquals(n, new ScriptElement(stack.pop()).toInt(),
                    "OP_" + n + " debería empujar " + n);
        }
    }

    @Test
    void pushData_pushesOperand() throws Exception {
        byte[] data = {0x01, 0x02, 0x03};
        DataOpcodes.pushData().execute(stack, data);
        assertArrayEquals(data, stack.pop());
    }

    @Test
    void pushData_nullOperand_throws() {
        assertThrows(ScriptExecutionException.class,
                () -> DataOpcodes.pushData().execute(stack, null));
    }
}
