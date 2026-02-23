package edu.uvg.data;

import edu.uvg.exceptions.InvalidOperandException;
import edu.uvg.exceptions.ScriptExecutionException;
import edu.uvg.interfaces.OpcodeHandler;
import edu.uvg.model.OpcodeType;

import java.util.Arrays;
import java.util.Deque;

/**
 * Implementación de PUSHDATA2 (0x4D).
 * Los primeros 2 bytes del operand indican N en little-endian,
 * la cantidad de bytes de datos a empujar. Los siguientes N bytes
 * son el payload.
 *
 * Formato de operand esperado:
 *   [ N_low (1 byte) | N_high (1 byte) | data (N bytes) ]
 *
 * Complejidad: O(N) para la copia del buffer.
 *
 * @author Weslly Cabrera
 * @version 1.0
 */
public class OpPushData2 implements OpcodeHandler {

    @Override
    public void execute(Deque<byte[]> stack, byte[] operand)
            throws ScriptExecutionException {

        // validar que existan al menos los 2 bytes de longitud
        if (operand == null || operand.length < 2) {
            throw new InvalidOperandException(OpcodeType.PUSHDATA2, 2,
                    operand == null ? 0 : operand.length);
        }

        // longitud en 2 bytes
        int length = (operand[0] & 0xFF) | ((operand[1] & 0xFF) << 8);

        // validar que el operand contenga los bytes prometidos
        if (operand.length < 2 + length) {
            throw new InvalidOperandException(OpcodeType.PUSHDATA2,
                    2 + length, operand.length);
        }

        byte[] payload = Arrays.copyOfRange(operand, 2, 2 + length);
        stack.push(payload);
    }
}
