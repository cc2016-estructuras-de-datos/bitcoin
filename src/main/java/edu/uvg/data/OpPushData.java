package edu.uvg.data;

import edu.uvg.exceptions.InvalidOperandException;
import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.interfaces.OpcodeHandler;
import edu.uvg.model.OpcodeType;

import java.util.Arrays;
import java.util.Deque;

/**
 * Implementación base para opcodes que empujan datos arbitrarios.
 * Recibe el buffer de datos directamente como operand y lo empuja
 * al tope de la pila.
 *
 * Usado cuando el parser ya extrajo los bytes del script y los entrega
 * como operand al handler. OpPushData1 y OpPushData2 delegan aquí
 * su lógica de empuje una vez que determinan la longitud.
 *
 * Complejidad: O(k) donde k = operand.length
 *
 * @author Weslly Cabrera
 * @version 1.0
 */
public class OpPushData implements OpcodeHandler {

    @Override
    public void execute(Deque<byte[]> stack, byte[] operand)
            throws ScriptExecutionException {
        if (operand == null) {
            throw new InvalidOperandException(OpcodeType.PUSHDATA1, 1, 0);
        }

        stack.push(Arrays.copyOf(operand, operand.length));
    }
}
