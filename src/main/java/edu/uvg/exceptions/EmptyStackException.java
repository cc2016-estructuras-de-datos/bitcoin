package edu.uvg.exceptions;

import edu.uvg.model.OpcodeType;

/**
 * Se lanza cuando un opcode intenta hacer pop() o peek()
 * sobre una pila vacía.
 *
 * @author Weslly Cabrera
 * @version 1.0
 */
public class EmptyStackException extends ScriptExecutionException {

    /**
     * @param opcode opcode que intentó acceder a una pila vacía
     */
    public EmptyStackException(OpcodeType opcode) {
        super(opcode, "Se intentó acceder a la pila pero está vacía.");
    }
}
