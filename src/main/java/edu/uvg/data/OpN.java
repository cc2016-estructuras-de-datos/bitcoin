package edu.uvg.data;

import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.interfaces.OpcodeHandler;

import java.util.Deque;

/**
 * Implementación de OP_1 .. OP_16 (y OP_TRUE como alias de OP_1).
 * Empuja el byte vector new byte[]{(byte) n} al tope de la pila,
 * donde n es el valor entero 1 a 16.
 *
 * Una sola clase cubre los 16 opcodes parametrizando el valor en
 * el constructor, evitando duplicación de código.
 *
 * Complejidad: O(1)
 *
 * @author Weslly Cabrera
 * @version 1.0
 */
public class OpN implements OpcodeHandler {

    private final int n;

    /**
     * @param n valor entero a empujar; debe estar en el rango [1, 16]
     * @throws IllegalArgumentException si n está fuera del rango permitido
     */
    public OpN(int n) {
        if (n < 1 || n > 16) {
            throw new IllegalArgumentException(
                    "OpN solo acepta valores entre 1 y 16, se recibió: " + n
            );
        }
        this.n = n;
    }

    @Override
    public void execute(Deque<byte[]> stack, byte[] operand)
            throws ScriptExecutionException {
        stack.push(new byte[]{(byte) n});
    }

    public int getN() {
        return n;
    }
}
