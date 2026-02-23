package edu.uvg.data;

import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.interfaces.OpcodeHandler;

import java.util.Deque;

/**
 * Implementación de OP_0 / OP_FALSE.
 * Empuja un byte vector vacío (new byte[0]) al tope de la pila,
 * que representa el valor falso o el entero cero en Bitcoin Script.
 *
 * Complejidad: O(1)
 *
 * @author Weslly Cabrera
 * @version 1.0
 */
public class OpFalse implements OpcodeHandler {

    @Override
    public void execute(Deque<byte[]> stack, byte[] operand)
            throws ScriptExecutionException {
        stack.push(new byte[0]);
    }
}
