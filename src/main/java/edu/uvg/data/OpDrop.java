package edu.uvg.data;

import edu.uvg.exceptions.EmptyStackException;
import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.interfaces.OpcodeHandler;
import edu.uvg.model.OpcodeType;

import java.util.Deque;

/**
 * Implementación de OP_DROP (0x75).
 * Extrae y descarta el elemento en el tope de la pila.
 * No retorna ni usa el valor extraído.
 *
 * Complejidad: O(1)
 *
 * @author Weslly Cabrera
 * @version 1.0
 */
public class OpDrop implements OpcodeHandler {

    @Override
    public void execute(Deque<byte[]> stack, byte[] operand)
            throws ScriptExecutionException {

        if (stack.isEmpty()) {
            throw new EmptyStackException(OpcodeType.OP_DROP);
        }

        stack.pop();
    }
}
