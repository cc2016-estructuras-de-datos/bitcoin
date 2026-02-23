package edu.uvg.data;

import edu.uvg.exceptions.InvalidOperandException;
import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.interfaces.OpcodeHandler;
import edu.uvg.model.OpcodeType;

import java.util.Arrays;
import java.util.Deque;

/**
 * Implementación de PUSHDATA1 (0x4C).
 * El primer byte del operand indica N, la cantidad de bytes de datos
 * a empujar. Los siguientes N bytes son el payload.
 *
 * Formato de operand esperado:
 *   [ N (1 byte) | data (N bytes) ]
 *
 * Complejidad: O(N) para la copia del buffer.
 *
 * @author Weslly Cabrera
 * @version 1.0
 */
public class OpPushData1 implements OpcodeHandler {

    @Override
    public void execute(Deque<byte[]> stack, byte[] operand)
            throws ScriptExecutionException {

        // validar que exista al menos el byte de longitud
        if (operand == null || operand.length < 1) {
            throw new InvalidOperandException(OpcodeType.PUSHDATA1, 1,
                    operand == null ? 0 : operand.length);
        }

        int length = operand[0] & 0xFF;

        // validar que el operand contenga los bytes prometidos
        if (operand.length < 1 + length) {
            throw new InvalidOperandException(OpcodeType.PUSHDATA1,
                    1 + length, operand.length);
        }

        byte[] payload = Arrays.copyOfRange(operand, 1, 1 + length);
        stack.push(payload);
    }
}
